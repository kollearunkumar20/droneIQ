package com.droneiq.incident.dto;

import java.time.Instant;

public record EvidenceExportResponse(
        Long incidentId,
        String incidentCode,
        String droneId,
        String evidencePackageUrl,
        String sha256Checksum,
        String exportedBy,
        Instant exportedAt,
        String verificationStatus
) {
}
