# DroneIQ Ground Control Station (GCS) Backend

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.4](https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-KRaft-black.svg)](https://kafka.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![AWS S3](https://img.shields.io/badge/AWS%20S3-Data%20Lake-527FFF.svg)](https://aws.amazon.com/s3/)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

Production-grade, modular backend platform for modern Drone Ground Control Stations (GCS). Built for mission-critical telemetry ingestion, real-time WebSocket broadcasting, instantaneous low-latency state caching, and long-term analytical persistence into an AWS S3 Data Lake.

---

## 1. Architectural Pipeline

```
DRONE / MAVLink SOURCE (or Mock Simulator)
                   ↓
   Kafka Topic: drone.telemetry (Partitioned by droneId)
                   ↓
        Spring Boot 3 Backend
         ├── WebSocket (/ws/telemetry) ──────> Frontend Web GCS (Live Stream)
         ├── In-Memory Cache (Sub-ms) ──────> REST API (GET /api/drones/{id}/telemetry/latest)
         └── Micro-Batch Window (JSONL) ────> AWS S3 Data Lake (Athena / Analytics)
```

---

## 2. Key Highlights & Features

- **Standard Java 21 & Spring Boot 3.4**: Clean, modern records, pattern matching, type-safe builders, zero annotation processor issues.
- **Stateless JWT Security**: Secure `POST /api/auth/login`, BCrypt password hashing, and role-based permissions (`ADMIN`, `OPERATOR`, `PILOT`, `VIEWER`).
- **First-Message Authenticated WebSocket**: Browser-compatible in-band authentication protocol (`ws://localhost:8080/ws/telemetry`) that prevents token leakage in URL query parameters, with 5-second automatic timeout protection.
- **Kafka-Driven Normalized Telemetry**: Standardized `TelemetryMessage` schema with strict boundary validation (`latitude`, `longitude`, `altitude`, `heading`, `speed`, `roll`, `pitch`, `yaw`, `batteryPercentage`, `armed`, `flightMode`).
- **Realistic Flight Simulator**: Built-in 1Hz simulator generating smooth orbital flight physics, heading recalculation, altitude fluctuations, and gradual battery depletion across multiple simulated drones.
- **High-Performance Micro-Batched S3 Data Lake**: Buffers telemetry and asynchronously uploads newline-delimited JSON (`.jsonl`) to Hive-style partitions (`telemetry/year=YYYY/month=MM/day=DD/drone_id=.../`) ready for AWS Athena. S3 network calls never block live WebSocket streaming.
- **Effortless Local Dev**: Operates completely locally via Docker Compose (PostgreSQL 16 & Kafka in KRaft mode). S3 automatically falls back to an offline no-op implementation when AWS credentials are not configured.

---

## 3. Technology Stack

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Language** | Java 21 | Modern LTS features, Records, Pattern matching |
| **Framework** | Spring Boot 3.4.3 | Spring Web, Spring WebSocket, Spring Security, Spring Data JPA |
| **Database** | PostgreSQL 16 | Relational persistence for users and drone fleet metadata |
| **Migrations** | Flyway | Versioned database migrations (`V1`, `V2`) |
| **Message Broker** | Apache Kafka | KRaft mode (ready for zero-code migration to Amazon MSK) |
| **Object Storage** | AWS SDK v2 S3 | Micro-batched analytical archival (compatible with AWS Athena) |
| **Auth / Security** | Spring Security + JJWT 0.12.6 | Stateless bearer authentication |
| **Documentation** | Springdoc OpenAPI 3 / Swagger | Interactive API testing at `/swagger-ui.html` |
| **Build & Tooling** | Maven 3.9+ / Docker Compose | Containerized local infrastructure |

---

## 4. Quickstart in 3 Steps

### Step 1: Start Local Infrastructure
```bash
docker compose up -d
```
Starts PostgreSQL 16 on port `5432` and Apache Kafka (KRaft mode) on port `9092`.

### Step 2: Configure Environment
```bash
cp .env.example .env
```

### Step 3: Launch Application
```bash
# On Linux / macOS:
./mvnw spring-boot:run

# On Windows (PowerShell):
.\mvnw.cmd spring-boot:run
```

The application will start at `http://localhost:8080`.

---

## 5. Default Credentials & Test Data

The application automatically seeds initial data on startup:

| Username | Password | Role | Description |
| :--- | :--- | :--- | :--- |
| `admin` | `admin123` | `ADMIN` | Full administrator privileges |
| `operator` | `operator123` | `OPERATOR` | Ground control operator |

### Pre-registered Drones
- `DRONE-001` (Quadcopter Alpha) - Active in mock simulator
- `DRONE-002` (Hexacopter Beta) - Active in mock simulator
- `DRONE-003` (VTOL Explorer) - Active in mock simulator

---

## 6. Documentation Hub

Detailed documentation is available in the [`docs/`](docs/) directory:

- 📖 **[Frontend Integration Guide](docs/FRONTEND_INTEGRATION.md)**: Complete WebSocket handshake, message frames, and TypeScript client code.
- 📐 **[Telemetry Schema Specification](docs/TELEMETRY_SCHEMA.md)**: Data types, valid coordinate ranges, units, and AWS Athena DDL queries.
- 🏗️ **[System Architecture](docs/ARCHITECTURE.md)**: Architectural diagrams, pipeline breakdown, and AWS cloud migration path.
- 🚀 **[Local Setup & Developer Guide](docs/LOCAL_SETUP.md)**: Detailed step-by-step local workflow, Swagger UI, and `wscat` testing.

---

## 7. Running Tests

Execute the complete test suite:

```bash
./mvnw clean test
```
