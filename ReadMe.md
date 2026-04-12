# Smart Campus API (5COSC022W Coursework)
Part 1 – Report Answers

### Q1) Default lifecycle of a JAX-RS Resource class

1. The Default Lifecycle: Per-Request
In JAX-RS, the default behavior is Request-Scoped.

This means that every time a user sends a request to an endpoint, the server creates a brand-new instance of the Resource class. Once the server sends the JSON response back, that instance is destroyed. It is not a singleton by default.

2. Impact on Data Management
Because a new class instance is created for every request, we cannot store our lists of rooms or sensors as regular variables inside the Resource class. If we did, the list would be empty every time a new request arrived! To prevent this data loss, we have to store the data in a Shared Service or a Singleton class. This ensures that only one copy of the data exists in the server's memory, regardless of how many request objects are being created and destroyed.

3. Synchronization and Race Conditions
Since the API is running on Tomcat, it is multi-threaded. This means multiple users could try to add or delete rooms at the exact same millisecond.

The Risk: If two requests try to modify a standard ArrayList at the same time, it can lead to a "Race Condition" where data gets corrupted or the server crashes.

The Solution: To prevent this, we use thread-safe structures like ConcurrentHashMap. This ensures that the server handles one modification at a time, keeping our in-memory data consistent and safe from concurrent errors.

### Q2) Why Hypermedia (HATEOAS) is advanced RESTful design

Hypermedia (HATEOAS) means responses include (links/navigation) so clients can discover available resources and actions dynamically.

**Benefits vs static documentation:**

- Clients can follow links instead of hardcoding URL paths.
- APIs can evolve with less client breakage (clients rely on discoverable links).
- It reduces coupling between client and server and improves long-term maintainability.

Part 2 – Report Answers

### Q3) Returning only IDs vs full room objects in a list

If `GET /rooms` returns "only room IDs", the response is smaller and cheaper to transmit (lower bandwidth) and can be faster for clients that only need a picker/list.

If `GET /rooms` returns the "full room objects", the client gets everything in one call (less round-trips) but the payload is larger, which increases bandwidth usage and client-side parsing costs.

A common compromise is to return a summary representation (e.g., id + name) or use pagination. For this SmartCampusAPI, returning full objects is acceptable for simplicity, but the trade-off is larger responses as the number of rooms grows.

### Q4) Is DELETE idempotent?

A DELETE request is **idempotent** if repeating the exact same request results in the same server state.

In this implementation:

- First `DELETE /rooms/{id}` removes the room (server state changes).
- Repeating the same DELETE again returns **404 Not Found** because the room is already gone and the server state remains unchanged.

Therefore, the operation is idempotent with respect to server state.

Part 3 – Report Answers

### Q5) Consequences of @Consumes(MediaType.APPLICATION_JSON)

Using `@Consumes(MediaType.APPLICATION_JSON)` on a `POST` endpoint tells JAX-RS that the method only accepts requests whose `Content-Type` is `application/json`.

If a client sends the payload with a different content type (e.g., `text/plain` or `application/xml`):

- The JAX-RS runtime will not select this method for handling the request body.
- The server will typically respond with **415 Unsupported Media Type** (or a similar client error) because it cannot find a suitable message body reader for that media type / the resource method refuses it.

This protects the API by enforcing a clear contract and preventing ambiguous parsing of request bodies.

### Q6) Why query parameters are better than putting the filter in the path

`GET /sensors?type=CO2` is generally preferred for filtering/search because:

- Filtering is optional and combinable (later you can add `?type=CO2&status=ACTIVE`, pagination, sorting, etc.).
- The canonical “resource collection” remains `/sensors`; query params refine the representation.
- It avoids creating many extra URL path variations and keeps routing simpler.

A path design like `/sensors/type/CO2` can work, but it is less flexible for multiple filters and tends to mix “resource identity” with “search criteria”.

Part 4 – Report Answers

### Q7) Benefits of the Sub-Resource Locator pattern

The Sub-Resource Locator pattern helps manage complexity in larger APIs by delegating nested resource logic to dedicated classes.

Benefits include:

- Separation of concerns: `SensorResource` remains focused on `/sensors` collection logic, while `SensorReadingResource` focuses on `/sensors/{id}/readings`.
- Better maintainability: nested logic can grow (validation, business rules, pagination) without turning one class into a large “god controller”.
- Reusability and testability: each resource class can be tested and evolved independently.
- Clearer routing/structure: the code mirrors the URI hierarchy (Sensor → Readings).

Part 5 – Report Answers (Error Handling & Observability)

### Q8) Why return 422 (Unprocessable Entity) instead of 404 in some cases?

A 404 Not Found means the requested URI/resource itself does not exist (e.g., `GET /rooms/XYZ` when there is no room with id `XYZ`).

A 422 Unprocessable Entity is useful when the target endpoint exists, and the request body is syntactically valid JSON, but a _semantic constraint_ fails. In this API the main example is creating a sensor with a `roomId` that does not exist:

- The client is correctly calling `POST /sensors` (endpoint exists)
- The body is valid JSON and matches the `Sensor` shape
- But `roomId` references a room that is missing, so the server cannot process the request as instructed

Using 422 communicates “your request structure is fine, but the linked/related entity makes it impossible to apply”. It is more precise than 404 for this scenario.

### Q9) Why is returning stack traces in API error responses risky?

Returning stack tracesncan leak sensitive implementation details such as:

- class/package names and library/framework versions
- internal file paths and configuration hints
- details of validation/business rules that help attackers probe the API

It also increases coupling: clients may start relying on internal error strings that can change. For these reasons, this API uses a generic 500 JSON response for unexpected errors and returns only safe, intentionally designed error messages for known/expected failures.

### Q10) Why use JAX-RS filters for logging instead of manual logging in each resource method?

Using a `ContainerRequestFilter` / `ContainerResponseFilter` is preferable because logging is a cross-cutting concern:

- Consistency: one filter produces a uniform log format (method, URI, status, timing) for every endpoint.
- Coverage: the response filter runs even when errors happen (including exceptions mapped by `ExceptionMapper`s), so failures are logged too.
- Less duplication: avoids repeating `System.out.println(...)` in every resource class/method.
- Maintainability: log behaviour can be changed in one place (e.g., add headers, correlation ids, or switch to a logger) without touching every endpoint.

In short, filters centralize a shared concern and keep resource classes focused on request handling/business logic.

## Sample curl commands

> Note: Update host/port depending on how you deploy the WAR.

```bash
# Discovery
curl -i http://localhost:8080/api/v1

# List rooms
curl -i http://localhost:8080/api/v1/rooms

# Create a room
curl -i -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"LIB-301\",\"name\":\"Library Quiet Study\",\"capacity\":40}"

# Get a room by id
curl -i http://localhost:8080/api/v1/rooms/LIB-301

# Delete a room
curl -i -X DELETE http://localhost:8080/api/v1/rooms/LIB-301

# Create a sensor (assumes room LIB-301 exists)
curl -i -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"TEMP-001\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":0,\"roomId\":\"LIB-301\"}"

# List sensors
curl -i http://localhost:8080/api/v1/sensors

# Filter sensors by type
curl -i "http://localhost:8080/api/v1/sensors?type=Temperature"

# Add a reading to a sensor
curl -i -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"R-001\",\"timestamp\":1710000000000,\"value\":23.5}"

# Get reading history
curl -i http://localhost:8080/api/v1/sensors/TEMP-001/readings
```
