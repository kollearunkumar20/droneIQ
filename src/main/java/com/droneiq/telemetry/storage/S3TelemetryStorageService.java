package com.droneiq.telemetry.storage;

import com.droneiq.telemetry.dto.TelemetryMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "storage.s3.enabled", havingValue = "true")
public class S3TelemetryStorageService implements TelemetryStorageService {

    private static final Logger log = LoggerFactory.getLogger(S3TelemetryStorageService.class);

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;
    private final String bucketName;
    private final int batchSize;
    private final ConcurrentLinkedQueue<TelemetryMessage> messageBuffer = new ConcurrentLinkedQueue<>();

    public S3TelemetryStorageService(
            S3Client s3Client,
            ObjectMapper objectMapper,
            @Value("${storage.s3.bucket:droneiq-telemetry-data-lake}") String bucketName,
            @Value("${storage.s3.batch-size:50}") int batchSize) {
        this.s3Client = s3Client;
        this.objectMapper = objectMapper;
        this.bucketName = bucketName;
        this.batchSize = batchSize;
        log.info("S3 Telemetry Storage ENABLED. Archiving to bucket: {}, batch size: {}", bucketName, batchSize);
    }

    @Override
    public void storeTelemetry(TelemetryMessage message) {
        if (message != null) {
            messageBuffer.offer(message);
            if (messageBuffer.size() >= batchSize) {
                flush();
            }
        }
    }

    @Override
    @Scheduled(fixedDelayString = "${storage.s3.flush-interval-ms:10000}")
    public synchronized void flush() {
        if (messageBuffer.isEmpty()) {
            return;
        }

        List<TelemetryMessage> batch = new ArrayList<>();
        TelemetryMessage msg;
        while ((msg = messageBuffer.poll()) != null) {
            batch.add(msg);
        }

        if (batch.isEmpty()) {
            return;
        }

        log.debug("Flushing micro-batch of {} telemetry records to S3 Data Lake", batch.size());

        // Group by partition key: year/month/day/drone_id
        Map<String, List<TelemetryMessage>> partitioned = batch.stream()
                .collect(Collectors.groupingBy(this::buildPartitionPrefix));

        partitioned.forEach(this::uploadPartitionBatch);
    }

    private String buildPartitionPrefix(TelemetryMessage message) {
        ZonedDateTime utc = message.timestamp().atZone(ZoneOffset.UTC);
        int year = utc.getYear();
        String month = String.format("%02d", utc.getMonthValue());
        String day = String.format("%02d", utc.getDayOfMonth());
        String droneId = message.droneId();

        return String.format("telemetry/year=%d/month=%s/day=%s/drone_id=%s", year, month, day, droneId);
    }

    private void uploadPartitionBatch(String partitionPrefix, List<TelemetryMessage> messages) {
        try {
            StringBuilder jsonlBuilder = new StringBuilder();
            for (TelemetryMessage m : messages) {
                jsonlBuilder.append(objectMapper.writeValueAsString(m)).append("\n");
            }

            String fileName = String.format("%s/telemetry_%d_%s.jsonl",
                    partitionPrefix,
                    System.currentTimeMillis(),
                    UUID.randomUUID().toString().substring(0, 8));

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType("application/x-ndjson")
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromString(jsonlBuilder.toString(), StandardCharsets.UTF_8));
            log.info("Archived {} telemetry records to s3://{}/{}", messages.size(), bucketName, fileName);
        } catch (Exception e) {
            log.error("Failed to archive telemetry batch to S3 [partition={}]: {}", partitionPrefix, e.getMessage(), e);
            // Re-queue to avoid silent data loss on transient network issues
            messageBuffer.addAll(messages);
        }
    }

    public int getBufferSize() {
        return messageBuffer.size();
    }
}
