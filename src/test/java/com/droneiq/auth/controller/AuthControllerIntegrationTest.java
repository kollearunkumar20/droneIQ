package com.droneiq.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Complete E2E Auth and Drone API Flow")
    void testAuthAndProtectedApiFlow() throws Exception {
        // 1. Unauthenticated request to /api/drones should return 401 Unauthorized
        mockMvc.perform(get("/api/drones"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));

        // 2. Login with invalid password returns 401
        String invalidLoginJson = """
                {
                    "username": "admin",
                    "password": "wrongpassword"
                }
                """;
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidLoginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("Invalid username or password")));

        // 3. Login with valid seed admin credentials
        String validLoginJson = """
                {
                    "username": "admin",
                    "password": "admin123"
                }
                """;
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validLoginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.username", is("admin")))
                .andExpect(jsonPath("$.data.role", is("ADMIN")))
                .andExpect(jsonPath("$.data.token").isString())
                .andReturn();

        JsonNode loginResponse = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginResponse.get("data").get("token").asText();
        assertNotNull(token);

        // 4. Authenticated request to /api/auth/me returns current user
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username", is("admin")))
                .andExpect(jsonPath("$.data.email", is("admin@droneiq.io")));

        // 5. Authenticated request to /api/drones retrieves seeded fleet
        mockMvc.perform(get("/api/drones")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(3)));

        // 6. Register a new drone
        String newDroneJson = """
                {
                    "droneId": "DRONE-INT-TEST",
                    "model": "Integration Test Drone",
                    "firmwareVersion": "v1.0.0",
                    "ipAddress": "10.0.0.99"
                }
                """;
        mockMvc.perform(post("/api/drones")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newDroneJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.droneId", is("DRONE-INT-TEST")))
                .andExpect(jsonPath("$.data.model", is("Integration Test Drone")));

        // 7. Query newly created drone
        mockMvc.perform(get("/api/drones/DRONE-INT-TEST")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.droneId", is("DRONE-INT-TEST")));
    }
}
