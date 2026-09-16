package com.droneiq.command.repository;

import com.droneiq.command.entity.FlightCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlightCommandRepository extends JpaRepository<FlightCommand, Long> {
    List<FlightCommand> findByDroneIdOrderByExecutedAtDesc(String droneId);
}
