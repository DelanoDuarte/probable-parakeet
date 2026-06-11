package com.uphill.scheduling.doctor;

import java.util.UUID;

/**
 * Immutable read-model of a doctor, exposed to other modules (e.g. the
 * appointment module) so they never touch the doctor aggregate directly.
 */
public record DoctorSnapshot(UUID id, String fullName, Specialty specialty) {
}
