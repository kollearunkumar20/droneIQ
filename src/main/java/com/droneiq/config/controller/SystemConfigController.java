package com.droneiq.config.controller;

import com.droneiq.common.dto.ApiResponse;
import com.droneiq.config.dto.SystemConfigDto;
import com.droneiq.config.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/system/config")
@Tag(name = "System Configuration", description = "RBAC System Config & Operational Settings")
@SecurityRequirement(name = "BearerAuth")
public class SystemConfigController {

    private final SystemConfigService configService;

    public SystemConfigController(SystemConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SYSTEM_CONFIG:FULL')")
    @Operation(summary = "Get all system configuration parameters (Super Admin only)")
    public ResponseEntity<ApiResponse<List<SystemConfigDto>>> getAllConfigs() {
        List<SystemConfigDto> configs = configService.getAllConfigs();
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @GetMapping("/{key}")
    @PreAuthorize("hasAuthority('SYSTEM_CONFIG:FULL')")
    @Operation(summary = "Get specific system configuration by key (Super Admin only)")
    public ResponseEntity<ApiResponse<SystemConfigDto>> getConfigByKey(@PathVariable String key) {
        SystemConfigDto config = configService.getConfigByKey(key);
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasAuthority('SYSTEM_CONFIG:FULL')")
    @Operation(summary = "Update or set system configuration parameter (Super Admin only)")
    public ResponseEntity<ApiResponse<SystemConfigDto>> updateConfig(
            @PathVariable String key,
            @Valid @RequestBody SystemConfigDto request,
            @AuthenticationPrincipal UserDetails userDetails) {
        SystemConfigDto updated = configService.setConfig(
                key,
                request.configValue(),
                request.description(),
                userDetails.getUsername()
        );
        return ResponseEntity.ok(ApiResponse.success("System configuration updated", updated));
    }
}
