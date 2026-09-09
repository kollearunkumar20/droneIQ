package com.droneiq.drone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateDroneRequest(
    @NotBlank(message = "Drone ID is required")
    @Size(min = 2, max = 50, message = "Drone ID must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Drone ID can only contain alphanumeric characters, underscores, and dashes")
    String droneId,

    @NotBlank(message = "Model is required")
    @Size(min = 2, max = 100, message = "Model name must be between 2 and 100 characters")
    String model,

    String firmwareVersion,

    String ipAddress
) {}
