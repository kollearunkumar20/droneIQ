package com.droneiq.mission.service;

import com.droneiq.common.exception.ResourceNotFoundException;
import com.droneiq.mission.dto.CreateMissionRequest;
import com.droneiq.mission.dto.MissionResponse;
import com.droneiq.mission.entity.Mission;
import com.droneiq.mission.repository.MissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MissionService {

    private final MissionRepository missionRepository;

    public MissionService(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    @Transactional(readOnly = true)
    public List<MissionResponse> getAllMissions() {
        return missionRepository.findAll().stream()
                .map(MissionResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public MissionResponse getMissionById(Long id) {
        return missionRepository.findById(id)
                .map(MissionResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with ID: " + id));
    }

    @Transactional
    public MissionResponse createMission(CreateMissionRequest request, String createdBy) {
        Mission mission = new Mission();
        mission.setName(request.name());
        mission.setDroneId(request.droneId());
        mission.setDescription(request.description());
        mission.setWaypointsJson(request.waypointsJson());
        mission.setStatus("PLANNED");
        mission.setCreatedBy(createdBy);

        Mission saved = missionRepository.save(mission);
        return MissionResponse.fromEntity(saved);
    }

    @Transactional
    public MissionResponse updateMission(Long id, CreateMissionRequest request) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with ID: " + id));

        mission.setName(request.name());
        mission.setDroneId(request.droneId());
        mission.setDescription(request.description());
        mission.setWaypointsJson(request.waypointsJson());

        Mission saved = missionRepository.save(mission);
        return MissionResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteMission(Long id) {
        if (!missionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Mission not found with ID: " + id);
        }
        missionRepository.deleteById(id);
    }
}
