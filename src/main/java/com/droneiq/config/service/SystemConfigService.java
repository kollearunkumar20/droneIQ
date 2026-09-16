package com.droneiq.config.service;

import com.droneiq.common.exception.ResourceNotFoundException;
import com.droneiq.config.dto.SystemConfigDto;
import com.droneiq.config.entity.SystemConfig;
import com.droneiq.config.repository.SystemConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SystemConfigService {

    private final SystemConfigRepository configRepository;

    public SystemConfigService(SystemConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Transactional(readOnly = true)
    public List<SystemConfigDto> getAllConfigs() {
        return configRepository.findAll().stream()
                .map(SystemConfigDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public SystemConfigDto getConfigByKey(String key) {
        return configRepository.findByConfigKey(key)
                .map(SystemConfigDto::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("System configuration key not found: " + key));
    }

    @Transactional
    public SystemConfigDto setConfig(String key, String value, String description, String updatedBy) {
        SystemConfig config = configRepository.findByConfigKey(key)
                .orElse(new SystemConfig(key, value, description, updatedBy));

        config.setConfigValue(value);
        if (description != null) {
            config.setDescription(description);
        }
        config.setUpdatedBy(updatedBy);

        SystemConfig saved = configRepository.save(config);
        return SystemConfigDto.fromEntity(saved);
    }
}
