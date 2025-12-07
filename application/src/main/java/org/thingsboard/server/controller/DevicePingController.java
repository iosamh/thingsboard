package org.thingsboard.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.device.DevicePingService;

import static org.thingsboard.server.controller.ControllerConstants.DEVICE_ID;
import static org.thingsboard.server.controller.ControllerConstants.DEVICE_ID_PARAM_DESCRIPTION;
import static org.thingsboard.server.controller.ControllerConstants.TENANT_OR_CUSTOMER_AUTHORITY_PARAGRAPH;

@RestController
@TbCoreComponent
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Device Ping", description = "APIs for checking device reachability")
public class DevicePingController extends BaseController {

    private final DevicePingService devicePingService;

    @Operation(
            operationId = "pingDevice",
            summary = "Ping Device",
            description = "Check if a device is reachable based on its last activity timestamp. " +
                    "A device is considered reachable if it has sent data within the configured timeout period (default: 60 seconds). " +
                    TENANT_OR_CUSTOMER_AUTHORITY_PARAGRAPH
    )
    @ApiResponse(
            responseCode = "200",
            description = "Device ping result",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = DevicePingResponse.class)
            )
    )
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/device/ping/{deviceId}", method = RequestMethod.GET)
    public DevicePingResponse pingDevice(
            @Parameter(description = DEVICE_ID_PARAM_DESCRIPTION)
            @PathVariable(DEVICE_ID) String strDeviceId
    ) throws ThingsboardException {
        checkParameter(DEVICE_ID, strDeviceId);
        DeviceId deviceId = new DeviceId(toUUID(strDeviceId));
        Device device = checkDeviceId(deviceId, org.thingsboard.server.service.security.permission.Operation.READ);
        log.debug("Pinging device: {} ({})", device.getName(), deviceId);
        return devicePingService.pingDevice(getTenantId(), device);
    }
}