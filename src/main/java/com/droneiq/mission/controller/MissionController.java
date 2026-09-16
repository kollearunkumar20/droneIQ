package com.droneiq.mission.controller;

import com.droneiq.common.dto.ApiResponse;
import com.droneiq.mission.dto.CreateMissionRequest;
import com.droneiq.mission.dto.MissionResponse;
import com.droneiq.mission.service.MissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
@Tag(name = "Mission Planning", description = "Mission Planning & Waypoint Editing Endpoints")
@SecurityRequirement(name = "BearerAuth")
public class MissionController {

    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MISSION:VIEW')")
    @Operation(summary = "Get all mission flight plans (All Roles)")
    public ResponseEntity<ApiResponse<List<MissionResponse>>> getAllMissions() {
        List<MissionResponse> missions = missionService.getAllMissions();
        return ResponseEntity.ok(ApiResponse.success(missions));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MISSION:VIEW')")
    @Operation(summary = "Get mission flight plan by ID (All Roles)")
    public ResponseEntity<ApiResponse<MissionResponse>> getMissionById(@PathVariable Long id) {
        MissionResponse mission = missionService.getMissionById(id);
        return ResponseEntity.ok(ApiResponse.success(mission));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MISSION:WRITE')")
    @Operation(summary = "Create & Save a new mission flight plan (Super Admin, Fleet Manager, Flight Operator)")
    public ResponseEntity<ApiResponse<MissionResponse>> createMission(
            @Valid @RequestBody CreateMissionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        MissionResponse response = missionService.createMission(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Mission plan created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MISSION:WRITE')")
    @Operation(summary = "Update mission plan and waypoints (Super Admin, Fleet Manager, Flight Operator)")
    public ResponseEntity<ApiResponse<MissionResponse>> updateMission(
            @PathVariable Long id,
            @Valid @RequestBody CreateMissionRequest request) {
        MissionResponse response = missionService.updateMission(id, request);
        return ResponseEntity.ok(ApiResponse.success("Mission plan updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MISSION:FULL')")
    @Operation(summary = "Delete mission plan (Super Admin & Fleet Manager only)")
    public ResponseEntity<ApiResponse<Void>> deleteMission(@PathVariable Long id) {
        missionService.deleteMission(id);
        return ResponseEntity.ok(ApiResponse.success("Mission plan deleted successfully", null));
    }
}
