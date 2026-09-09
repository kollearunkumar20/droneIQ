package com.droneiq.telemetry.kafka;

import com.droneiq.telemetry.dto.TelemetryMessage;
import com.droneiq.telemetry.validation.TelemetryValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class TelemetryKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(TelemetryKafkaProducer.class);

    private final KafkaTemplate<String, TelemetryMessage> kafkaTemplate;
    private final TelemetryValidator validator;
    private final String topic;

    public TelemetryKafkaProducer(
            KafkaTemplate<String, TelemetryMessage> kafkaTemplate,
            TelemetryValidator validator,
            @Value("${app.kafka.telemetry-topic:drone.telemetry}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.validator = validator;
        this.topic = topic;
    }

    public CompletableFuture<SendResult<String, TelemetryMessage>> sendTelemetry(TelemetryMessage message) {
        // Validate schema and boundaries before emitting
        validator.validate(message);

        log.debug("Publishing telemetry to Kafka [topic={}, droneId={}]: lat={}, lon={}, alt={}",
                topic, message.droneId(), message.latitude(), message.longitude(), message.altitude());

        CompletableFuture<SendResult<String, TelemetryMessage>> future =
                kafkaTemplate.send(topic, message.droneId(), message);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish telemetry to Kafka for drone {}: {}",
                        message.droneId(), ex.getMessage());
            } else {
                log.trace("Telemetry published to Kafka offset: {}",
                        result.getRecordMetadata().offset());
            }
        });

        return future;
    }
}
