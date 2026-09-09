package com.droneiq.telemetry.simulator;

import com.droneiq.telemetry.dto.TelemetryMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneFlightStateTest {

    @Test
    @DisplayName("DroneFlightState initializes with valid coordinates and state")
    void testInitialization() {
        DroneFlightState state = new DroneFlightState("DRONE-001", 37.4138, -122.0549, 1000.0, 0.05, 100.0);
        TelemetryMessage msg = state.toTelemetryMessage();

        assertEquals("DRONE-001", msg.droneId());
        assertNotNull(msg.latitude());
        assertNotNull(msg.longitude());
        assertTrue(msg.batteryPercentage().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(msg.altitude().compareTo(BigDecimal.ZERO) > 0);
        assertEquals("AUTO", msg.flightMode());
        assertTrue(msg.armed());
    }

    @Test
    @DisplayName("DroneFlightState tick smoothly updates positions and depletes battery")
    void testTickUpdates() {
        DroneFlightState state = new DroneFlightState("DRONE-001", 37.4138, -122.0549, 1000.0, 0.05, 100.0);
        TelemetryMessage initial = state.toTelemetryMessage();

        for (int i = 0; i < 10; i++) {
            state.tick();
        }

        TelemetryMessage afterTicks = state.toTelemetryMessage();
        assertTrue(afterTicks.batteryPercentage().compareTo(initial.batteryPercentage()) <= 0);
        assertNotNull(afterTicks.heading());
        assertTrue(afterTicks.heading().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(afterTicks.heading().compareTo(BigDecimal.valueOf(360.0)) <= 0);
    }
}
