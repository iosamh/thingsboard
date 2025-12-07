package org.thingsboard.server.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Device Ping Response - Contains device reachability status and activity information")
public class DevicePingResponse {

    @Schema(
            description = "Device ID (UUID format)",
            example = "784f394c-42b6-435a-983c-b7beff2784f9",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String deviceId;

    @Schema(
            description = "Whether the device is reachable. A device is considered reachable if it has " +
                    "sent telemetry data within the configured timeout period (default: 60 seconds)",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean reachable;

    @Schema(
            description = "Last time the device was seen (sent telemetry or connected). " +
                    "Null if device has never been active.",
            example = "2024-12-06T10:30:00Z"
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant lastSeen;

    @Schema(
            description = "Human-readable device name",
            example = "Temperature Sensor 01",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String deviceName;

    @Schema(
            description = "Time since last activity in seconds. Null if device has never been active.",
            example = "120"
    )
    private Long inactivitySeconds;
}