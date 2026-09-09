package com.droneiq.telemetry.controller;

import com.droneiq.common.dto.ApiResponse;
import com.droneiq.common.exception.ResourceNotFoundException;
import com.droneiq.telemetry.dto.TelemetryMessage;
import com.droneiq.telemetry.kafka.TelemetryKafkaProducer;
import com.droneiq.telemetry.service.CurrentDroneTelemetryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Telemetry", description = "Telemetry Queries and Ingestion Endpoints")
public class TelemetryController {

    private final CurrentDroneTelemetryService currentTelemetryService;
    private final TelemetryKafkaProducer kafkaProducer;

    public TelemetryController(
            CurrentDroneTelemetryService currentTelemetryService,
            TelemetryKafkaProducer kafkaProducer) {
        this.currentTelemetryService = currentTelemetryService;
        this.kafkaProducer = kafkaProducer;
    }

    @GetMapping("/drones/{droneId}/telemetry/latest")
    @Operation(summary = "Get the latest real-time telemetry state for a specific drone")
    public ResponseEntity<ApiResponse<TelemetryMessage>> getLatestDroneTelemetry(@PathVariable String droneId) {
        TelemetryMessage telemetry = currentTelemetryService.getLatestTelemetry(droneId)
                .orElseThrow(() -> new ResourceNotFoundException("No telemetry available for drone ID: " + droneId));

        return ResponseEntity.ok(ApiResponse.success(telemetry));
    }

    @GetMapping("/telemetry/latest")
    @Operation(summary = "Get the latest telemetry state for all active drones")
    public ResponseEntity<ApiResponse<Map<String, TelemetryMessage>>> getAllLatestTelemetry() {
        Map<String, TelemetryMessage> allTelemetry = currentTelemetryService.getAllLatestTelemetry();
        return ResponseEntity.ok(ApiResponse.success(allTelemetry));
    }

    @PostMapping("/telemetry/publish")
    @Operation(summary = "Publish a telemetry message directly to Kafka (for MAVLink gateway or testing)")
    public ResponseEntity<ApiResponse<String>> publishTelemetry(@Valid @RequestBody TelemetryMessage message) {
        kafkaProducer.sendTelemetry(message);
        return ResponseEntity.ok(ApiResponse.success("Telemetry published to Kafka pipeline successfully", message.droneId()));
    }
}
