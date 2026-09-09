package com.droneiq.telemetry.validation;

import com.droneiq.telemetry.dto.TelemetryMessage;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TelemetryValidator {

    private static final Logger log = LoggerFactory.getLogger(TelemetryValidator.class);

    private final Validator validator;

    public TelemetryValidator(Validator validator) {
        this.validator = validator;
    }

    public void validate(TelemetryMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("Telemetry message must not be null");
        }

        Set<ConstraintViolation<TelemetryMessage>> violations = validator.validate(message);
        if (!violations.isEmpty()) {
            String errorMsg = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            log.warn("Invalid telemetry message for drone {}: {}", message.droneId(), errorMsg);
            throw new IllegalArgumentException("Invalid telemetry data: " + errorMsg);
        }
    }

    public boolean isValid(TelemetryMessage message) {
        try {
            validate(message);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
