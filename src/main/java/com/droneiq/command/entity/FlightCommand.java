package com.droneiq.command.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "flight_commands")
public class FlightCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "drone_id", nullable = false, length = 50)
    private String droneId;

    @Column(name = "command_type", nullable = false, length = 50)
    private String commandType;

    @Column(name = "is_override", nullable = false)
    private boolean isOverride = false;

    @Column(name = "executed_by", nullable = false, length = 50)
    private String executedBy;

    @Column(nullable = false, length = 30)
    private String status = "EXECUTED";

    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson;

    @Column(name = "executed_at", nullable = false, updatable = false)
    private Instant executedAt;

    public FlightCommand() {
    }

    public FlightCommand(String droneId, String commandType, boolean isOverride, String executedBy, String status, String payloadJson) {
        this.droneId = droneId;
        this.commandType = commandType;
        this.isOverride = isOverride;
        this.executedBy = executedBy;
        this.status = status;
        this.payloadJson = payloadJson;
    }

    @PrePersist
    protected void onCreate() {
        this.executedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDroneId() {
        return droneId;
    }

    public void setDroneId(String droneId) {
        this.droneId = droneId;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public boolean isOverride() {
        return isOverride;
    }

    public void setOverride(boolean override) {
        isOverride = override;
    }

    public String getExecutedBy() {
        return executedBy;
    }

    public void setExecutedBy(String executedBy) {
        this.executedBy = executedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(Instant executedAt) {
        this.executedAt = executedAt;
    }
}
