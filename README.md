# Appointment Scheduling Service

Medical appointment scheduling API built as a modular monolith with DDD and Spring Modulith. A patient submits their details, a desired time slot, and a required specialty; the system assigns an available doctor and room, confirms the booking, and triggers post-booking side effects asynchronously.

---

## Tech stack

| Concern       | Choice |
|---------------|--------|
| Language      | Java 21 |
| Framework     | Spring Boot 4 · Spring Framework 7 · Jakarta EE 11 |
| Modularity    | Spring Modulith 2 |
| Persistence   | Spring Data JPA · H2 (PostgreSQL compatibility mode) |
| API docs      | springdoc-openapi (Swagger UI) |
| Observability | Spring Boot Actuator · Micrometer / Prometheus |
| Mail          | Spring Mail — log transport by default, SMTP via config |
| Build         | Gradle (Kotlin DSL) |
| Packaging     | Multi-stage Docker image · Docker Compose · Kubernetes manifests |

---

## Running

### Docker Compose

```bash
docker compose up --build
```

Starts the service and MailHog (fake SMTP). No local JDK or Gradle needed.

| Endpoint | URL |
|----------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| MailHog inbox | http://localhost:8025 |
| H2 console | http://localhost:8080/h2-console |
| Actuator health | http://localhost:8080/actuator/health |

### Gradle

```bash
./gradlew bootRun
```

No SMTP needed — confirmation emails are written to the log. Requires JDK 21; the Gradle wrapper handles the rest.

Service runs on `http://localhost:8080`.

### Tests

```bash
./gradlew test
```

Covers domain unit tests, web-layer slice tests, and a full `@SpringBootTest` integration test including module boundary verification.

---

## API

Base path: `/api/v1/appointments`

### Book an appointment

```bash
curl -X POST http://localhost:8080/api/v1/appointments \
  -H 'Content-Type: application/json' \
  -d '{
        "patientName":  "Maria Silva",
        "patientEmail": "maria@example.pt",
        "specialty":    "CARDIOLOGY",
        "start":        "2026-07-01T09:00:00Z",
        "end":          "2026-07-01T09:30:00Z"
      }'
```

Returns `201 Created` with the assigned doctor and room.
Returns `409 Conflict` when no doctor of that specialty or no room is free, or a concurrent request claimed the same resource.
Returns `400 Bad Request` for validation failures.
All errors follow RFC 7807 (`application/problem+json`).

Available specialties: `GENERAL_PRACTICE` · `CARDIOLOGY` · `DERMATOLOGY` · `PEDIATRICS` · `ORTHOPEDICS`

Seven doctors and five rooms are seeded at startup.

### List appointments

```bash
curl http://localhost:8080/api/v1/appointments
```

Returns `200 OK` with all confirmed appointments.

---

## Architecture

### Modules

```
com.uphill.scheduling
├── appointment     booking use case, Appointment aggregate, REST API
├── doctor          doctor registry (public: DoctorRegistry, DoctorView, Specialty)
├── room            room registry (public: RoomRegistry, RoomView)
├── notification    confirmation email — reacts to AppointmentBooked
└── integration     external calendar + room reservation — react to AppointmentBooked
```

Module boundaries are enforced at build time by `ModularityTests`. Cross-module access is limited to each module's public API package; internals are inaccessible.

### Booking flow

1. `POST /api/v1/appointments` → `BookAppointmentUseCase`
2. Read busy doctor IDs for the slot; pick first free doctor of requested specialty
3. Read busy room IDs for the slot; pick first free room
4. Build and persist the `Appointment` aggregate
5. `AppointmentBooked` domain event registered on the aggregate; published by Spring Data on `save()`
6. Three independent `@ApplicationModuleListener`s fire after commit on separate threads: email · calendar update · room reservation

### Concurrency

The application-level availability check is optimistic. The authoritative guard is two database unique constraints:

```
uq_doctor_slot (doctor_id, start_at)
uq_room_slot   (room_id,   start_at)
```

When two concurrent requests resolve to the same resource, exactly one insert wins. The other retries automatically — the use case re-reads availability in a fresh transaction and resolves to a different free resource. After three failed attempts, `409 Conflict` is returned.

### Reliable side effects

Post-booking listeners use Spring Modulith's Event Publication Registry. Each listener's publication is persisted before delivery and marked complete only on success. Incomplete publications are resubmitted on restart and periodically during runtime (every 2 minutes) so a temporary external system outage does not lose events.

---

## Production notes

- **Database** — replace H2 with PostgreSQL; manage schema with Flyway/Liquibase; switch `ddl-auto` to `validate`.
- **Mail** — set `SCHEDULING_NOTIFICATIONS_TRANSPORT=smtp` and configure `SPRING_MAIL_*` for a real relay (SES, SendGrid).
- **External systems** — the `integration` module's logging clients are the seams for real HTTP implementations; swap the adapters, keep the ports.
- **Observability** — Prometheus at `/actuator/prometheus`; liveness and readiness probes configured in the Kubernetes manifests.

### Kubernetes

The image must be built locally before applying the manifests. The deployment uses `imagePullPolicy: Never` — remove that line if you want to pull from a registry instead.

**1 — Build the image**
```bash
docker build -t uphill/appointment-scheduling:latest .
```

**2 — Load into the cluster** (skip for Docker Desktop K8s — image is available automatically)

minikube:
```bash
minikube image load uphill/appointment-scheduling:latest
```

kind:
```bash
kind load docker-image uphill/appointment-scheduling:latest
```

**3 — Apply manifests**
```bash
kubectl apply -f k8s/
```

**4 — Wait for pods**
```bash
kubectl rollout status deployment/appointment-scheduling -n scheduling
```

**5 — Port-forward and access**
```bash
kubectl port-forward -n scheduling svc/appointment-scheduling 8080:80
```

API and Swagger UI available at `http://localhost:8080`.

**Useful commands**
```bash
kubectl get pods -n scheduling
kubectl logs -n scheduling -l app=appointment-scheduling --follow
kubectl get hpa -n scheduling
```
