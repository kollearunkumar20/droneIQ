package com.droneiq.telemetry.service;

import com.droneiq.telemetry.dto.TelemetryMessage;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryCurrentDroneTelemetryService implements CurrentDroneTelemetryService {

    private final Map<String, TelemetryMessage> latestTelemetryMap = new ConcurrentHashMap<>();

    @Override
    public void updateTelemetry(TelemetryMessage message) {
        if (message != null && message.droneId() != null) {
            latestTelemetryMap.put(message.droneId(), message);
        }
    }

    @Override
    public Optional<TelemetryMessage> getLatestTelemetry(String droneId) {
        if (droneId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(latestTelemetryMap.get(droneId));
    }

    @Override
    public Map<String, TelemetryMessage> getAllLatestTelemetry() {
        return Collections.unmodifiableMap(latestTelemetryMap);
    }
}
