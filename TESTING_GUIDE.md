# Smart Campus API – Manual Testing Guide (Postman)

This document is a **manual test checklist** for the Smart Campus REST API.

- **Base path:** `/api/v1`
- **Default example host:** `http://localhost:8080`
- **Content-Type:** `application/json` for POST requests

---

## 1) Start the API (how to run)

### Option A: Run with Jetty (recommended for this project)

This project includes the **Jetty Maven plugin**, so you can run the WAR using:

- Build: `mvn clean install`
- Run: `mvn jetty:run`

Then open:

- `GET http://localhost:8080/api/v1`

> Note: The exact port can be configured by Jetty; by default it is typically 8080.

### Option B: Deploy to Tomcat (external servlet container)

The project builds a standard `.war` (`target/smart-campus-api.war`) that can be deployed to **Apache Tomcat 9+**.

- Copy `target/smart-campus-api.war` into Tomcat’s `webapps/` folder.
- Start Tomcat.

The URL becomes:

- `http://localhost:8080/smart-campus-api/api/v1`

Because Tomcat adds the **context path** equal to the WAR name (`smart-campus-api`).

---

## 2) What server are we using (Tomcat vs Jetty)?

- **In code**: the API is a standard **Servlet-based Jersey WAR** (`packaging=war`, dependency `jersey-container-servlet`).
- **For local running**: the project includes **Jetty** via `jetty-maven-plugin` in `pom.xml`. That means:
  - You don’t need to install Tomcat to run locally.
  - You _can_ still deploy the WAR to Tomcat if you want.

So the project is **not “Tomcat-only”**. It is a WAR that can run in **any Servlet container** (Jetty/Tomcat), and the repo is set up to run easiest with **Jetty Maven**.

---

## 3) Logging and error responses (what to expect)

### Request/Response logging

A JAX-RS filter logs to stdout:

- method + full URL on request
- method + full URL + status code + elapsed time on response

### Standard error body

Errors are returned as JSON similar to:

```json
{
  "code": "NOT_FOUND",
  "message": "Room not found",
  "status": 404,
  "path": "/api/v1/rooms/XYZ",
  "timestamp": "..."
}
```

---

## 4) Test plan (run in this order)

### A. Discovery

1. **GET** `/api/v1`
   - Expect: `200 OK`
   - Confirm it lists useful metadata/links.

### B. Rooms

2. **POST** `/api/v1/rooms`
   - Body example:
     ```json
     { "id": "LIB-301", "name": "Library Quiet Study", "capacity": 40 }
     ```
   - Expect: `201 Created`

3. **GET** `/api/v1/rooms`
   - Expect: `200 OK`
   - Confirm the new room appears.

4. **GET** `/api/v1/rooms/LIB-301`
   - Expect: `200 OK`

5. **POST** `/api/v1/rooms` again with same id
   - Expect: `409 Conflict` (duplicate)

6. **DELETE** `/api/v1/rooms/LIB-301`
   - Expect: `204 No Content` (only if no sensors linked)

7. **DELETE** `/api/v1/rooms/LIB-301` again
   - Expect: `404 Not Found` (idempotency check)

### C. Sensors

8. Create a room again (if deleted): **POST** `/api/v1/rooms`

9. **POST** `/api/v1/sensors`
   - Body example:
     ```json
     {
       "id": "TEMP-001",
       "type": "Temperature",
       "status": "ACTIVE",
       "currentValue": 0,
       "roomId": "LIB-301"
     }
     ```
   - Expect: `201 Created`

10. **GET** `/api/v1/sensors`
    - Expect: `200 OK`

11. **GET** `/api/v1/sensors?type=Temperature`
    - Expect: `200 OK`
    - Confirm only Temperature sensors returned.

12. **GET** `/api/v1/sensors/TEMP-001`
    - Expect: `200 OK`
    - Confirm returned `id` is `TEMP-001`.

13. **PUT** `/api/v1/sensors/TEMP-001` (full replace)
    - Body example:
      ```json
      {
        "id": "TEMP-001",
        "type": "Temperature",
        "status": "MAINTENANCE",
        "currentValue": 99.9,
        "roomId": "LIB-301"
      }
      ```
    - Expect: `200 OK`
    - Confirm new `status`/`currentValue` persisted (check with GET by id).

14. **PUT** `/api/v1/sensors/TEMP-001` with attempt to change ID (ID immortality)
    - Body example:
      ```json
      {
        "id": "TEMP-999",
        "type": "Temperature",
        "status": "ACTIVE",
        "currentValue": 0,
        "roomId": "LIB-301"
      }
      ```
    - Expect: `400 Bad Request`

15. **PUT** `/api/v1/sensors/TEMP-001` with non-existent roomId (semantic validation)
    - Body example:
      ```json
      {
        "id": "TEMP-001",
        "type": "Temperature",
        "status": "ACTIVE",
        "currentValue": 0,
        "roomId": "GHOST-ROOM"
      }
      ```
    - Expect: `422 Unprocessable Entity`

16. **POST** `/api/v1/sensors` with non-existent roomId
    - Example: `roomId":"NOPE"`
    - Expect: `422 Unprocessable Entity`

17. **POST** `/api/v1/sensors` with duplicate id
    - Expect: `409 Conflict`

18. **DELETE** `/api/v1/sensors/TEMP-001`
    - Expect: `204 No Content`

19. **GET** `/api/v1/sensors/TEMP-001` after delete
    - Expect: `404 Not Found`

### D. Readings (sub-resource)

14. **POST** `/api/v1/sensors/TEMP-001/readings`
    - Body example:
      ```json
      { "id": "R-001", "timestamp": 1710000000000, "value": 23.5 }
      ```
    - Expect: `201 Created`

15. **GET** `/api/v1/sensors/TEMP-001/readings`
    - Expect: `200 OK`
    - Confirm `R-001` appears.

16. Side-effect check:
    - (No direct GET `/sensors/{id}` exists in this API.)
    - Practical check: add another reading `R-002` with `value: 30.1` then confirm the Sensor list shows updated `currentValue`.
    - **GET** `/api/v1/sensors` and locate `TEMP-001.currentValue`.

### E. Room deletion constraint

17. **DELETE** `/api/v1/rooms/LIB-301`
    - Expect: `409 Conflict` because `TEMP-001` is linked to the room.

### F. 403 Forbidden rule (Day 18)

18. Create an OFFLINE sensor:
    - **POST** `/api/v1/sensors`
      ```json
      {
        "id": "TEMP-OFF",
        "type": "Temperature",
        "status": "OFFLINE",
        "currentValue": 0,
        "roomId": "LIB-301"
      }
      ```
    - Expect: `201 Created`

19. Try to add a reading to it:
    - **POST** `/api/v1/sensors/TEMP-OFF/readings`
      ```json
      { "id": "R-OFF", "timestamp": 1710000001000, "value": 10.0 }
      ```
    - Expect: `403 Forbidden`

---

## 5) Extra negative tests (quick)

- POST `/api/v1/rooms` missing `id` → expect `400`
- POST `/api/v1/sensors` missing `roomId` → expect `400`
- POST `/api/v1/sensors/TEMP-001/readings` missing `id` → expect `400`
- GET `/api/v1/sensors/DOES-NOT-EXIST/readings` → expect `404`

---

## 6) Notes about in-memory storage

The API uses in-memory collections. That means:

- Data resets when you stop/restart the server.
- You should create Rooms/Sensors again after restarting.
