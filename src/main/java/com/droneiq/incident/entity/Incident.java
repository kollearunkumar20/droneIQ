package com.droneiq.incident.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "incident_code", nullable = false, unique = true, length = 50)
    private String incidentCode;

    @Column(name = "drone_id", nullable = false, length = 50)
    private String droneId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 30)
    private String severity = "MEDIUM";

    @Column(nullable = false, length = 30)
    private String status = "ACTIVE";

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "triaged_by", length = 50)
    private String triagedBy;

    @Column(name = "triaged_notes", columnDefinition = "TEXT")
    private String triagedNotes;

    @Column(name = "evidence_exported", nullable = false)
    private boolean evidenceExported = false;

    @Column(name = "evidence_checksum", length = 100)
    private String evidenceChecksum;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Incident() {
    }

    public Incident(String incidentCode, String droneId, String title, String severity, String status, String details) {
        this.incidentCode = incidentCode;
        this.droneId = droneId;
        this.title = title;
        this.severity = severity != null ? severity : "MEDIUM";
        this.status = status != null ? status : "ACTIVE";
        this.details = details;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIncidentCode() {
        return incidentCode;
    }

    public void setIncidentCode(String incidentCode) {
        this.incidentCode = incidentCode;
    }

    public String getDroneId() {
        return droneId;
    }

    public void setDroneId(String droneId) {
        this.droneId = droneId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getTriagedBy() {
        return triagedBy;
    }

    public void setTriagedBy(String triagedBy) {
        this.triagedBy = triagedBy;
    }

    public String getTriagedNotes() {
        return triagedNotes;
    }

    public void setTriagedNotes(String triagedNotes) {
        this.triagedNotes = triagedNotes;
    }

    public boolean isEvidenceExported() {
        return evidenceExported;
    }

    public void setEvidenceExported(boolean evidenceExported) {
        this.evidenceExported = evidenceExported;
    }

    public String getEvidenceChecksum() {
        return evidenceChecksum;
    }

    public void setEvidenceChecksum(String evidenceChecksum) {
        this.evidenceChecksum = evidenceChecksum;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
