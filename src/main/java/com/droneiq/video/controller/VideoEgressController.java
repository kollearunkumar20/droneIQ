package com.droneiq.video.controller;

import com.droneiq.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/video")
@Tag(name = "Video Egress & HUD", description = "Live Video Streams & Telemetry Egress HUD Overlay")
@SecurityRequirement(name = "BearerAuth")
public class VideoEgressController {

    @GetMapping("/streams")
    @PreAuthorize("hasAuthority('TELEMETRY:VIEW')")
    @Operation(summary = "List all active video egress feeds (All Roles)")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getVideoStreams() {
        List<Map<String, Object>> streams = List.of(
                Map.of("droneId", "DRONE-001", "protocol", "WebRTC", "streamUrl", "webrtc://gcs.droneiq.io/live/drone-001", "status", "ONLINE"),
                Map.of("droneId", "DRONE-002", "protocol", "RTSP", "streamUrl", "rtsp://gcs.droneiq.io:8554/drone-002", "status", "ONLINE"),
                Map.of("droneId", "DRONE-003", "protocol", "WebRTC", "streamUrl", "webrtc://gcs.droneiq.io/live/drone-003", "status", "STANDBY")
        );
        return ResponseEntity.ok(ApiResponse.success(streams));
    }

    @GetMapping("/streams/{droneId}/hud")
    @PreAuthorize("hasAuthority('TELEMETRY:VIEW')")
    @Operation(summary = "Get Heads-Up Display (HUD) overlay parameters for video egress (All Roles)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHudOverlay(@PathVariable String droneId) {
        Map<String, Object> hudConfig = Map.of(
                "droneId", droneId,
                "artificialHorizon", true,
                "compassRose", true,
                "altitudeTape", true,
                "speedTape", true,
                "batteryGauge", true,
                "fps", 30,
                "resolution", "1920x1080"
        );
        return ResponseEntity.ok(ApiResponse.success(hudConfig));
    }

    @PostMapping("/streams/{droneId}/control")
    @PreAuthorize("hasAuthority('TELEMETRY:FULL')")
    @Operation(summary = "Modify video egress bitrate/stream parameters (Super Admin, Fleet Manager, Flight Operator)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> controlVideoStream(
            @PathVariable String droneId,
            @RequestBody Map<String, Object> controlParams) {
        return ResponseEntity.ok(ApiResponse.success("Video egress configuration applied", Map.of(
                "droneId", droneId,
                "appliedSettings", controlParams,
                "status", "UPDATED"
        )));
    }
}
