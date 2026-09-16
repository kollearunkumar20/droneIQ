package com.droneiq.auth.entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Role-Based Access Control (RBAC) Roles and Authority Mappings.
 * Corresponds directly to the DroneIQ Ground Control Station RBAC Matrix.
 */
public enum Role {
    SUPER_ADMIN,
    FLEET_MANAGER,
    FLIGHT_OPERATOR,
    VIEWER,

    // Backward-compatibility aliases
    ADMIN,
    OPERATOR,
    PILOT;

    /**
     * Resolves the canonical role for permissions evaluation.
     */
    public Role toCanonical() {
        return switch (this) {
            case ADMIN -> SUPER_ADMIN;
            case OPERATOR, PILOT -> FLIGHT_OPERATOR;
            default -> this;
        };
    }

    /**
     * Returns the fine-grained permission authorities associated with this role
     * as defined in the DroneIQ RBAC Matrix.
     */
    public Set<String> getPermissions() {
        Set<String> permissions = new HashSet<>();

        switch (toCanonical()) {
            case SUPER_ADMIN -> {
                // 1. System Config & User Management: Full Access
                permissions.add("SYSTEM_CONFIG:FULL");
                permissions.add("USER:MANAGE");
                permissions.add("USER:READ");
                permissions.add("USER:INVITE");

                // 2. Drone & Fleet Asset Registration: Full Access
                permissions.add("ASSET:FULL");
                permissions.add("ASSET:VIEW");

                // 3. Mission Planning & Waypoint Editing: Full Access
                permissions.add("MISSION:FULL");
                permissions.add("MISSION:WRITE");
                permissions.add("MISSION:VIEW");

                // 4. Flight Command Execution: Full Access
                permissions.add("FLIGHT_COMMAND:EXECUTE");
                permissions.add("FLIGHT_COMMAND:OVERRIDE");

                // 5. Live Telemetry & Video Egress HUD: Full Access
                permissions.add("TELEMETRY:FULL");
                permissions.add("TELEMETRY:VIEW");

                // 6. AI Incident Triage & Evidence Export: Full Access
                permissions.add("INCIDENT:FULL");
                permissions.add("INCIDENT:TRIAGE");
                permissions.add("INCIDENT:VIEW");
            }
            case FLEET_MANAGER -> {
                // 1. System Config & User Management: Read / Invite Users
                permissions.add("USER:READ");
                permissions.add("USER:INVITE");

                // 2. Drone & Fleet Asset Registration: Full Access
                permissions.add("ASSET:FULL");
                permissions.add("ASSET:VIEW");

                // 3. Mission Planning & Waypoint Editing: Full Access
                permissions.add("MISSION:FULL");
                permissions.add("MISSION:WRITE");
                permissions.add("MISSION:VIEW");

                // 4. Flight Command Execution: Override Access Only
                permissions.add("FLIGHT_COMMAND:OVERRIDE");

                // 5. Live Telemetry & Video Egress HUD: Full Access
                permissions.add("TELEMETRY:FULL");
                permissions.add("TELEMETRY:VIEW");

                // 6. AI Incident Triage & Evidence Export: Full Access
                permissions.add("INCIDENT:FULL");
                permissions.add("INCIDENT:TRIAGE");
                permissions.add("INCIDENT:VIEW");
            }
            case FLIGHT_OPERATOR -> {
                // 1. System Config & User Management: No Access

                // 2. Drone & Fleet Asset Registration: View Assets
                permissions.add("ASSET:VIEW");

                // 3. Mission Planning & Waypoint Editing: Create & Save Plans
                permissions.add("MISSION:WRITE");
                permissions.add("MISSION:VIEW");

                // 4. Flight Command Execution: Full Execution
                permissions.add("FLIGHT_COMMAND:EXECUTE");

                // 5. Live Telemetry & Video Egress HUD: Full Access
                permissions.add("TELEMETRY:FULL");
                permissions.add("TELEMETRY:VIEW");

                // 6. AI Incident Triage & Evidence Export: Triage Alerts
                permissions.add("INCIDENT:TRIAGE");
                permissions.add("INCIDENT:VIEW");
            }
            case VIEWER -> {
                // 1. System Config & User Management: No Access

                // 2. Drone & Fleet Asset Registration: View Assets
                permissions.add("ASSET:VIEW");

                // 3. Mission Planning & Waypoint Editing: View Plans Only
                permissions.add("MISSION:VIEW");

                // 4. Flight Command Execution: NO EXECUTION

                // 5. Live Telemetry & Video Egress HUD: Live View Only
                permissions.add("TELEMETRY:VIEW");

                // 6. AI Incident Triage & Evidence Export: View Reports Only
                permissions.add("INCIDENT:VIEW");
            }
        }

        return Collections.unmodifiableSet(permissions);
    }
}
