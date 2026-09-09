package com.droneiq.drone.repository;

import com.droneiq.drone.entity.Drone;
import com.droneiq.drone.entity.DroneStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DroneRepository extends JpaRepository<Drone, Long> {
    Optional<Drone> findByDroneId(String droneId);
    boolean existsByDroneId(String droneId);
    List<Drone> findByStatus(DroneStatus status);
    void deleteByDroneId(String droneId);
}
