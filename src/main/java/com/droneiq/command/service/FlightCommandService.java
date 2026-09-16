package com.droneiq.command.service;

import com.droneiq.command.dto.FlightCommandRequest;
import com.droneiq.command.dto.FlightCommandResponse;
import com.droneiq.command.entity.FlightCommand;
import com.droneiq.command.repository.FlightCommandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FlightCommandService {

    private final FlightCommandRepository commandRepository;

    public FlightCommandService(FlightCommandRepository commandRepository) {
        this.commandRepository = commandRepository;
    }

    @Transactional
    public FlightCommandResponse executeRoutineCommand(FlightCommandRequest request, String pilotUsername) {
        FlightCommand command = new FlightCommand(
                request.droneId(),
                request.commandType().toUpperCase(),
                false,
                pilotUsername,
                "EXECUTED",
                request.payloadJson()
        );
        FlightCommand saved = commandRepository.save(command);
        return FlightCommandResponse.fromEntity(saved);
    }

    @Transactional
    public FlightCommandResponse executeOverrideCommand(FlightCommandRequest request, String managerUsername) {
        FlightCommand command = new FlightCommand(
                request.droneId(),
                request.commandType().toUpperCase(),
                true,
                managerUsername,
                "OVERRIDE_EXECUTED",
                request.payloadJson()
        );
        FlightCommand saved = commandRepository.save(command);
        return FlightCommandResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<FlightCommandResponse> getCommandHistory(String droneId) {
        List<FlightCommand> commands = (droneId != null && !droneId.isBlank())
                ? commandRepository.findByDroneIdOrderByExecutedAtDesc(droneId)
                : commandRepository.findAll();
        return commands.stream()
                .map(FlightCommandResponse::fromEntity)
                .toList();
    }
}
