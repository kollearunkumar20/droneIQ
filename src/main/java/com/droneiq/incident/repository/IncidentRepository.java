package com.droneiq.incident.repository;

import com.droneiq.incident.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    Optional<Incident> findByIncidentCode(String incidentCode);
    List<Incident> findByDroneId(String droneId);
}
