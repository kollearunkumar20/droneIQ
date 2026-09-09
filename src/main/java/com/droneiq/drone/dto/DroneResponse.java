package com.droneiq.drone.dto;

import com.droneiq.drone.entity.Drone;
import com.droneiq.drone.entity.DroneStatus;

import java.time.Instant;

public record DroneResponse(
    Long id,
    String droneId,
    String model,
    DroneStatus status,
    String firmwareVersion,
    String ipAddress,
    Instant createdAt,
    Instant updatedAt
) {
    public static DroneResponse fromEntity(Drone drone) {
        return new DroneResponse(
            drone.getId(),
            drone.getDroneId(),
            drone.getModel(),
            drone.getStatus(),
            drone.getFirmwareVersion(),
            drone.getIpAddress(),
            drone.getCreatedAt(),
            drone.getUpdatedAt()
        );
    }
}
