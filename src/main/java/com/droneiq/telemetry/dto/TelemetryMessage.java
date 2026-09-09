package com.droneiq.telemetry.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TelemetryMessage(
    @NotBlank(message = "Drone ID is required")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Invalid Drone ID format")
    String droneId,

    @NotNull(message = "Timestamp is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
    Instant timestamp,

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and +90 degrees")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and +90 degrees")
    BigDecimal latitude,

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and +180 degrees")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and +180 degrees")
    BigDecimal longitude,

    @NotNull(message = "Altitude is required")
    @DecimalMin(value = "0.0", message = "Altitude must be non-negative")
    @DecimalMax(value = "10000.0", message = "Altitude must not exceed 10,000 meters")
    BigDecimal altitude,

    @NotNull(message = "Heading is required")
    @DecimalMin(value = "0.0", message = "Heading must be between 0 and 360 degrees")
    @DecimalMax(value = "360.0", message = "Heading must be between 0 and 360 degrees")
    BigDecimal heading,

    @NotNull(message = "Speed is required")
    @DecimalMin(value = "0.0", message = "Speed must be non-negative")
    @DecimalMax(value = "150.0", message = "Speed must not exceed 150 m/s")
    BigDecimal speed,

    @DecimalMin(value = "-180.0", message = "Roll must be between -180 and +180 degrees")
    @DecimalMax(value = "180.0", message = "Roll must be between -180 and +180 degrees")
    BigDecimal roll,

    @DecimalMin(value = "-90.0", message = "Pitch must be between -90 and +90 degrees")
    @DecimalMax(value = "90.0", message = "Pitch must be between -90 and +90 degrees")
    BigDecimal pitch,

    @DecimalMin(value = "0.0", message = "Yaw must be between 0 and 360 degrees")
    @DecimalMax(value = "360.0", message = "Yaw must be between 0 and 360 degrees")
    BigDecimal yaw,

    @NotNull(message = "Battery percentage is required")
    @DecimalMin(value = "0.0", message = "Battery percentage must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Battery percentage must be between 0 and 100")
    BigDecimal batteryPercentage,

    @Min(value = 0, message = "GPS satellite count must be non-negative")
    Integer gpsSatellites,

    @NotBlank(message = "Flight mode is required")
    String flightMode,

    @NotNull(message = "Armed status is required")
    Boolean armed,

    String status,

    Map<String, Object> metadata
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String droneId;
        private Instant timestamp;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private BigDecimal altitude;
        private BigDecimal heading;
        private BigDecimal speed;
        private BigDecimal roll;
        private BigDecimal pitch;
        private BigDecimal yaw;
        private BigDecimal batteryPercentage;
        private Integer gpsSatellites;
        private String flightMode;
        private Boolean armed;
        private String status;
        private Map<String, Object> metadata;

        public Builder droneId(String droneId) {
            this.droneId = droneId;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder latitude(BigDecimal latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder longitude(BigDecimal longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder altitude(BigDecimal altitude) {
            this.altitude = altitude;
            return this;
        }

        public Builder heading(BigDecimal heading) {
            this.heading = heading;
            return this;
        }

        public Builder speed(BigDecimal speed) {
            this.speed = speed;
            return this;
        }

        public Builder roll(BigDecimal roll) {
            this.roll = roll;
            return this;
        }

        public Builder pitch(BigDecimal pitch) {
            this.pitch = pitch;
            return this;
        }

        public Builder yaw(BigDecimal yaw) {
            this.yaw = yaw;
            return this;
        }

        public Builder batteryPercentage(BigDecimal batteryPercentage) {
            this.batteryPercentage = batteryPercentage;
            return this;
        }

        public Builder gpsSatellites(Integer gpsSatellites) {
            this.gpsSatellites = gpsSatellites;
            return this;
        }

        public Builder flightMode(String flightMode) {
            this.flightMode = flightMode;
            return this;
        }

        public Builder armed(Boolean armed) {
            this.armed = armed;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public TelemetryMessage build() {
            return new TelemetryMessage(
                    droneId,
                    timestamp != null ? timestamp : Instant.now(),
                    latitude,
                    longitude,
                    altitude,
                    heading,
                    speed,
                    roll,
                    pitch,
                    yaw,
                    batteryPercentage,
                    gpsSatellites != null ? gpsSatellites : 0,
                    flightMode != null ? flightMode : "MANUAL",
                    armed != null ? armed : false,
                    status != null ? status : "OK",
                    metadata
            );
        }
    }
}
