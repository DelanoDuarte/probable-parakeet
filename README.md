# Appointment Scheduling Service

A Spring Boot service for scheduling medical appointments in Portugal. A patient
submits their details, a desired time slot, and a required specialty; the system
automatically assigns an available doctor of that specialty and an available room,
confirms the booking, and triggers the post-booking side effects (doctor-calendar
update, room reservation, confirmation e-mail).

Built as a **modular monolith** with Domain-Driven Design, rich domain models, and
Spring Modulith enforcing the module boundaries.

---

## Tech stack

| Concern            | Choice                                                              |
|--------------------|--------------------------------------------------------------------|
| Language / runtime | Java 21 (LTS)                                                       |
| Framework          | Spring Boot 4.0.x (Spring Framework 7, Jakarta EE 11)              |
| Modularity         | Spring Modulith 2.0.x                                               |
| Persistence        | Spring Data JPA + H2 (in-memory, PostgreSQL compatibility mode)     |
| API docs           | springdoc-openapi (Swagger UI)                                      |
| Observability      | Spring Boot Actuator + Micrometer / Prometheus                      |
| Mail               | Spring Mail (`JavaMailSender`); MailHog for local fake SMTP         |
| Build              | Gradle (Kotlin DSL), with wrapper                                   |
| Packaging          | Multi-stage Docker image; Docker Compose; Kubernetes manifests      |

> **Why H2?** The challenge leaves the data layer open and asks for something a
> teammate can run instantly. H2 runs in-memory with zero setup. It is configured
> in PostgreSQL-compatibility mode and accessed only through JPA, so moving to a
> real Postgres is a dependency swap plus a URL — see *Production notes* below.

---

## Architecture

### Modules

Each top-level package under `com.uphill.scheduling` is a Spring Modulith module.
Cross-module access is restricted to each module's **public API** (its base
package); everything under `domain`, `application`, `infrastructure`, and `web`
is internal. `ModularityTests` fails the build if this is violated or if a cycle
is introduced.

```
com.uphill.scheduling
├── appointment     core scheduling context (the aggregate + booking use case)
│   ├── (api)       AppointmentBooked event
│   ├── domain      Appointment aggregate, TimeSlot, PatientInfo, repository port
│   ├── application AppointmentBookingService, AppointmentQueryService
│   ├── infrastructure  JPA repository + adapter
│   └── web         REST API interface, controller, DTOs, exception handler
├── doctor          doctor roster + availability (public: DoctorCatalog, Specialty, DoctorSnapshot)
├── room            room roster + availability (public: RoomCatalog, RoomSnapshot)
├── notification    confirmation e-mail (listens to AppointmentBooked)
└── integration     external systems: doctor calendar + room reservation (listen to AppointmentBooked)
```

Dependency direction (acyclic):

```
appointment ──▶ doctor
appointment ──▶ room
notification ──▶ appointment   (event only)
integration  ──▶ appointment   (event only)
```

`appointment` knows nothing about `notification` or `integration`: it just
publishes a domain event. The side-effect modules react. This is what keeps the
booking logic clean and the side effects independently evolvable.

### Booking flow

1. `POST /api/v1/appointments` → `AppointmentController` → `AppointmentBookingService`.
2. The service reads the doctors busy in the slot and picks the first free doctor
   of the requested specialty (`NoDoctorAvailableException` if none); same for rooms.
3. It builds the `Appointment` aggregate (a confirmed booking) and persists it.
4. The DB unique constraints are the authoritative no-overbooking guard; a lost
   race surfaces as `SlotAlreadyTakenException` → HTTP 409.
5. On commit, an `AppointmentBooked` event is published.
6. Three independent `@ApplicationModuleListener`s react **after commit**, each on
   its own thread/transaction and its own persisted publication:
   - send the confirmation e-mail,
   - update the doctor's (external) calendar,
   - reserve the room in the (external) facilities system.

### No overbooking — how it's actually enforced

The application-level "pick a free doctor/room" check is necessary but **not**
sufficient under concurrency: two simultaneous requests can both read the same
resource as free. The real guarantee is at the database:

```
uq_doctor_slot (doctor_id, start_at)
uq_room_slot   (room_id,   start_at)
```

Exactly one of two racing inserts wins; the other hits a
`DataIntegrityViolationException`, which the service translates to
`SlotAlreadyTakenException` (HTTP 409). Correctness does not depend on locking or
on the order in which requests arrive.

