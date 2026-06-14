package com.uphill.scheduling.doctor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public API of the doctor module: a read-only roster of bookable doctors.
 *
 * <p>Exposes the roster, not availability. Whether a doctor is free at a given
 * time is determined by the scheduling (appointment) module from its own booking
 * data — the doctor module deliberately knows nothing about appointments.
 */
public interface DoctorRegistry {

    /** Active doctors of the given specialty, ordered deterministically. */
    List<DoctorView> findActiveBySpecialty(Specialty specialty);

    Optional<DoctorView> findById(UUID id);
}
