package com.droneiq.telemetry.service;

import com.droneiq.telemetry.dto.TelemetryMessage;

import java.util.Map;
import java.util.Optional;

public interface CurrentDroneTelemetryService {

    void updateTelemetry(TelemetryMessage message);

    Optional<TelemetryMessage> getLatestTelemetry(String droneId);

    Map<String, TelemetryMessage> getAllLatestTelemetry();
}
