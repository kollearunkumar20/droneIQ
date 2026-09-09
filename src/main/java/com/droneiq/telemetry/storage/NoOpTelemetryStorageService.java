package com.droneiq.telemetry.storage;

import com.droneiq.telemetry.dto.TelemetryMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "storage.s3.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpTelemetryStorageService implements TelemetryStorageService {

    private static final Logger log = LoggerFactory.getLogger(NoOpTelemetryStorageService.class);

    public NoOpTelemetryStorageService() {
        log.info("S3 Telemetry Storage is DISABLED. Operating in local dev mode (NoOpTelemetryStorageService active).");
    }

    @Override
    public void storeTelemetry(TelemetryMessage message) {
        // No-op for local development when S3 is disabled
        log.trace("Local dev mode: Telemetry recorded in memory for drone {} (S3 archival bypassed)",
                message != null ? message.droneId() : "unknown");
    }

    @Override
    public void flush() {
        // No-op
    }
}
