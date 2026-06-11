package com.uphill.scheduling.appointment;

import com.uphill.scheduling.doctor.Specialty;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published once an appointment has been successfully persisted.
 * It is the appointment module's public contract for "something was booked", and
 * the seam along which all post-booking side effects hang (e-mail confirmation,
 * external doctor-calendar update, external room reservation).
 *
 * <p>It carries only primitive/standard types — never internal domain objects —
 * so listeners in other modules don't couple to the aggregate.
 */
public record AppointmentBooked(
        UUID appointmentId,
        String patientName,
        String patientEmail,
        Specialty specialty,
        UUID doctorId,
        String doctorName,
        UUID roomId,
        String roomName,
        Instant start,
        Instant end) {
}
