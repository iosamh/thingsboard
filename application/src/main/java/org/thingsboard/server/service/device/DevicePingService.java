package org.thingsboard.server.service.device;

import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.controller.DevicePingResponse;

public interface DevicePingService {

    DevicePingResponse pingDevice(TenantId tenantId, Device device);

    boolean isDeviceReachable(long lastActivityTime, long timeoutMs);
}