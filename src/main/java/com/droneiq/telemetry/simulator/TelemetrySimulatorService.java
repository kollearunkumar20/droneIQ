package com.droneiq.telemetry.simulator;

import com.droneiq.telemetry.dto.TelemetryMessage;
import com.droneiq.telemetry.kafka.TelemetryKafkaProducer;
import com.droneiq.telemetry.service.TelemetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(name = "simulator.enabled", havingValue = "true", matchIfMissing = true)
public class TelemetrySimulatorService {

    private static final Logger log = LoggerFactory.getLogger(TelemetrySimulatorService.class);

    private final TelemetryKafkaProducer kafkaProducer;
    private final TelemetryService telemetryService;
    private final List<DroneFlightState> activeDrones = new ArrayList<>();

    public TelemetrySimulatorService(
            TelemetryKafkaProducer kafkaProducer,
            TelemetryService telemetryService) {
        this.kafkaProducer = kafkaProducer;
        this.telemetryService = telemetryService;
        initSimulatedDrones();
    }

    private void initSimulatedDrones() {
        // San Francisco Bay Area coordinates (Moffett Field / Silicon Valley airspace)
        activeDrones.add(new DroneFlightState("DRONE-001", 37.4138, -122.0549, 1200.0, 0.04, 75.0));
        activeDrones.add(new DroneFlightState("DRONE-002", 37.4220, -122.0841, 800.0, -0.03, 110.0));
        activeDrones.add(new DroneFlightState("DRONE-003", 37.4010, -122.0310, 1500.0, 0.02, 50.0));

        log.info("Initialized {} simulated drones for mock telemetry generation", activeDrones.size());
    }

    @Scheduled(fixedDelayString = "${simulator.interval-ms:1000}")
    public void generateAndBroadcastTelemetry() {
        for (DroneFlightState droneState : activeDrones) {
            try {
                droneState.tick();
                TelemetryMessage message = droneState.toTelemetryMessage();

                // Publish to Kafka pipeline
                try {
                    kafkaProducer.sendTelemetry(message);
                } catch (Exception kafkaEx) {
                    // Fallback to direct telemetry processing if Kafka broker is offline during local dev
                    log.debug("Kafka dispatch unavailable, routing simulated telemetry directly to service pipeline: {}",
                            kafkaEx.getMessage());
                    telemetryService.processTelemetry(message);
                }
            } catch (Exception e) {
                log.error("Error generating telemetry tick for drone {}: {}", droneState.getDroneId(), e.getMessage());
            }
        }
    }

    public List<DroneFlightState> getActiveDrones() {
        return activeDrones;
    }
}
