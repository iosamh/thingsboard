# ThingsBoard Device Ping Feature

## Overview

This implementation adds a new REST API endpoint to check device reachability.

## New API Endpoint

**GET** `/api/device/ping/{deviceId}`

**Response:**
```json
{
  "deviceId": "784f394c-42b6-435a-983c-b7beff2784f9",
  "reachable": true,
  "lastSeen": "2024-12-06T10:30:00Z",
  "deviceName": "Temperature Sensor 01",
  "inactivitySeconds": 30
}
```

## Build Instructions
```bash
# Build backend (skip tests)
mvn clean install -DskipTests -Dlicense.skip=true -pl !ui-ngx

# Run tests
mvn test -pl application -Dtest=DevicePingServiceTest -Dlicense.skip=true

# Build frontend
cd ui-ngx && npm install && npm run build
```

## Test the API

### Using Swagger UI
1. Start ThingsBoard: `docker compose up -d`
2. Open: http://localhost:8080/swagger-ui.html
3. Login and find "Device Ping" section
4. Try the `/api/device/ping/{deviceId}` endpoint

### Using cURL
```bash
# Get auth token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"tenant@thingsboard.org","password":"tenant"}' \
  | jq -r '.token')

# Ping device
curl -X GET "http://localhost:8080/api/device/ping/{DEVICE_ID}" \
  -H "X-Authorization: Bearer $TOKEN"
```

## Files Added

### Backend (application module)
| File | Path |
|------|------|
| DevicePingResponse.java | `src/main/java/org/thingsboard/server/controller/` |
| DevicePingService.java | `src/main/java/org/thingsboard/server/service/device/` |
| DevicePingServiceImpl.java | `src/main/java/org/thingsboard/server/service/device/` |
| DevicePingController.java | `src/main/java/org/thingsboard/server/controller/` |
| DevicePingServiceTest.java | `src/test/java/org/thingsboard/server/service/device/` |

### Frontend (ui-ngx module)
| File | Path |
|------|------|
| device-ping.service.ts | `src/app/core/http/` |

## Key Challenges

1. **Device Reachability Logic**: Used ThingsBoard's `lastActivityTime` server attribute instead of actual network ping, since IoT devices often can't be pinged directly.

2. **Following Conventions**: Extended `BaseController`, used `@TbCoreComponent`, `@PreAuthorize` for security.

3. **Async Handling**: Used `CompletableFuture.get()` with proper exception handling for attribute retrieval.

## Author
Osama - Software Engineer