package com.droneiq.drone.dto;

import com.droneiq.drone.entity.DroneStatus;
import jakarta.validation.constraints.Size;

public record UpdateDroneRequest(
    @Size(min = 2, max = 100, message = "Model name must be between 2 and 100 characters")
    String model,

    DroneStatus status,

    String firmwareVersion,

    String ipAddress
) {}
