package com.droneiq.command.dto;

import jakarta.validation.constraints.NotBlank;

public record FlightCommandRequest(
        @NotBlank(message = "Drone ID is required")
        String droneId,

        @NotBlank(message = "Command type is required (e.g. ARM, TAKEOFF, LAND, RTL, WAYPOINT_NAV)")
        String commandType,

        String payloadJson
) {
}
