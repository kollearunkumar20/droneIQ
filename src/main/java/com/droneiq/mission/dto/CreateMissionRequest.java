package com.droneiq.mission.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateMissionRequest(
        @NotBlank(message = "Mission name is required")
        String name,

        @NotBlank(message = "Target drone ID is required")
        String droneId,

        String description,

        @NotBlank(message = "Waypoints JSON definition is required")
        String waypointsJson
) {
}
