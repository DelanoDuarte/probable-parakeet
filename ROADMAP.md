# Roadmap

Improvements and expansions deferred from the initial implementation. Ordered roughly by impact and dependency.

---

## Domain model

### Doctor availability calendar

The current implementation derives availability implicitly: a doctor is considered free if no CONFIRMED appointment overlaps the requested slot. This covers the challenge requirement but has a meaningful gap — a doctor might have no bookings on a Saturday afternoon yet simply not be scheduled to work.

A proper availability model requires explicit scheduling:

- `DoctorAvailability` aggregate: recurring weekly schedules (working hours per day) and explicit exception blocks (vacation, on-call, days off)
- Management API to set and update availability per doctor
- `BookingTransaction` would check availability in two steps:
  1. Doctor is scheduled to work during the slot (`doctor_availability` table)
  2. Doctor has no existing confirmed appointment in that slot (current check)

This also changes the role of `DoctorCalendarClient` in the `integration` module. Currently it only writes (blocks a slot after booking). With an explicit calendar it would also be read — or replaced entirely by the internal availability model, depending on whether the external calendar is the source of truth.

Dependency: benefits from doctor multi-specialty and the Specialty entity migration, since availability rules may differ per specialty.

---

### Doctor multi-specialty

A doctor currently holds a single `Specialty`. In practice a physician can be qualified in multiple specialties (e.g. Internal Medicine + Cardiology). The correct model is `Set<Specialty>` on `Doctor`, backed by a `doctor_specialties` join table.

Impact on auto-assignment: a broader specialty match means fewer `NoDoctorAvailableException`s under load — the system resolves bookings across more of the doctor pool before exhausting any specialty.

The factory method would need a guard:
```java
if (specialties == null || specialties.isEmpty())
    throw new IllegalArgumentException("doctor must have at least one specialty");
```

Dependency: best combined with the Specialty entity migration below.

---

### Specialty as a managed entity

`Specialty` is currently a Java enum. This is correct for a fixed, code-owned set, but breaks as soon as any of the following is needed:

- **Adding a specialty without a redeploy** — a new department (e.g. Nephrology) should be a DB insert, not a code change.
- **External code mappings** — HL7 FHIR, insurance APIs, and national health registries use their own specialty coding systems (SNOMED CT, CMS taxonomy). A DB entity carries `fhir_code`, `cms_code`, and similar mapping columns without touching the domain.
- **Specialty-level metadata** — billing codes, default session duration, required equipment type, insurance coverage rules.
- **Multi-tenancy** — different clinics have different specialty structures; a table can be scoped per tenant.

Migration path: introduce a `specialty` module with a `Specialty` aggregate and its own registry interface. The `Doctor` module references `Specialty` by ID across the module boundary, the same way `Appointment` references `Doctor` and `Room`.

---

## Persistence

### Interval-overlap constraint (PostgreSQL)

The current unique constraints on `(doctor_id, start_at)` and `(room_id, start_at)` prevent exact-start duplicates but do not catch partially overlapping intervals (two appointments that start at different times but overlap in duration).

The correct tool in PostgreSQL is an exclusion constraint over a range type:

```sql
ALTER TABLE appointments
  ADD CONSTRAINT no_doctor_overlap
  EXCLUDE USING GIST (
    doctor_id WITH =,
    tstzrange(start_at, end_at, '[)') WITH &&
  )
  WHERE (status = 'CONFIRMED');
```

Same pattern for rooms. H2 cannot express this; it is a production-only migration.

### Partial index for confirmed appointments

The current unique constraints count cancelled rows. A slot cancelled and then re-booked at the same time by the same doctor fails the constraint.

Fix: partial index scoped to confirmed appointments:

```sql
CREATE UNIQUE INDEX uq_doctor_slot ON appointments (doctor_id, start_at)
  WHERE status = 'CONFIRMED';

CREATE UNIQUE INDEX uq_room_slot ON appointments (room_id, start_at)
  WHERE status = 'CONFIRMED';
```

### Schema migration tooling

Replace `ddl-auto: create-drop` with Flyway or Liquibase. All schema changes — including the specialty table, doctor_specialties join table, exclusion constraints, and partial indexes — become versioned, auditable migrations.

---

## Reliability

### Circuit breaker on external clients

`DoctorCalendarClient` and `RoomReservationClient` currently have no fault isolation. If an external system is slow, its listener thread hangs for the full HTTP timeout, which under sustained failure can exhaust the async thread pool.

Add a Resilience4j `@CircuitBreaker` to each client implementation. When the circuit opens, the listener throws immediately, the Event Publication Registry retains the entry as OPEN, and the periodic republication scheduler retries when the circuit closes.

### Exponential backoff for event republication

The current republication scheduler retries with a flat 2-minute interval. A recovering external system gets retried at the same rate regardless of how long it has been down. Exponential backoff (2 min → 4 → 8 → cap at 1 hour) reduces pressure during recovery and avoids retry storms.

### Dead-letter alerting

Publications that remain OPEN beyond a threshold (e.g. 1 hour) should trigger an alert. The Spring Modulith actuator endpoint (`/actuator/modulith`) exposes incomplete publications; a scheduled check against that data can emit a metric or log at ERROR level for ops to act on.

---

## Domain expansion

### Patient module

`PatientInfo` is currently a minimal embedded value object (`name`, `email`) inside `Appointment`. It carries just enough to send the confirmation email. A full patient registry would enable:

- Appointment history per patient
- Cancellation / rescheduling flows
- Duplicate-booking detection (same patient, overlapping slots)
- Integration with national patient identification systems

This would become its own module (`patient/`) with a `Patient` aggregate, referenced from `Appointment` by ID — the same cross-boundary-by-identity pattern already used for `Doctor` and `Room`.

### Cancellation and rescheduling

`Appointment.cancel()` exists on the aggregate but there is no API endpoint or use case for it. A full flow would include:

- `DELETE /api/v1/appointments/{id}` or `PATCH` to cancel
- Corresponding `AppointmentCancelled` domain event
- Listeners to release the doctor's calendar slot and the room reservation in external systems
- Business rule: cancellations within X hours of the appointment may be rejected or flagged

### Authentication and authorisation

No security layer exists. A production service would need at minimum:

- Identity: OAuth2 / OIDC (e.g. Keycloak, Auth0)
- Role separation: patients book their own appointments; staff list all; admins manage doctors and rooms
- Patient-scoped data access: a patient must not see other patients' appointments
