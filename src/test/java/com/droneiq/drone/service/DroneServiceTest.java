package com.droneiq.drone.service;

import com.droneiq.common.exception.DuplicateResourceException;
import com.droneiq.common.exception.ResourceNotFoundException;
import com.droneiq.drone.dto.CreateDroneRequest;
import com.droneiq.drone.dto.DroneResponse;
import com.droneiq.drone.dto.UpdateDroneRequest;
import com.droneiq.drone.entity.Drone;
import com.droneiq.drone.entity.DroneStatus;
import com.droneiq.drone.repository.DroneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DroneServiceTest {

    private DroneRepository droneRepository;
    private DroneService droneService;

    @BeforeEach
    void setUp() {
        droneRepository = mock(DroneRepository.class);
        droneService = new DroneService(droneRepository);
    }

    private Drone sampleDrone() {
        return Drone.builder()
                .id(1L)
                .droneId("DRONE-001")
                .model("Quadcopter Alpha")
                .status(DroneStatus.OFFLINE)
                .firmwareVersion("v1.0.0")
                .ipAddress("192.168.1.100")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Should create drone when droneId is unique")
    void testCreateDroneSuccess() {
        when(droneRepository.existsByDroneId("DRONE-001")).thenReturn(false);
        when(droneRepository.save(any(Drone.class))).thenAnswer(i -> i.getArgument(0));

        CreateDroneRequest request = new CreateDroneRequest("DRONE-001", "Quadcopter Alpha", "v1.0.0", "192.168.1.100");
        DroneResponse response = droneService.createDrone(request);

        assertNotNull(response);
        assertEquals("DRONE-001", response.droneId());
        assertEquals("Quadcopter Alpha", response.model());
        assertEquals(DroneStatus.OFFLINE, response.status());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when creating existing droneId")
    void testCreateDroneDuplicate() {
        when(droneRepository.existsByDroneId("DRONE-001")).thenReturn(true);

        CreateDroneRequest request = new CreateDroneRequest("DRONE-001", "Quadcopter Alpha", "v1.0.0", "192.168.1.100");
        assertThrows(DuplicateResourceException.class, () -> droneService.createDrone(request));
    }

    @Test
    @DisplayName("Should get drone by ID when exists")
    void testGetDroneByIdSuccess() {
        when(droneRepository.findByDroneId("DRONE-001")).thenReturn(Optional.of(sampleDrone()));

        DroneResponse response = droneService.getDroneById("DRONE-001");
        assertEquals("DRONE-001", response.droneId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when drone does not exist")
    void testGetDroneByIdNotFound() {
        when(droneRepository.findByDroneId("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> droneService.getDroneById("UNKNOWN"));
    }

    @Test
    @DisplayName("Should update drone status and model")
    void testUpdateDrone() {
        Drone drone = sampleDrone();
        when(droneRepository.findByDroneId("DRONE-001")).thenReturn(Optional.of(drone));
        when(droneRepository.save(any(Drone.class))).thenAnswer(i -> i.getArgument(0));

        UpdateDroneRequest request = new UpdateDroneRequest("Updated Model", DroneStatus.IN_FLIGHT, "v1.1.0", "192.168.1.200");
        DroneResponse response = droneService.updateDrone("DRONE-001", request);

        assertEquals("Updated Model", response.model());
        assertEquals(DroneStatus.IN_FLIGHT, response.status());
    }

    @Test
    @DisplayName("Should delete drone when exists")
    void testDeleteDrone() {
        Drone drone = sampleDrone();
        when(droneRepository.findByDroneId("DRONE-001")).thenReturn(Optional.of(drone));

        droneService.deleteDrone("DRONE-001");
        verify(droneRepository).delete(drone);
    }

    @Test
    @DisplayName("Should filter drones by status")
    void testGetDronesWithStatusFilter() {
        when(droneRepository.findByStatus(DroneStatus.IN_FLIGHT)).thenReturn(List.of(sampleDrone()));

        List<DroneResponse> result = droneService.getAllDrones(DroneStatus.IN_FLIGHT);
        assertEquals(1, result.size());
        verify(droneRepository).findByStatus(DroneStatus.IN_FLIGHT);
    }
}
