# Smart Campus API (5COSC022W Coursework)

## Overview

This project implements the **"Smart Campus" Sensor & Room Management API** using **JAX-RS (Jersey)**.

- **API base path:** `/api/v1`
- **No database:** uses in-memory data structures (`ConcurrentHashMap`, lists)
- **Framework constraint:** **No Spring / Spring Boot**

## Build

### Prerequisites

- Java JDK (compatible with the Maven compiler configuration)

### Build the WAR

```
.tools/apache-maven-3.9.6/bin/mvn clean install
```

The build output will be created at `target/smart-campus-api.war`.

## Part 1 – Report Answers

### Q1) Default lifecycle of a JAX-RS Resource class

By default, a JAX-RS resource class is typically treated as **per-request** by the runtime (i.e., a new instance may be created for each incoming HTTP request). While some implementations can be configured differently, designing as if resources are request-scoped is the safe assumption.

**Impact on in-memory data structures:**

- If resources are created per request, instance fields inside a resource class are **not a reliable place** to store shared state.
- Shared state must be kept in a separate shared component (e.g., a singleton), and because multiple requests can be processed concurrently, the shared data must be protected from race conditions.

In this project, shared state is centralized in a `DataStore` singleton backed by thread-safe data structures (`ConcurrentHashMap`) to prevent data loss and concurrent modification issues.

### Q2) Why Hypermedia (HATEOAS) is advanced RESTful design

Hypermedia (HATEOAS) means responses include **links / navigation** so clients can discover available resources and actions dynamically.

**Benefits vs static documentation:**

- Clients can follow links instead of hardcoding URL paths.
- APIs can evolve with less client breakage (clients rely on discoverable links).
- It reduces coupling between client and server and improves long-term maintainability.

## Part 2 – Report Answers

### Q3) Returning only IDs vs full room objects in a list

If `GET /rooms` returns **only room IDs**, the response is smaller and cheaper to transmit (lower bandwidth) and can be faster for clients that only need a picker/list.

If `GET /rooms` returns the **full room objects**, the client gets everything in one call (less round-trips) but the payload is larger, which increases bandwidth usage and client-side parsing costs.

A common compromise is to return a summary representation (e.g., id + name) or use pagination. In this coursework, returning full objects is acceptable for simplicity, but the trade-off is larger responses as the number of rooms grows.

### Q4) Is DELETE idempotent?

A DELETE request is **idempotent** if repeating the exact same request results in the same server state.

In this implementation:

- First `DELETE /rooms/{id}` removes the room (server state changes).
- Repeating the same DELETE again returns **404 Not Found** (because the room is already gone) and the server state remains unchanged.

Therefore, the operation is **idempotent** with respect to server state (after the first call, repeated calls do not keep changing state).

## Part 3 – Report Answers

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

## Part 4 – Report Answers

### Q7) Benefits of the Sub-Resource Locator pattern

The Sub-Resource Locator pattern helps manage complexity in larger APIs by **delegating nested resource logic** to dedicated classes.

Benefits include:

- **Separation of concerns:** `SensorResource` remains focused on `/sensors` collection logic, while `SensorReadingResource` focuses on `/sensors/{id}/readings`.
- **Better maintainability:** nested logic can grow (validation, business rules, pagination) without turning one class into a large “god controller”.
- **Reusability and testability:** each resource class can be tested and evolved independently.
- **Clearer routing/structure:** the code mirrors the URI hierarchy (Sensor → Readings).

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
```
