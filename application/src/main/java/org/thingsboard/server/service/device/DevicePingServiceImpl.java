package org.thingsboard.server.service.device;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.kv.AttributeKvEntry;
import org.thingsboard.server.controller.DevicePingResponse;
import org.thingsboard.server.dao.attributes.AttributesService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@TbCoreComponent
@RequiredArgsConstructor
@Slf4j
public class DevicePingServiceImpl implements DevicePingService {

    private static final String LAST_ACTIVITY_TIME = "lastActivityTime";
    private static final String ACTIVE = "active";
    private static final String SERVER_SCOPE = "SERVER_SCOPE";

    private final AttributesService attributesService;

    @Value("${device.ping.timeout:60000}")
    private long defaultTimeoutMs;

    @Override
    public DevicePingResponse pingDevice(TenantId tenantId, Device device) {
        log.debug("Checking reachability for device: {} ({})", device.getName(), device.getId());

        Long lastActivityTime = getLastActivityTime(tenantId, device);
        long currentTime = System.currentTimeMillis();

        boolean reachable = false;
        Long inactivitySeconds = null;
        Instant lastSeen = null;

        if (lastActivityTime != null && lastActivityTime > 0) {
            lastSeen = Instant.ofEpochMilli(lastActivityTime);
            inactivitySeconds = (currentTime - lastActivityTime) / 1000;
            reachable = isDeviceReachable(lastActivityTime, defaultTimeoutMs);
        }

        if (!reachable) {
            Boolean isActive = getActiveAttribute(tenantId, device);
            if (isActive != null && isActive) {
                reachable = true;
            }
        }

        return DevicePingResponse.builder()
                .deviceId(device.getId().getId().toString())
                .deviceName(device.getName())
                .reachable(reachable)
                .lastSeen(lastSeen)
                .inactivitySeconds(inactivitySeconds)
                .build();
    }

    @Override
    public boolean isDeviceReachable(long lastActivityTime, long timeoutMs) {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastActivity = currentTime - lastActivityTime;
        return timeSinceLastActivity <= timeoutMs;
    }

    private Long getLastActivityTime(TenantId tenantId, Device device) {
        try {
            List<AttributeKvEntry> attributes = attributesService
                    .find(tenantId, device.getId(), SERVER_SCOPE, List.of(LAST_ACTIVITY_TIME))
                    .get();

            return attributes.stream()
                    .filter(attr -> LAST_ACTIVITY_TIME.equals(attr.getKey()))
                    .findFirst()
                    .map(attr -> attr.getLongValue().orElse(null))
                    .orElse(null);

        } catch (InterruptedException e) {
            log.warn("Interrupted while retrieving lastActivityTime for device {}", device.getId());
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException e) {
            log.warn("Failed to retrieve lastActivityTime for device {}: {}", device.getId(), e.getMessage());
            return null;
        }
    }

    private Boolean getActiveAttribute(TenantId tenantId, Device device) {
        try {
            List<AttributeKvEntry> attributes = attributesService
                    .find(tenantId, device.getId(), SERVER_SCOPE, List.of(ACTIVE))
                    .get();

            return attributes.stream()
                    .filter(attr -> ACTIVE.equals(attr.getKey()))
                    .findFirst()
                    .map(attr -> attr.getBooleanValue().orElse(false))
                    .orElse(null);

        } catch (InterruptedException e) {
            log.warn("Interrupted while retrieving active attribute for device {}", device.getId());
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException e) {
            log.warn("Failed to retrieve active attribute for device {}: {}", device.getId(), e.getMessage());
            return null;
        }
    }
}