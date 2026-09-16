package com.droneiq.incident.dto;

import com.droneiq.incident.entity.Incident;

import java.time.Instant;

public record IncidentResponse(
        Long id,
        String incidentCode,
        String droneId,
        String title,
        String severity,
        String status,
        String details,
        String triagedBy,
        String triagedNotes,
        boolean evidenceExported,
        String evidenceChecksum,
        Instant createdAt,
        Instant updatedAt
) {
    public static IncidentResponse fromEntity(Incident incident) {
        return new IncidentResponse(
                incident.getId(),
                incident.getIncidentCode(),
                incident.getDroneId(),
                incident.getTitle(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getDetails(),
                incident.getTriagedBy(),
                incident.getTriagedNotes(),
                incident.isEvidenceExported(),
                incident.getEvidenceChecksum(),
                incident.getCreatedAt(),
                incident.getUpdatedAt()
        );
    }
}
