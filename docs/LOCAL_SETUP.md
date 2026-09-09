# DroneIQ Local Development Setup Guide

Follow this guide to get the DroneIQ Ground Control Station backend running on your local machine.

---

## 1. Prerequisites

Ensure you have installed:
- **Java Development Kit (JDK)**: Java 21 or higher
- **Maven**: 3.9+ (or use the included `./mvnw` wrapper)
- **Docker & Docker Compose**: Docker 24+ and Docker Compose v2+
- **Git**

---

## 2. Environment Configuration

Copy the example environment template:

```bash
cp .env.example .env
```

Key environment variables:
| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `dev` | Active Spring profile |
| `SERVER_PORT` | `8080` | Application HTTP/WebSocket port |
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_USER` / `DB_PASSWORD` | `droneiq` / `droneiq_password` | Database credentials |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Local Kafka broker |
| `SIMULATOR_ENABLED` | `true` | Runs built-in realistic mock drone telemetry |
| `STORAGE_S3_ENABLED` | `false` | Set to `true` when AWS credentials are provided |

---

## 3. Starting Local Infrastructure with Docker Compose

DroneIQ provides a self-contained local stack with **PostgreSQL 16** and **Apache Kafka (KRaft mode)**:

```bash
docker compose up -d
```

Verify that all services are running and healthy:

```bash
docker compose ps
```

You should see:
- `droneiq-postgres`: Port `5432` (healthy)
- `droneiq-kafka`: Port `9092` (healthy)

---

## 4. Running the Spring Boot Backend

Start the application using the Maven wrapper:

```bash
# On Linux / macOS
./mvnw spring-boot:run

# On Windows (PowerShell)
.\mvnw.cmd spring-boot:run
```

Once started, the application will:
1. Automatically execute Flyway migrations creating tables `users` and `drones`.
2. Seed initial credentials:
   - **Admin**: `admin` / `admin123` (Role: `ADMIN`)
   - **Operator**: `operator` / `operator123` (Role: `OPERATOR`)
3. Seed default fleet: `DRONE-001`, `DRONE-002`, `DRONE-003`.
4. Start the mock telemetry simulator generating smooth flight physics at 1Hz.

---

## 5. Testing REST APIs with Swagger UI

1. Open your browser and navigate to:
   [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
2. Use `POST /api/auth/login` with:
   ```json
   {
     "username": "admin",
     "password": "admin123"
   }
   ```
3. Copy the returned `token`.
4. Click the green **Authorize** button at the top right, paste `Bearer <token>`, and click **Authorize**.
5. Test:
   - `GET /api/drones` - Lists the registered drone fleet.
   - `GET /api/drones/{droneId}/telemetry/latest` - Returns the real-time coordinates, altitude, speed, and battery.
   - `GET /api/telemetry/latest` - Returns latest state for all active drones.

---

## 6. Testing Real-Time WebSocket Streaming

### Option A: Using Browser Developer Console (No tools required)

Open your browser's Developer Tools Console (`F12`), paste the following snippet, and replace `<TOKEN>` with your JWT:

```javascript
const token = "<TOKEN>";
const ws = new WebSocket("ws://localhost:8080/ws/telemetry");

ws.onopen = () => {
    console.log("Connected to DroneIQ WebSocket!");
    // Send first-message authentication
    ws.send(JSON.stringify({ type: "AUTH", token: token }));
};

ws.onmessage = (event) => {
    const msg = JSON.parse(event.data);
    if (msg.type === "TELEMETRY") {
        console.log(`[Telemetry] Drone: ${msg.droneId} | Alt: ${msg.data.altitude}m | Bat: ${msg.data.batteryPercentage}% | Lat: ${msg.data.latitude} | Lon: ${msg.data.longitude}`);
    } else {
        console.log("[Server Event]", msg);
    }
};

ws.onclose = (e) => console.log("WebSocket closed:", e.code, e.reason);
```

### Option B: Using `wscat` CLI

```bash
# Install wscat if needed: npm install -g wscat
wscat -c ws://localhost:8080/ws/telemetry

# Send first-message authentication within 5 seconds:
{"type":"AUTH","token":"<YOUR_JWT_TOKEN>"}

# Subscribe to a specific drone:
{"type":"SUBSCRIBE","droneId":"DRONE-001"}
```

---

## 7. Running Tests

Run the complete automated test suite (Unit + Integration Tests with in-memory H2 database):

```bash
./mvnw clean test
```
