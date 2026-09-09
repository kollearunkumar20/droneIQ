package com.droneiq.drone.service;

import com.droneiq.common.exception.DuplicateResourceException;
import com.droneiq.common.exception.ResourceNotFoundException;
import com.droneiq.drone.dto.CreateDroneRequest;
import com.droneiq.drone.dto.DroneResponse;
import com.droneiq.drone.dto.UpdateDroneRequest;
import com.droneiq.drone.entity.Drone;
import com.droneiq.drone.entity.DroneStatus;
import com.droneiq.drone.repository.DroneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DroneService {

    private static final Logger log = LoggerFactory.getLogger(DroneService.class);

    private final DroneRepository droneRepository;

    public DroneService(DroneRepository droneRepository) {
        this.droneRepository = droneRepository;
    }

    @Transactional(readOnly = true)
    public List<DroneResponse> getAllDrones(DroneStatus status) {
        List<Drone> drones = (status != null)
                ? droneRepository.findByStatus(status)
                : droneRepository.findAll();

        return drones.stream()
                .map(DroneResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DroneResponse getDroneById(String droneId) {
        Drone drone = findDroneEntity(droneId);
        return DroneResponse.fromEntity(drone);
    }

    @Transactional
    public DroneResponse createDrone(CreateDroneRequest request) {
        if (droneRepository.existsByDroneId(request.droneId())) {
            throw new DuplicateResourceException("Drone already exists with ID: " + request.droneId());
        }

        Drone drone = Drone.builder()
                .droneId(request.droneId())
                .model(request.model())
                .status(DroneStatus.OFFLINE)
                .firmwareVersion(request.firmwareVersion())
                .ipAddress(request.ipAddress())
                .build();

        Drone savedDrone = droneRepository.save(drone);
        log.info("Created new drone registration: {}", savedDrone.getDroneId());
        return DroneResponse.fromEntity(savedDrone);
    }

    @Transactional
    public DroneResponse updateDrone(String droneId, UpdateDroneRequest request) {
        Drone drone = findDroneEntity(droneId);

        if (request.model() != null && !request.model().isBlank()) {
            drone.setModel(request.model());
        }
        if (request.status() != null) {
            drone.setStatus(request.status());
        }
        if (request.firmwareVersion() != null) {
            drone.setFirmwareVersion(request.firmwareVersion());
        }
        if (request.ipAddress() != null) {
            drone.setIpAddress(request.ipAddress());
        }

        Drone updatedDrone = droneRepository.save(drone);
        log.info("Updated drone registration: {}", droneId);
        return DroneResponse.fromEntity(updatedDrone);
    }

    @Transactional
    public void deleteDrone(String droneId) {
        Drone drone = findDroneEntity(droneId);
        droneRepository.delete(drone);
        log.info("Deleted drone registration: {}", droneId);
    }

    @Transactional
    public void updateDroneStatus(String droneId, DroneStatus status) {
        droneRepository.findByDroneId(droneId).ifPresent(drone -> {
            drone.setStatus(status);
            droneRepository.save(drone);
            log.debug("Updated status for drone {} to {}", droneId, status);
        });
    }

    public Drone findDroneEntity(String droneId) {
        return droneRepository.findByDroneId(droneId)
                .orElseThrow(() -> new ResourceNotFoundException("Drone not found with ID: " + droneId));
    }
}
