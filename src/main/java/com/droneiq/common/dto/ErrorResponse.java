package com.droneiq.common.dto;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
    int status,
    String error,
    String message,
    String path,
    Instant timestamp,
    List<String> details
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message, path, Instant.now(), List.of());
    }

    public ErrorResponse(int status, String error, String message, String path, List<String> details) {
        this(status, error, message, path, Instant.now(), details != null ? details : List.of());
    }
}
