package com.droneiq.telemetry.service;

import com.droneiq.telemetry.dto.TelemetryMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurrentDroneTelemetryServiceTest {

    private InMemoryCurrentDroneTelemetryService service;

    @BeforeEach
    void setUp() {
        service = new InMemoryCurrentDroneTelemetryService();
    }

    private TelemetryMessage createMessage(String droneId, double lat, double lon) {
        return TelemetryMessage.builder()
                .droneId(droneId)
                .timestamp(Instant.now())
                .latitude(BigDecimal.valueOf(lat))
                .longitude(BigDecimal.valueOf(lon))
                .altitude(BigDecimal.valueOf(50.0))
                .heading(BigDecimal.valueOf(90.0))
                .speed(BigDecimal.valueOf(10.0))
                .batteryPercentage(BigDecimal.valueOf(90.0))
                .flightMode("AUTO")
                .armed(true)
                .build();
    }

    @Test
    @DisplayName("Should store and retrieve latest telemetry for drone")
    void testStoreAndRetrieve() {
        TelemetryMessage msg1 = createMessage("DRONE-001", 37.77, -122.41);
        service.updateTelemetry(msg1);

        Optional<TelemetryMessage> latest = service.getLatestTelemetry("DRONE-001");
        assertTrue(latest.isPresent());
        assertEquals("DRONE-001", latest.get().droneId());
        assertEquals(BigDecimal.valueOf(37.77), latest.get().latitude());

        // Update with newer coordinates
        TelemetryMessage msg2 = createMessage("DRONE-001", 37.78, -122.42);
        service.updateTelemetry(msg2);

        Optional<TelemetryMessage> updated = service.getLatestTelemetry("DRONE-001");
        assertTrue(updated.isPresent());
        assertEquals(BigDecimal.valueOf(37.78), updated.get().latitude());
    }

    @Test
    @DisplayName("Should return empty for unknown drone ID")
    void testUnknownDroneReturnsEmpty() {
        Optional<TelemetryMessage> latest = service.getLatestTelemetry("UNKNOWN-DRONE");
        assertFalse(latest.isPresent());
    }

    @Test
    @DisplayName("Should return all latest telemetry records")
    void testGetAllLatest() {
        service.updateTelemetry(createMessage("DRONE-001", 37.1, -122.1));
        service.updateTelemetry(createMessage("DRONE-002", 37.2, -122.2));

        Map<String, TelemetryMessage> all = service.getAllLatestTelemetry();
        assertEquals(2, all.size());
        assertTrue(all.containsKey("DRONE-001"));
        assertTrue(all.containsKey("DRONE-002"));
    }
}
