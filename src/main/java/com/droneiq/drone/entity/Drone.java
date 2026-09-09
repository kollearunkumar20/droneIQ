package com.droneiq.drone.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "drones")
public class Drone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "drone_id", nullable = false, unique = true, length = 50)
    private String droneId;

    @Column(nullable = false, length = 100)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DroneStatus status = DroneStatus.OFFLINE;

    @Column(name = "firmware_version", length = 50)
    private String firmwareVersion;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Drone() {
    }

    public Drone(Long id, String droneId, String model, DroneStatus status, String firmwareVersion, String ipAddress, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.droneId = droneId;
        this.model = model;
        this.status = status != null ? status : DroneStatus.OFFLINE;
        this.firmwareVersion = firmwareVersion;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getDroneId() {
        return droneId;
    }

    public void setDroneId(String droneId) {
        this.droneId = droneId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public DroneStatus getStatus() {
        return status;
    }

    public void setStatus(DroneStatus status) {
        this.status = status;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
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

    public static DroneBuilder builder() {
        return new DroneBuilder();
    }

    public static class DroneBuilder {
        private Long id;
        private String droneId;
        private String model;
        private DroneStatus status = DroneStatus.OFFLINE;
        private String firmwareVersion;
        private String ipAddress;
        private Instant createdAt;
        private Instant updatedAt;

        public DroneBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public DroneBuilder droneId(String droneId) {
            this.droneId = droneId;
            return this;
        }

        public DroneBuilder model(String model) {
            this.model = model;
            return this;
        }

        public DroneBuilder status(DroneStatus status) {
            this.status = status;
            return this;
        }

        public DroneBuilder firmwareVersion(String firmwareVersion) {
            this.firmwareVersion = firmwareVersion;
            return this;
        }

        public DroneBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public DroneBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public DroneBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Drone build() {
            return new Drone(id, droneId, model, status, firmwareVersion, ipAddress, createdAt, updatedAt);
        }
    }
}
