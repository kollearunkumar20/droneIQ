-- Flyway Migration V3: RBAC Operational Tables (Missions, Commands, Incidents, System Configs)

-- 1. Missions Table for Mission Planning & Waypoint Editing
CREATE TABLE IF NOT EXISTS missions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    drone_id VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',
    waypoints_json TEXT NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_missions_drone_id ON missions(drone_id);
CREATE INDEX IF NOT EXISTS idx_missions_status ON missions(status);

-- 2. Flight Commands Table for Command Execution Audit Trail
CREATE TABLE IF NOT EXISTS flight_commands (
    id BIGSERIAL PRIMARY KEY,
    drone_id VARCHAR(50) NOT NULL,
    command_type VARCHAR(50) NOT NULL,
    is_override BOOLEAN NOT NULL DEFAULT FALSE,
    executed_by VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'EXECUTED',
    payload_json TEXT,
    executed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_commands_drone_id ON flight_commands(drone_id);
CREATE INDEX IF NOT EXISTS idx_commands_executed_by ON flight_commands(executed_by);

-- 3. AI Incidents Table for Incident Triage & Evidence Export
CREATE TABLE IF NOT EXISTS incidents (
    id BIGSERIAL PRIMARY KEY,
    incident_code VARCHAR(50) NOT NULL UNIQUE,
    drone_id VARCHAR(50) NOT NULL,
    title VARCHAR(150) NOT NULL,
    severity VARCHAR(30) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    details TEXT,
    triaged_by VARCHAR(50),
    triaged_notes TEXT,
    evidence_exported BOOLEAN NOT NULL DEFAULT FALSE,
    evidence_checksum VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_incidents_drone_id ON incidents(drone_id);
CREATE INDEX IF NOT EXISTS idx_incidents_severity ON incidents(severity);
CREATE INDEX IF NOT EXISTS idx_incidents_status ON incidents(status);

-- 4. System Configurations Table for System Config & User Management
CREATE TABLE IF NOT EXISTS system_configs (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    updated_by VARCHAR(50) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_system_configs_key ON system_configs(config_key);
