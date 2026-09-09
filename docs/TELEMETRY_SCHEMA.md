# DroneIQ Telemetry Schema & Data Specification

This document defines the normalized telemetry data contract used across Kafka topics, WebSocket streams, and AWS S3 Data Lake archival.

---

## 1. Data Contract Overview

The telemetry pipeline normalizes all raw MAVLink protocol packets (such as `GLOBAL_POSITION_INT`, `ATTITUDE`, `SYS_STATUS`, `BATTERY_STATUS`, and `HEARTBEAT`) into a unified, high-precision JSON schema.

### Topic Details
- **Kafka Topic**: `drone.telemetry`
- **Partition Key**: `droneId` (ensures strict message ordering per drone)
- **Serialization**: JSON
- **Compression**: Snappy / LZ4 (production)

---

## 2. Field Specifications

| Field | Type | Required | Units | Range / Constraints | Description |
| :--- | :--- | :---: | :--- | :--- | :--- |
| `droneId` | `String` | Yes | - | Alphanumeric, `_`, `-` | Unique identifier of the drone |
| `timestamp` | `String (ISO-8601)` | Yes | UTC | Valid ISO-8601 timestamp | Instant the telemetry was sampled |
| `latitude` | `BigDecimal` | Yes | Degrees | `[-90.000000, 90.000000]` | WGS84 GPS Latitude |
| `longitude` | `BigDecimal` | Yes | Degrees | `[-180.000000, 180.000000]` | WGS84 GPS Longitude |
| `altitude` | `BigDecimal` | Yes | Meters | `[0.00, 10000.00]` | Altitude Above Ground Level (AGL) / MSL |
| `heading` | `BigDecimal` | Yes | Degrees | `[0.0, 360.0]` | Compass heading (0 = North, 90 = East) |
| `speed` | `BigDecimal` | Yes | m/s | `[0.00, 150.00]` | Current ground speed |
| `roll` | `BigDecimal` | No | Degrees | `[-180.00, 180.00]` | Vehicle roll angle (banking) |
| `pitch` | `BigDecimal` | No | Degrees | `[-90.00, 90.00]` | Vehicle pitch angle (elevation) |
| `yaw` | `BigDecimal` | No | Degrees | `[0.0, 360.0]` | Vehicle yaw angle |
| `batteryPercentage` | `BigDecimal` | Yes | % | `[0.0, 100.0]` | Remaining battery capacity |
| `gpsSatellites` | `Integer` | No | Count | `>= 0` | Visible GPS satellites locked |
| `flightMode` | `String` | Yes | - | `MANUAL`, `AUTO`, `LOITER`, `GUIDED`, `RTL` | Autopilot flight mode |
| `armed` | `Boolean` | Yes | - | `true` / `false` | Motor arming status |
| `status` | `String` | No | - | `OK`, `WARNING`, `CRITICAL` | Vehicle diagnostic health state |
| `metadata` | `Map<String,Object>` | No | - | Arbitrary key-value pairs | Extensible payload (sensors, temperature) |

---

## 3. Example Telemetry Message Payload

```json
{
  "droneId": "DRONE-001",
  "timestamp": "2026-09-09T10:15:30.450Z",
  "latitude": 37.414521,
  "longitude": -122.053892,
  "altitude": 78.45,
  "heading": 145.2,
  "speed": 12.80,
  "roll": 3.85,
  "pitch": -1.20,
  "yaw": 145.2,
  "batteryPercentage": 92.5,
  "gpsSatellites": 16,
  "flightMode": "AUTO",
  "armed": true,
  "status": "OK",
  "metadata": {
    "voltageVolts": 24.2,
    "currentAmps": 18.5,
    "cpuLoadPercent": 14.0
  }
}
```

---

## 4. AWS S3 Data Lake Partitioning

Telemetry messages are micro-batched into newline-delimited JSON (`.jsonl`) files and persisted into Hive-style partitioned folders in AWS S3.

### S3 Key Structure

```
s3://<bucket-name>/telemetry/year=YYYY/month=MM/day=DD/drone_id={droneId}/telemetry_{timestamp}_{uuid}.jsonl
```

### Example S3 Path
```
s3://droneiq-telemetry-data-lake/telemetry/year=2026/month=09/day=09/drone_id=DRONE-001/telemetry_1788950400000_a1b2c3d4.jsonl
```

### AWS Athena External Table Definition

```sql
CREATE EXTERNAL TABLE IF NOT EXISTS droneiq.raw_telemetry (
    droneId STRING,
    timestamp STRING,
    latitude DOUBLE,
    longitude DOUBLE,
    altitude DOUBLE,
    heading DOUBLE,
    speed DOUBLE,
    roll DOUBLE,
    pitch DOUBLE,
    yaw DOUBLE,
    batteryPercentage DOUBLE,
    gpsSatellites INT,
    flightMode STRING,
    armed BOOLEAN,
    status STRING,
    metadata MAP<STRING, STRING>
)
PARTITIONED BY (
    year INT,
    month STRING,
    day STRING,
    drone_id STRING
)
ROW FORMAT SERDE 'org.openx.data.jsonserde.JsonSerDe'
LOCATION 's3://droneiq-telemetry-data-lake/telemetry/';
```

### Athena Query Example

```sql
-- Query average speed and minimum battery for DRONE-001 on Sep 9, 2026
SELECT 
    drone_id,
    AVG(speed) AS avg_speed_mps,
    MIN(batteryPercentage) AS min_battery_percent,
    MAX(altitude) AS max_altitude_meters
FROM droneiq.raw_telemetry
WHERE year = 2026 AND month = '09' AND day = '09' AND drone_id = 'DRONE-001'
GROUP BY drone_id;
```
