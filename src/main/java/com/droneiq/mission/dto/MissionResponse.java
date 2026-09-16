package com.droneiq.mission.dto;

import com.droneiq.mission.entity.Mission;

import java.time.Instant;

public record MissionResponse(
        Long id,
        String name,
        String droneId,
        String description,
        String status,
        String waypointsJson,
        String createdBy,
        Instant createdAt,
        Instant updatedAt
) {
    public static MissionResponse fromEntity(Mission mission) {
        return new MissionResponse(
                mission.getId(),
                mission.getName(),
                mission.getDroneId(),
                mission.getDescription(),
                mission.getStatus(),
                mission.getWaypointsJson(),
                mission.getCreatedBy(),
                mission.getCreatedAt(),
                mission.getUpdatedAt()
        );
    }
}
