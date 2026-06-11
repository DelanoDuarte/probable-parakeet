package com.uphill.scheduling.appointment.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Domain repository (port) for appointments. Deliberately free of any Spring or
 * JPA types so the domain stays persistence-ignorant; the adapter lives in
 * {@code infrastructure}.
 */
public interface AppointmentRepository {

    Appointment save(Appointment appointment);

    Optional<Appointment> findById(UUID id);

    List<Appointment> findAll();

    /** Ids of doctors with a CONFIRMED appointment overlapping {@code [start, end)}. */
    Set<UUID> findBusyDoctorIds(Instant start, Instant end);

    /** Ids of rooms with a CONFIRMED appointment overlapping {@code [start, end)}. */
    Set<UUID> findBusyRoomIds(Instant start, Instant end);
}
