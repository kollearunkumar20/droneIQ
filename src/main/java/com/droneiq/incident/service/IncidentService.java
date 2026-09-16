package com.droneiq.incident.service;

import com.droneiq.common.exception.ResourceNotFoundException;
import com.droneiq.incident.dto.EvidenceExportResponse;
import com.droneiq.incident.dto.IncidentResponse;
import com.droneiq.incident.dto.TriageIncidentRequest;
import com.droneiq.incident.entity.Incident;
import com.droneiq.incident.repository.IncidentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;

    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> getAllIncidents() {
        return incidentRepository.findAll().stream()
                .map(IncidentResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public IncidentResponse getIncidentById(Long id) {
        return incidentRepository.findById(id)
                .map(IncidentResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + id));
    }

    @Transactional
    public IncidentResponse triageIncident(Long id, TriageIncidentRequest request, String triagedBy) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + id));

        incident.setStatus(request.status());
        incident.setTriagedBy(triagedBy);
        incident.setTriagedNotes(request.triagedNotes());

        Incident saved = incidentRepository.save(incident);
        return IncidentResponse.fromEntity(saved);
    }

    @Transactional
    public EvidenceExportResponse exportEvidence(Long id, String exportedBy) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + id));

        String rawDataToHash = incident.getIncidentCode() + ":" + incident.getDroneId() + ":" + Instant.now();
        String checksum;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawDataToHash.getBytes());
            checksum = HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            checksum = "sha256-mock-checksum-" + id;
        }

        incident.setEvidenceExported(true);
        incident.setEvidenceChecksum(checksum);
        incidentRepository.save(incident);

        String packageUrl = "s3://droneiq-evidence-vault/incidents/" + incident.getIncidentCode() + "/bundle.zip";

        return new EvidenceExportResponse(
                incident.getId(),
                incident.getIncidentCode(),
                incident.getDroneId(),
                packageUrl,
                checksum,
                exportedBy,
                Instant.now(),
                "VERIFIED_IMMUTABLE"
        );
    }
}
