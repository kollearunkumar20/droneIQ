package com.droneiq.drone.controller;

import com.droneiq.common.dto.ApiResponse;
import com.droneiq.drone.dto.CreateDroneRequest;
import com.droneiq.drone.dto.DroneResponse;
import com.droneiq.drone.dto.UpdateDroneRequest;
import com.droneiq.drone.entity.DroneStatus;
import com.droneiq.drone.service.DroneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/drones")
@Tag(name = "Drones", description = "Drone Fleet Management Endpoints")
public class DroneController {

    private final DroneService droneService;

    public DroneController(DroneService droneService) {
        this.droneService = droneService;
    }

    @GetMapping
    @Operation(summary = "Get all registered drones with optional status filter")
    public ResponseEntity<ApiResponse<List<DroneResponse>>> getAllDrones(
            @RequestParam(required = false) DroneStatus status) {
        List<DroneResponse> drones = droneService.getAllDrones(status);
        return ResponseEntity.ok(ApiResponse.success(drones));
    }

    @GetMapping("/{droneId}")
    @Operation(summary = "Get drone registration details by Drone ID")
    public ResponseEntity<ApiResponse<DroneResponse>> getDroneById(@PathVariable String droneId) {
        DroneResponse drone = droneService.getDroneById(droneId);
        return ResponseEntity.ok(ApiResponse.success(drone));
    }

    @PostMapping
    @Operation(summary = "Register a new drone in the fleet")
    public ResponseEntity<ApiResponse<DroneResponse>> createDrone(@Valid @RequestBody CreateDroneRequest request) {
        DroneResponse drone = droneService.createDrone(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Drone registered successfully", drone));
    }

    @PutMapping("/{droneId}")
    @Operation(summary = "Update drone details or operational status")
    public ResponseEntity<ApiResponse<DroneResponse>> updateDrone(
            @PathVariable String droneId,
            @Valid @RequestBody UpdateDroneRequest request) {
        DroneResponse drone = droneService.updateDrone(droneId, request);
        return ResponseEntity.ok(ApiResponse.success("Drone updated successfully", drone));
    }

    @DeleteMapping("/{droneId}")
    @Operation(summary = "Deregister and delete drone from fleet")
    public ResponseEntity<ApiResponse<Void>> deleteDrone(@PathVariable String droneId) {
        droneService.deleteDrone(droneId);
        return ResponseEntity.ok(ApiResponse.success("Drone deleted successfully", null));
    }
}
