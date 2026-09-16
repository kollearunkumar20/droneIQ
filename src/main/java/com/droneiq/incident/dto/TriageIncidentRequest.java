package com.droneiq.incident.dto;

import jakarta.validation.constraints.NotBlank;

public record TriageIncidentRequest(
        @NotBlank(message = "Status is required (e.g. ACKNOWLEDGED, UNDER_INVESTIGATION, RESOLVED, DISMISSED)")
        String status,

        String triagedNotes
) {
}
