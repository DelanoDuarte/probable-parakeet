package com.uphill.scheduling.doctor;

import java.util.UUID;

/** Immutable read-model of a doctor, exposed to other modules. */
public record DoctorView(UUID id, String fullName, Specialty specialty) {
}
