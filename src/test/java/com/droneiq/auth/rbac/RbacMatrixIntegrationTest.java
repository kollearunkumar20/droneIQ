package com.droneiq.auth.rbac;

import com.droneiq.auth.dto.LoginRequest;
import com.droneiq.auth.dto.LoginResponse;
import com.droneiq.auth.entity.Role;
import com.droneiq.auth.entity.User;
import com.droneiq.auth.repository.UserRepository;
import com.droneiq.incident.entity.Incident;
import com.droneiq.incident.repository.IncidentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RbacMatrixIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.droneiq.telemetry.service.CurrentDroneTelemetryService currentTelemetryService;

    private String superAdminToken;
    private String fleetManagerToken;
    private String flightOperatorToken;
    private String viewerToken;
    private Long sampleIncidentId;

    @BeforeEach
    void setUp() throws Exception {
        ensureUserExists("superadmin", "superadmin@droneiq.io", "admin123", Role.SUPER_ADMIN);
        ensureUserExists("fleet_manager", "manager@droneiq.io", "manager123", Role.FLEET_MANAGER);
        ensureUserExists("pilot", "pilot@droneiq.io", "operator123", Role.FLIGHT_OPERATOR);
        ensureUserExists("viewer", "viewer@droneiq.io", "viewer123", Role.VIEWER);

        superAdminToken = obtainToken("superadmin", "admin123");
        fleetManagerToken = obtainToken("fleet_manager", "manager123");
        flightOperatorToken = obtainToken("pilot", "operator123");
        viewerToken = obtainToken("viewer", "viewer123");

        currentTelemetryService.updateTelemetry(com.droneiq.telemetry.dto.TelemetryMessage.builder()
                .droneId("DRONE-001")
                .timestamp(java.time.Instant.now())
                .latitude(java.math.BigDecimal.valueOf(37.7749))
                .longitude(java.math.BigDecimal.valueOf(-122.4194))
                .altitude(java.math.BigDecimal.valueOf(120.0))
                .heading(java.math.BigDecimal.valueOf(180.0))
                .speed(java.math.BigDecimal.valueOf(15.5))
                .batteryPercentage(java.math.BigDecimal.valueOf(92.0))
                .armed(true)
                .flightMode("AUTO")
                .build());

        if (incidentRepository.count() == 0) {
            Incident incident = new Incident("INC-001", "DRONE-001", "GPS Degraded Navigation Alert", "HIGH", "ACTIVE", "Signal loss detected");
            sampleIncidentId = incidentRepository.save(incident).getId();
        } else {
            sampleIncidentId = incidentRepository.findAll().get(0).getId();
        }
    }

    private void ensureUserExists(String username, String email, String rawPassword, Role role) {
        if (!userRepository.existsByUsername(username)) {
            User user = User.builder()
                    .username(username)
                    .email(email)
                    .passwordHash(passwordEncoder.encode(rawPassword))
                    .role(role)
                    .build();
            userRepository.save(user);
        }
    }

    private String obtainToken(String username, String password) throws Exception {
        LoginRequest request = new LoginRequest(username, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        LoginResponse response = objectMapper.readTree(json).get("data").traverse(objectMapper).readValueAs(LoginResponse.class);
        return response.token();
    }

    // ==========================================
    // 1. Super Admin: Full Access Across All 6 Areas
    // ==========================================
    @Test
    @DisplayName("Super Admin: Full Access across all 6 feature areas")
    void testSuperAdminFullAccess() throws Exception {
        // Area 1: System Config & User Management
        mockMvc.perform(get("/api/system/config")
                        .header("Authorization", "Bearer " + superAdminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + superAdminToken))
                .andExpect(status().isOk());

        // Area 2: Fleet Asset Registration
        String newDrone = """
                {"droneId":"DRONE-ADMIN-01","model":"Matrice 350 RTK","firmwareVersion":"v4.0.0"}
                """;
        mockMvc.perform(post("/api/drones")
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newDrone))
                .andExpect(status().isCreated());

        // Area 3: Mission Planning
        String newMission = """
                {"name":"Perimeter Scan","droneId":"DRONE-001","waypointsJson":"[{\\"lat\\":37.77,\\"lon\\":-122.41}]"}
                """;
        MvcResult missionResult = mockMvc.perform(post("/api/missions")
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newMission))
                .andExpect(status().isCreated())
                .andReturn();
        Long missionId = objectMapper.readTree(missionResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        // Area 3: Delete Mission (Full Access)
        mockMvc.perform(delete("/api/missions/" + missionId)
                        .header("Authorization", "Bearer " + superAdminToken))
                .andExpect(status().isOk());

        // Area 4: Flight Command Execution (Routine & Override)
        String routineCmd = """
                {"droneId":"DRONE-001","commandType":"ARM"}
                """;
        mockMvc.perform(post("/api/commands/execute")
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(routineCmd))
                .andExpect(status().isOk());

        String overrideCmd = """
                {"droneId":"DRONE-001","commandType":"OVERRIDE_RTL"}
                """;
        mockMvc.perform(post("/api/commands/override")
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(overrideCmd))
                .andExpect(status().isOk());

        // Area 5: Live Telemetry & Video HUD
        mockMvc.perform(get("/api/drones/DRONE-001/telemetry/latest")
                        .header("Authorization", "Bearer " + superAdminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/video/streams")
                        .header("Authorization", "Bearer " + superAdminToken))
                .andExpect(status().isOk());

        // Area 6: AI Incident Triage & Evidence Export
        String triageReq = """
                {"status":"UNDER_INVESTIGATION","triagedNotes":"Investigated by Super Admin"}
                """;
        mockMvc.perform(post("/api/incidents/" + sampleIncidentId + "/triage")
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(triageReq))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/incidents/" + sampleIncidentId + "/export")
                        .header("Authorization", "Bearer " + superAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED_IMMUTABLE"));
    }

    // ==========================================
    // 2. Fleet Manager: Specific RBAC Bounds
    // ==========================================
    @Test
    @DisplayName("Fleet Manager: Allowed & Restricted boundaries")
    void testFleetManagerAccess() throws Exception {
        // Area 1: Allowed: Read Users
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + fleetManagerToken))
                .andExpect(status().isOk());

        // Area 1: Allowed: Invite Operator/Viewer
        String inviteOp = """
                {"username":"new_pilot_fm","email":"pilot_fm@droneiq.io","temporaryPassword":"password123","role":"FLIGHT_OPERATOR"}
                """;
        mockMvc.perform(post("/api/users/invite")
                        .header("Authorization", "Bearer " + fleetManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inviteOp))
                .andExpect(status().isCreated());

        // Area 1: Denied (403): Fleet Manager cannot invite Admin
        String inviteAdmin = """
                {"username":"unauthorized_admin","email":"hacked_admin@droneiq.io","temporaryPassword":"password123","role":"SUPER_ADMIN"}
                """;
        mockMvc.perform(post("/api/users/invite")
                        .header("Authorization", "Bearer " + fleetManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inviteAdmin))
                .andExpect(status().isForbidden());

        // Area 1: Denied (403): Fleet Manager cannot access System Config
        mockMvc.perform(get("/api/system/config")
                        .header("Authorization", "Bearer " + fleetManagerToken))
                .andExpect(status().isForbidden());

        // Area 2: Allowed: Fleet Asset Registration (Full Access)
        String newDrone = """
                {"droneId":"DRONE-FM-01","model":"Inspire 3","firmwareVersion":"v2.1.0"}
                """;
        mockMvc.perform(post("/api/drones")
                        .header("Authorization", "Bearer " + fleetManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newDrone))
                .andExpect(status().isCreated());

        // Area 3: Allowed: Mission Planning (Full Access)
        String missionJson = """
                {"name":"Survey Area 4","droneId":"DRONE-001","waypointsJson":"[{\\"lat\\":37.77,\\"lon\\":-122.41}]"}
                """;
        MvcResult mResult = mockMvc.perform(post("/api/missions")
                        .header("Authorization", "Bearer " + fleetManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missionJson))
                .andExpect(status().isCreated())
                .andReturn();
        Long mId = objectMapper.readTree(mResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        mockMvc.perform(delete("/api/missions/" + mId)
                        .header("Authorization", "Bearer " + fleetManagerToken))
                .andExpect(status().isOk());

        // Area 4: Denied (403): Routine flight execution (reserved for pilots)
        String routineCmd = """
                {"droneId":"DRONE-001","commandType":"TAKEOFF"}
                """;
        mockMvc.perform(post("/api/commands/execute")
                        .header("Authorization", "Bearer " + fleetManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(routineCmd))
                .andExpect(status().isForbidden());

        // Area 4: Allowed: Emergency Override Command
        String overrideCmd = """
                {"droneId":"DRONE-001","commandType":"EMERGENCY_LAND"}
                """;
        mockMvc.perform(post("/api/commands/override")
                        .header("Authorization", "Bearer " + fleetManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(overrideCmd))
                .andExpect(status().isOk());

        // Area 6: Allowed: AI Incident Triage & Evidence Export
        mockMvc.perform(post("/api/incidents/" + sampleIncidentId + "/export")
                        .header("Authorization", "Bearer " + fleetManagerToken))
                .andExpect(status().isOk());
    }

    // ==========================================
    // 3. Flight Operator / Pilot: Specific RBAC Bounds
    // ==========================================
    @Test
    @DisplayName("Flight Operator / Pilot: Allowed & Restricted boundaries")
    void testFlightOperatorAccess() throws Exception {
        // Area 1: Denied (403): User Management & System Config
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + flightOperatorToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/system/config")
                        .header("Authorization", "Bearer " + flightOperatorToken))
                .andExpect(status().isForbidden());

        // Area 2: Allowed: View Assets
        mockMvc.perform(get("/api/drones")
                        .header("Authorization", "Bearer " + flightOperatorToken))
                .andExpect(status().isOk());

        // Area 2: Denied (403): Cannot register drones
        String newDrone = """
                {"droneId":"DRONE-OP-01","model":"Unauthorized","firmwareVersion":"v1.0"}
                """;
        mockMvc.perform(post("/api/drones")
                        .header("Authorization", "Bearer " + flightOperatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newDrone))
                .andExpect(status().isForbidden());

        // Area 3: Allowed: Create & Save Mission Plans
        String opMission = """
                {"name":"Pilot Route 1","droneId":"DRONE-001","waypointsJson":"[{\\"lat\\":37.78,\\"lon\\":-122.42}]"}
                """;
        MvcResult opResult = mockMvc.perform(post("/api/missions")
                        .header("Authorization", "Bearer " + flightOperatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(opMission))
                .andExpect(status().isCreated())
                .andReturn();
        Long opMissionId = objectMapper.readTree(opResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        // Area 3: Denied (403): Flight Operator cannot delete mission
        mockMvc.perform(delete("/api/missions/" + opMissionId)
                        .header("Authorization", "Bearer " + flightOperatorToken))
                .andExpect(status().isForbidden());

        // Area 4: Allowed: Routine Command Execution
        String pilotCmd = """
                {"droneId":"DRONE-001","commandType":"TAKEOFF"}
                """;
        mockMvc.perform(post("/api/commands/execute")
                        .header("Authorization", "Bearer " + flightOperatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pilotCmd))
                .andExpect(status().isOk());

        // Area 4: Denied (403): Flight Operator cannot trigger Override Commands
        String overrideCmd = """
                {"droneId":"DRONE-001","commandType":"OVERRIDE_RTL"}
                """;
        mockMvc.perform(post("/api/commands/override")
                        .header("Authorization", "Bearer " + flightOperatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(overrideCmd))
                .andExpect(status().isForbidden());

        // Area 5: Allowed: Live Telemetry & Video HUD
        mockMvc.perform(get("/api/drones/DRONE-001/telemetry/latest")
                        .header("Authorization", "Bearer " + flightOperatorToken))
                .andExpect(status().isOk());

        // Area 6: Allowed: Triage Alerts
        String triageReq = """
                {"status":"ACKNOWLEDGED","triagedNotes":"Pilot acknowledging high wind"}
                """;
        mockMvc.perform(post("/api/incidents/" + sampleIncidentId + "/triage")
                        .header("Authorization", "Bearer " + flightOperatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(triageReq))
                .andExpect(status().isOk());

        // Area 6: Denied (403): Cannot Export Evidence
        mockMvc.perform(post("/api/incidents/" + sampleIncidentId + "/export")
                        .header("Authorization", "Bearer " + flightOperatorToken))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // 4. Viewer / Auditor: Read-Only & NO EXECUTION
    // ==========================================
    @Test
    @DisplayName("Viewer / Auditor: Live View & Reports Only, NO EXECUTION")
    void testViewerAuditorAccess() throws Exception {
        // Area 1: Denied (403): User Management & System Config
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/system/config")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isForbidden());

        // Area 2: Allowed: View Assets
        mockMvc.perform(get("/api/drones")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk());

        // Area 2: Denied (403): Modify Assets
        String droneAttempt = """
                {"droneId":"DRONE-VIEWER","model":"Forbidden","firmwareVersion":"v1.0"}
                """;
        mockMvc.perform(post("/api/drones")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(droneAttempt))
                .andExpect(status().isForbidden());

        // Area 3: Allowed: View Missions Only
        mockMvc.perform(get("/api/missions")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk());

        // Area 3: Denied (403): Cannot create missions
        String missionAttempt = """
                {"name":"Viewer Mission","droneId":"DRONE-001","waypointsJson":"[]"}
                """;
        mockMvc.perform(post("/api/missions")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missionAttempt))
                .andExpect(status().isForbidden());

        // Area 4: NO EXECUTION (403 for both routine and override commands)
        String executeCmd = """
                {"droneId":"DRONE-001","commandType":"ARM"}
                """;
        mockMvc.perform(post("/api/commands/execute")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(executeCmd))
                .andExpect(status().isForbidden());

        String overrideCmd = """
                {"droneId":"DRONE-001","commandType":"OVERRIDE_RTL"}
                """;
        mockMvc.perform(post("/api/commands/override")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(overrideCmd))
                .andExpect(status().isForbidden());

        // Area 5: Allowed: Live Telemetry & Video Egress HUD (Live View Only)
        mockMvc.perform(get("/api/drones/DRONE-001/telemetry/latest")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/video/streams")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/video/streams/DRONE-001/hud")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk());

        // Area 5: Denied (403): Telemetry Ingestion / Publishing
        String teleMsg = """
                {
                    "droneId":"DRONE-001",
                    "timestamp":1700000000000,
                    "latitude":37.77,
                    "longitude":-122.41,
                    "altitude":50.0,
                    "heading":180.0,
                    "speed":10.0,
                    "batteryPercentage":90.0,
                    "armed":true,
                    "flightMode":"GUIDED"
                }
                """;
        mockMvc.perform(post("/api/telemetry/publish")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(teleMsg))
                .andExpect(status().isForbidden());

        // Area 6: Allowed: View Reports
        mockMvc.perform(get("/api/incidents")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk());

        // Area 6: Denied (403): Triage Alerts
        String triageReq = """
                {"status":"RESOLVED","triagedNotes":"Viewer trying to resolve"}
                """;
        mockMvc.perform(post("/api/incidents/" + sampleIncidentId + "/triage")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(triageReq))
                .andExpect(status().isForbidden());

        // Area 6: Denied (403): Export Evidence
        mockMvc.perform(post("/api/incidents/" + sampleIncidentId + "/export")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isForbidden());
    }
}
