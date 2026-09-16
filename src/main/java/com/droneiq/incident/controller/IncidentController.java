package com.droneiq.incident.controller;

import com.droneiq.common.dto.ApiResponse;
import com.droneiq.incident.dto.EvidenceExportResponse;
import com.droneiq.incident.dto.IncidentResponse;
import com.droneiq.incident.dto.TriageIncidentRequest;
import com.droneiq.incident.service.IncidentService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@Tag(name = "AI Incidents & Evidence", description = "AI Incident Triage & Evidence Export Endpoints")
@SecurityRequirement(name = "BearerAuth")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INCIDENT:VIEW')")
    @Operation(summary = "Get all AI incident reports (All Roles including Viewer/Auditor)")
    public ResponseEntity<ApiResponse<List<IncidentResponse>>> getAllIncidents() {
        List<IncidentResponse> incidents = incidentService.getAllIncidents();
        return ResponseEntity.ok(ApiResponse.success(incidents));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INCIDENT:VIEW')")
    @Operation(summary = "Get incident report details by ID (All Roles)")
    public ResponseEntity<ApiResponse<IncidentResponse>> getIncidentById(@PathVariable Long id) {
        IncidentResponse incident = incidentService.getIncidentById(id);
        return ResponseEntity.ok(ApiResponse.success(incident));
    }

    @PostMapping("/{id}/triage")
    @PreAuthorize("hasAuthority('INCIDENT:TRIAGE')")
    @Operation(summary = "Triage and update alert state (Super Admin, Fleet Manager, Flight Operator)")
    public ResponseEntity<ApiResponse<IncidentResponse>> triageIncident(
            @PathVariable Long id,
            @Valid @RequestBody TriageIncidentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        IncidentResponse response = incidentService.triageIncident(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Incident triaged successfully", response));
    }

    @PostMapping("/{id}/export")
    @PreAuthorize("hasAuthority('INCIDENT:FULL')")
    @Operation(summary = "Export cryptographic evidence package (Super Admin & Fleet Manager only)")
    public ResponseEntity<ApiResponse<EvidenceExportResponse>> exportEvidence(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        EvidenceExportResponse response = incidentService.exportEvidence(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Evidence package exported and verified", response));
    }
}
