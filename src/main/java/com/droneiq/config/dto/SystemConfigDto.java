package com.droneiq.config.dto;

import com.droneiq.config.entity.SystemConfig;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record SystemConfigDto(
        @NotBlank(message = "Config key is required")
        String configKey,

        @NotBlank(message = "Config value is required")
        String configValue,

        String description,
        String updatedBy,
        Instant updatedAt
) {
    public static SystemConfigDto fromEntity(SystemConfig entity) {
        return new SystemConfigDto(
                entity.getConfigKey(),
                entity.getConfigValue(),
                entity.getDescription(),
                entity.getUpdatedBy(),
                entity.getUpdatedAt()
        );
    }
}
