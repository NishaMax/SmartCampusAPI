# Smart Campus API - 20-Day Development Plan

This plan breaks down the coursework into manageable daily tasks to ensure consistent progress and a rich commit history.

## Phase 1: Setup & Architecture (Days 1-4)

### Day 1: Project Initialization

- [x] Create a Maven project (`pom.xml`).
- [x] Add dependencies: Jersey (JAX-RS), Servlet Container (Jetty/Tomcat), Jackson (JSON).
- [x] Create strict package structure (e.g., `com.westminster.smartcampus`).
- [ ] **Commit Message:** "Initial project structure and dependencies"

### Day 2: Application Config & Core Models

- [ ] Create `SmartCampusApp` extending `javax.ws.rs.core.Application`.
- [ ] Set `@ApplicationPath("/api/v1")`.
- [ ] Create POJO classes: `Room`, `Sensor`, `SensorReading` (fields/getters/setters).
- [ ] **Commit Message:** "Added application config and core data models"

### Day 3: In-Memory Database Service

- [ ] Create a `DataStore` singleton class (since no SQL allowed).
- [ ] Add `ConcurrentHashMap` for Rooms and Sensors.
- [ ] Implement thread-safe methods to add/get items.
- [ ] **Commit Message:** "Implemented in-memory data store singleton"

### Day 4: Part 1 - Discovery Endpoint & Report

- [ ] Implement `DiscoveryResource` at `GET /api/v1`.
- [ ] Return JSON with API metadata and links.
- [ ] **Report:** Answer Q1 (Lifecycle) and Q2 (Hypermedia) in `ReadMe.md`.
- [ ] **Commit Message:** "Added discovery endpoint and answered Part 1 questions"

## Phase 2: Room Management (Days 5-8)

### Day 5: Room Resource - Basic CRUD

- [ ] Create `RoomResource`.
- [ ] Implement `GET /rooms` (list all).
- [ ] Implement `POST /rooms` (create new).
- [ ] **Commit Message:** "Implemented Room listing and creation"

### Day 6: Room Resource - Details & Storage Logic

- [ ] Implement `GET /rooms/{id}`.
- [ ] Connect `RoomResource` to `DataStore` logic.
- [ ] **Commit Message:** "Added fetch single room functionality"

### Day 7: Room Deletion Logic

- [ ] Implement `DELETE /rooms/{id}`.
- [ ] Add TODO logic for checking sensors (placeholder for now).
- [ ] **Commit Message:** "Added room deletion endpoint"

### Day 8: Part 2 Verification & Report

- [ ] Test Room endpoints with Curl/Postman.
- [ ] **Report:** Answer Q3 (List vs Full Objects) and Q4 (Idempotency) in `ReadMe.md`.
- [ ] **Commit Message:** "Finalized Part 2 rooms and added report answers"

## Phase 3: Sensors & Linking (Days 9-12)

### Day 9: Sensor Resource - Basic Creation

- [ ] Create `SensorResource`.
- [ ] Implement `POST /sensors`.
- [ ] Add validation: Check if `roomId` exists in `DataStore`.
- [ ] **Commit Message:** "Implemented sensor creation with room validation"

### Day 10: Sensor Resource - Listing & Filtering

- [ ] Implement `GET /sensors`.
- [ ] Add `@QueryParam("type")` filtering logic.
- [ ] **Commit Message:** "Added sensor listing with type filtering"

### Day 11: Linking Back to Rooms

- [ ] Update `RoomResource` DELETE logic.
- [ ] **Constraint:** Block deletion if sensors exist for that room.
- [ ] **Commit Message:** "Enforced constraint: cannot delete room with sensors"

### Day 12: Part 3 Report & Cleanup

- [ ] Refactor code if needed.
- [ ] **Report:** Answer Q5 (Media Types) and Q6 (Query Params) in `ReadMe.md`.
- [ ] **Commit Message:** "Completed Part 3 report questions"

## Phase 4: Sub-Resources & Readings (Days 13-16)

### Day 13: Sub-Resource Setup

- [ ] Modify `SensorResource` to handle `{id}/readings`.
- [ ] Create `SensorReadingResource`.
- [ ] Implement "Sub-resource locator" pattern.
- [ ] **Commit Message:** "Implemented sub-resource locator structure"

### Day 14: Reading Operations

- [ ] In `SensorReadingResource`: Implement `GET /` (history).
- [ ] In `SensorReadingResource`: Implement `POST /` (add reading).
- [ ] **Report:** Answer Q7 (Sub-Resource Locator benefits).
- [ ] **Commit Message:** "Added reading history and creation"

### Day 15: Side Effects (Business Logic)

- [ ] Update `POST` reading logic.
- [ ] **Side Effect:** Update `currentValue` in parent `Sensor` object.
- [ ] **Commit Message:** "Implemented side-effects: updating sensor current value"

### Day 16: Part 4 Testing

- [ ] Verify deep nesting: `POST /sensors/S1/readings`.
- [ ] Verify parent update.
- [ ] **Commit Message:** "Refined sub-resource logic and tests"

## Phase 5: Error Handling & Observability (Days 17-19)

### Day 17: Custom Exceptions (409 & 422)

- [ ] Create `RoomNotEmptyException` + Mapper (409 Conflict).
- [ ] Create `LinkedResourceNotFoundException` + Mapper (422 Unprocessable).
- [ ] **Commit Message:** "Added 409 and 422 exception handling"

### Day 18: Security & Catch-all (403 & 500)

- [ ] Create `SensorUnavailableException` (403).
- [ ] Create generic `ExceptionMapper<Throwable>` (500).
- [ ] **Report:** Answer Q8 (422 vs 404) and Q9 (Stack trace risks).
- [ ] **Commit Message:** "Implemented security exception mappers and 500 catch-all"

### Day 19: Logging Filters

- [ ] Implement `ContainerRequestFilter` & `ContainerResponseFilter`.
- [ ] Log method, URI, and status codes.
- [ ] **Report:** Answer Q10 (Filters vs manual logging).
- [ ] **Commit Message:** "Added request/response logging filters"

## Phase 6: Final Polish (Day 20)

### Day 20: Documentation & Submission

- [ ] Update `ReadMe.md` with Build instructions.
- [ ] Add 5 sample Curl commands.
- [ ] Check against constraints (No Spring, No DB).
- [ ] **Commit Message:** "Final submission polish and documentation"
