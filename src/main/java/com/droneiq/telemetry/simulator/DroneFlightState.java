package com.droneiq.telemetry.simulator;

import com.droneiq.telemetry.dto.TelemetryMessage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;

public class DroneFlightState {

    private final String droneId;
    private double latitude;
    private double longitude;
    private double altitude;
    private double heading;
    private double speed;
    private double roll;
    private double pitch;
    private double yaw;
    private double battery;
    private int satellites;
    private String flightMode;
    private boolean armed;

    // Movement parameters
    private final double centerLat;
    private final double centerLon;
    private final double radius;
    private double angle;
    private final double angularSpeed;

    public DroneFlightState(String droneId, double centerLat, double centerLon, double radius, double angularSpeed, double baseAltitude) {
        this.droneId = droneId;
        this.centerLat = centerLat;
        this.centerLon = centerLon;
        this.radius = radius;
        this.angularSpeed = angularSpeed;
        this.angle = Math.random() * 2 * Math.PI;
        this.altitude = baseAltitude;
        this.speed = 12.5;
        this.battery = 98.0;
        this.satellites = 16;
        this.flightMode = "AUTO";
        this.armed = true;
        updatePosition();
    }

    public void tick() {
        angle += angularSpeed;
        if (angle >= 2 * Math.PI) {
            angle -= 2 * Math.PI;
        }

        updatePosition();

        // Simulate gentle battery depletion (0.02% per tick)
        battery = Math.max(5.0, battery - 0.02);

        // Heading is tangent to the circular orbit
        heading = (Math.toDegrees(angle + Math.PI / 2) + 360) % 360;
        yaw = heading;

        // Mild banking roll proportional to turn speed and slight pitch
        roll = Math.sin(angle) * 8.0;
        pitch = Math.cos(angle) * 3.0;

        // Micro altitude fluctuation (+/- 0.5m)
        altitude = Math.max(10.0, altitude + (Math.random() - 0.5) * 0.4);
    }

    private void updatePosition() {
        // Approximate 1 deg latitude ~ 111,000 meters
        double deltaLat = (radius * Math.sin(angle)) / 111000.0;
        double deltaLon = (radius * Math.cos(angle)) / (111000.0 * Math.cos(Math.toRadians(centerLat)));

        this.latitude = centerLat + deltaLat;
        this.longitude = centerLon + deltaLon;
    }

    public TelemetryMessage toTelemetryMessage() {
        return TelemetryMessage.builder()
                .droneId(droneId)
                .timestamp(Instant.now())
                .latitude(BigDecimal.valueOf(latitude).setScale(6, RoundingMode.HALF_UP))
                .longitude(BigDecimal.valueOf(longitude).setScale(6, RoundingMode.HALF_UP))
                .altitude(BigDecimal.valueOf(altitude).setScale(2, RoundingMode.HALF_UP))
                .heading(BigDecimal.valueOf(heading).setScale(1, RoundingMode.HALF_UP))
                .speed(BigDecimal.valueOf(speed).setScale(2, RoundingMode.HALF_UP))
                .roll(BigDecimal.valueOf(roll).setScale(2, RoundingMode.HALF_UP))
                .pitch(BigDecimal.valueOf(pitch).setScale(2, RoundingMode.HALF_UP))
                .yaw(BigDecimal.valueOf(yaw).setScale(2, RoundingMode.HALF_UP))
                .batteryPercentage(BigDecimal.valueOf(battery).setScale(1, RoundingMode.HALF_UP))
                .gpsSatellites(satellites)
                .flightMode(flightMode)
                .armed(armed)
                .status("OK")
                .metadata(Map.of("simulator", true, "engineTempC", 42.5))
                .build();
    }

    public String getDroneId() {
        return droneId;
    }
}
