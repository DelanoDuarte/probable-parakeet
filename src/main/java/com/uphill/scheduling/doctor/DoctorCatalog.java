package com.uphill.scheduling.doctor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public API of the doctor module: a read-only catalogue of bookable doctors.
 *
 * <p>Note this exposes the <em>roster</em>, not availability. Whether a doctor is
 * free at a given time is determined by the scheduling (appointment) module from
 * its own booking data — the doctor module deliberately knows nothing about
 * appointments, keeping the contexts decoupled.
 */
public interface DoctorCatalog {

    /** Active doctors of the given specialty, ordered deterministically. */
    List<DoctorSnapshot> findActiveBySpecialty(Specialty specialty);

    Optional<DoctorSnapshot> findById(UUID id);
}
