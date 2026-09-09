package com.droneiq.telemetry.storage;

import com.droneiq.telemetry.dto.TelemetryMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class S3TelemetryStorageServiceTest {

    private S3Client s3Client;
    private S3TelemetryStorageService storageService;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Batch size = 2 for easy testing
        storageService = new S3TelemetryStorageService(s3Client, objectMapper, "test-bucket", 2);
    }

    private TelemetryMessage createMessage(String droneId) {
        return TelemetryMessage.builder()
                .droneId(droneId)
                .timestamp(Instant.parse("2026-09-09T10:00:00Z"))
                .latitude(BigDecimal.valueOf(37.7749))
                .longitude(BigDecimal.valueOf(-122.4194))
                .altitude(BigDecimal.valueOf(100.0))
                .heading(BigDecimal.valueOf(180.0))
                .speed(BigDecimal.valueOf(15.0))
                .batteryPercentage(BigDecimal.valueOf(95.0))
                .flightMode("AUTO")
                .armed(true)
                .build();
    }

    @Test
    @DisplayName("Should buffer messages and automatically flush when batch size reached")
    void testAutoFlushOnBatchSize() {
        storageService.storeTelemetry(createMessage("DRONE-001"));
        assertEquals(1, storageService.getBufferSize());

        // Second message triggers flush (batch size = 2)
        storageService.storeTelemetry(createMessage("DRONE-001"));

        assertEquals(0, storageService.getBufferSize());
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("Should format S3 partition key with Hive-style partitioning")
    void testPartitionKeyFormat() {
        storageService.storeTelemetry(createMessage("DRONE-001"));
        storageService.flush();

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

        PutObjectRequest putRequest = requestCaptor.getValue();
        assertEquals("test-bucket", putRequest.bucket());
        assertTrue(putRequest.key().startsWith("telemetry/year=2026/month=09/day=09/drone_id=DRONE-001/"));
        assertTrue(putRequest.key().endsWith(".jsonl"));
    }
}
