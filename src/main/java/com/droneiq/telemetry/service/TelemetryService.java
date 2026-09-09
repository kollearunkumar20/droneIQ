package com.droneiq.telemetry.service;

import com.droneiq.drone.entity.DroneStatus;
import com.droneiq.drone.service.DroneService;
import com.droneiq.telemetry.dto.TelemetryMessage;
import com.droneiq.telemetry.storage.TelemetryStorageService;
import com.droneiq.telemetry.validation.TelemetryValidator;
import com.droneiq.telemetry.websocket.TelemetryWebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TelemetryService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryService.class);

    private final CurrentDroneTelemetryService currentTelemetryService;
    private final TelemetryWebSocketService webSocketService;
    private final TelemetryStorageService storageService;
    private final TelemetryValidator validator;
    private final DroneService droneService;

    public TelemetryService(
            CurrentDroneTelemetryService currentTelemetryService,
            TelemetryWebSocketService webSocketService,
            TelemetryStorageService storageService,
            TelemetryValidator validator,
            DroneService droneService) {
        this.currentTelemetryService = currentTelemetryService;
        this.webSocketService = webSocketService;
        this.storageService = storageService;
        this.validator = validator;
        this.droneService = droneService;
    }

    public void processTelemetry(TelemetryMessage message) {
        validator.validate(message);

        // 1. Update latest in-memory state for instantaneous REST lookups
        currentTelemetryService.updateTelemetry(message);

        // 2. Broadcast immediately to authenticated WebSocket subscribers
        webSocketService.broadcastTelemetry(message);

        // 3. Asynchronously record into Data Lake micro-batch buffer
        try {
            storageService.storeTelemetry(message);
        } catch (Exception e) {
            log.error("Error storing telemetry in data lake buffer: {}", e.getMessage());
        }

        // 4. Update drone fleet status if required
        try {
            DroneStatus targetStatus = Boolean.TRUE.equals(message.armed())
                    ? DroneStatus.IN_FLIGHT
                    : DroneStatus.ONLINE;
            droneService.updateDroneStatus(message.droneId(), targetStatus);
        } catch (Exception e) {
            log.trace("Drone status update bypassed: {}", e.getMessage());
        }
    }
}
