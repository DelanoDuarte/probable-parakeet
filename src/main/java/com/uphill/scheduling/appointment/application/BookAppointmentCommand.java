package com.uphill.scheduling.appointment.application;

import com.uphill.scheduling.doctor.Specialty;

import java.time.Instant;

/** Use-case input for booking an appointment. */
public record BookAppointmentCommand(
        String patientName,
        String patientEmail,
        Specialty specialty,
        Instant start,
        Instant end) {
}