**Known limitation (documented honestly):** a unique index on `start_at` assumes
appointments are aligned to a fixed slot grid (same start instant = same slot). It
does **not** prevent *partially* overlapping intervals with different start times.
For arbitrary intervals the correct tool is a PostgreSQL **exclusion constraint**
using a `tstzrange` and the `&&` operator over a GiST index — H2 cannot express
that, which is one more reason production runs on Postgres. Also note the unique
constraint counts cancelled rows; a production schema would use a partial index
(`WHERE status = 'CONFIRMED'`) so a cancelled slot can be rebooked.

### Reliable side effects

Post-booking actions use Spring Modulith's **Event Publication Registry**
(`spring-modulith-starter-jpa`). Each listener's publication is persisted before
delivery and only marked complete on success. If the process dies mid-delivery,
the publication remains incomplete and is **resubmitted on restart**
(`republish-outstanding-events-on-restart: true`) — at-least-once delivery instead
of a silently lost e-mail or un-reserved room. Because the three listeners are
separate publications, one failing (e.g. the calendar API is down) does not block
or duplicate the others.

---

## Running it

### Option A — Docker Compose (recommended; no local JDK/Gradle needed)

```bash
docker compose up --build
```

This starts the service and MailHog. Compose sets the SMTP transport so real
`JavaMailSender` calls are made against MailHog.

- API / Swagger UI: http://localhost:8080/swagger-ui.html
- MailHog inbox (see confirmation e-mails): http://localhost:8025
- H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:scheduling`, user `sa`, empty password)
- Actuator health: http://localhost:8080/actuator/health

### Option B — Gradle

```bash
./gradlew bootRun
```

Runs with the default **log** e-mail transport (no SMTP needed) — the confirmation
e-mail is written to the application log. The wrapper fetches the pinned Gradle
version automatically, so no local Gradle install is required (just a JDK 21).

### Tests

```bash
./gradlew test
```

Includes the Modulith boundary verification, domain unit tests, and a full
`@SpringBootTest` booking/overbooking integration test.

---

## API

Base path: `/api/v1/appointments`

### Create an appointment

```bash
curl -X POST http://localhost:8080/api/v1/appointments \
  -H 'Content-Type: application/json' \
  -d '{
        "patientName": "Maria Silva",
        "patientEmail": "maria@example.pt",
        "specialty": "CARDIOLOGY",
        "start": "2026-07-01T09:00:00Z",
        "end":   "2026-07-01T09:30:00Z"
      }'
```

`201 Created` with the assigned doctor and room. `409 Conflict` when no doctor of
that specialty / no room is free for the slot, or when the slot was taken by a
concurrent request; `400 Bad Request` for invalid input. Errors are returned as
RFC 7807 `application/problem+json`.

### List all appointments (admin)

```bash
curl http://localhost:8080/api/v1/appointments
```

Specialties: `GENERAL_PRACTICE`, `CARDIOLOGY`, `DERMATOLOGY`, `PEDIATRICS`,
`ORTHOPEDICS`. Seven doctors and five rooms are seeded at startup.

---

## Scaling & throughput

The brief calls for thousands of requests/hour — modest, with headroom by design:

- **Virtual threads** (`spring.threads.virtual.enabled=true`): the workload is
  blocking JPA, so virtual threads absorb concurrency without a large platform
  thread pool.
- **Stateless** service: the Deployment runs 2 replicas behind a Service, with an
  HPA scaling to 6 on CPU. Reliable delivery survives pod restarts via the
  persisted event registry.
- **Authoritative DB constraints** mean horizontal scaling needs no distributed
  lock — the database arbitrates contention.

---

## Production notes

- **Database:** swap H2 for PostgreSQL (driver + `spring.datasource.*`), switch
  `ddl-auto` to `validate`, and manage schema with Flyway/Liquibase. Replace the
  `start_at` unique constraints with a `tstzrange` GiST exclusion constraint for
  true interval-overlap protection, scoped to confirmed appointments.
- **Mail:** point `SCHEDULING_NOTIFICATIONS_TRANSPORT=smtp` at a real relay
  (SES/SendGrid SMTP) via `SPRING_MAIL_*`. No code change — same `EmailSender` port.
- **External systems:** the `integration` module's logging clients are the seams
  for the real doctor-calendar and room-reservation HTTP clients; swap the adapter
  implementations, keep the ports and listeners.
- **Observability:** Prometheus scrape at `/actuator/prometheus`; liveness/readiness
  probes wired in the K8s deployment.

## Kubernetes

```bash
kubectl apply -f k8s/
```

Namespace, ConfigMap (log transport in-cluster), Deployment (2 replicas, Actuator
liveness/readiness probes, resource requests/limits), ClusterIP Service, and HPA.
