package com.droneiq.telemetry.validation;

import com.droneiq.telemetry.dto.TelemetryMessage;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TelemetryValidatorTest {

    private TelemetryValidator validator;

    @BeforeEach
    void setUp() {
        Validator jakartaValidator = Validation.buildDefaultValidatorFactory().getValidator();
        validator = new TelemetryValidator(jakartaValidator);
    }

    private TelemetryMessage.Builder validBuilder() {
        return TelemetryMessage.builder()
                .droneId("DRONE-001")
                .timestamp(Instant.now())
                .latitude(BigDecimal.valueOf(37.7749))
                .longitude(BigDecimal.valueOf(-122.4194))
                .altitude(BigDecimal.valueOf(100.5))
                .heading(BigDecimal.valueOf(180.0))
                .speed(BigDecimal.valueOf(15.2))
                .roll(BigDecimal.valueOf(2.5))
                .pitch(BigDecimal.valueOf(-1.2))
                .yaw(BigDecimal.valueOf(180.0))
                .batteryPercentage(BigDecimal.valueOf(85.0))
                .gpsSatellites(16)
                .flightMode("AUTO")
                .armed(true)
                .status("OK");
    }

    @Test
    @DisplayName("Valid telemetry message passes validation")
    void testValidTelemetry() {
        TelemetryMessage message = validBuilder().build();
        assertDoesNotThrow(() -> validator.validate(message));
        assertTrue(validator.isValid(message));
    }

    @Test
    @DisplayName("Latitude out of range [-90, +90] is rejected")
    void testInvalidLatitude() {
        TelemetryMessage message = validBuilder()
                .latitude(BigDecimal.valueOf(95.0))
                .build();

        assertThrows(IllegalArgumentException.class, () -> validator.validate(message));
        assertFalse(validator.isValid(message));
    }

    @Test
    @DisplayName("Longitude out of range [-180, +180] is rejected")
    void testInvalidLongitude() {
        TelemetryMessage message = validBuilder()
                .longitude(BigDecimal.valueOf(-185.0))
                .build();

        assertThrows(IllegalArgumentException.class, () -> validator.validate(message));
        assertFalse(validator.isValid(message));
    }

    @Test
    @DisplayName("Negative altitude is rejected")
    void testNegativeAltitude() {
        TelemetryMessage message = validBuilder()
                .altitude(BigDecimal.valueOf(-5.0))
                .build();

        assertThrows(IllegalArgumentException.class, () -> validator.validate(message));
        assertFalse(validator.isValid(message));
    }

    @Test
    @DisplayName("Battery percentage > 100 is rejected")
    void testExcessiveBattery() {
        TelemetryMessage message = validBuilder()
                .batteryPercentage(BigDecimal.valueOf(105.0))
                .build();

        assertThrows(IllegalArgumentException.class, () -> validator.validate(message));
        assertFalse(validator.isValid(message));
    }

    @Test
    @DisplayName("Blank drone ID is rejected")
    void testBlankDroneId() {
        TelemetryMessage message = validBuilder()
                .droneId(" ")
                .build();

        assertThrows(IllegalArgumentException.class, () -> validator.validate(message));
        assertFalse(validator.isValid(message));
    }
}
