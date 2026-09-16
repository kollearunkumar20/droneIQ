package com.droneiq.command.dto;

import com.droneiq.command.entity.FlightCommand;

import java.time.Instant;

public record FlightCommandResponse(
        Long id,
        String droneId,
        String commandType,
        boolean isOverride,
        String executedBy,
        String status,
        String payloadJson,
        Instant executedAt
) {
    public static FlightCommandResponse fromEntity(FlightCommand command) {
        return new FlightCommandResponse(
                command.getId(),
                command.getDroneId(),
                command.getCommandType(),
                command.isOverride(),
                command.getExecutedBy(),
                command.getStatus(),
                command.getPayloadJson(),
                command.getExecutedAt()
        );
    }
}
