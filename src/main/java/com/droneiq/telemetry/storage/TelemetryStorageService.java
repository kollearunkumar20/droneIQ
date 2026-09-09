package com.droneiq.telemetry.storage;

import com.droneiq.telemetry.dto.TelemetryMessage;

public interface TelemetryStorageService {

    /**
     * Accepts a telemetry message for archival persistence into the Data Lake.
     * Implementations must be non-blocking or asynchronous so slow storage
     * never impacts live telemetry streaming.
     */
    void storeTelemetry(TelemetryMessage message);

    /**
     * Manually triggers flush of any pending micro-batches.
     */
    void flush();
}
