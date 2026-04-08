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

## Endpoints (so far)

- `GET /api/v1` – Discovery endpoint (returns version info, contact info, and top-level resource links)
