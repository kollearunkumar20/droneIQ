package com.droneiq.telemetry.kafka;

import com.droneiq.telemetry.dto.TelemetryMessage;
import com.droneiq.telemetry.service.TelemetryService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TelemetryKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(TelemetryKafkaConsumer.class);

    private final TelemetryService telemetryService;

    public TelemetryKafkaConsumer(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @KafkaListener(
            topics = "${app.kafka.telemetry-topic:drone.telemetry}",
            containerFactory = "telemetryKafkaListenerContainerFactory",
            groupId = "${spring.kafka.consumer.group-id:droneiq-telemetry-service}"
    )
    public void consumeTelemetry(ConsumerRecord<String, TelemetryMessage> record) {
        TelemetryMessage message = record.value();
        if (message == null) {
            log.warn("Received empty or corrupt telemetry record from Kafka [partition={}, offset={}]",
                    record.partition(), record.offset());
            return;
        }

        log.debug("Consumed telemetry from Kafka [topic={}, key={}, offset={}]: lat={}, lon={}",
                record.topic(), record.key(), record.offset(), message.latitude(), message.longitude());

        try {
            telemetryService.processTelemetry(message);
        } catch (Exception e) {
            log.error("Error processing telemetry record for drone {}: {}",
                    message.droneId(), e.getMessage(), e);
        }
    }
}
