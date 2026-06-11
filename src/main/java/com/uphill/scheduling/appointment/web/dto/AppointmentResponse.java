package com.uphill.scheduling.appointment.web.dto;

import com.uphill.scheduling.appointment.domain.Appointment;
import com.uphill.scheduling.appointment.domain.AppointmentStatus;
import com.uphill.scheduling.doctor.Specialty;

import java.time.Instant;
import java.util.UUID;

/** Representation of a booked appointment returned to API clients. */
public record AppointmentResponse(
        UUID id,
        String patientName,
        String patientEmail,
        Specialty specialty,
        UUID doctorId,
        String doctorName,
        UUID roomId,
        String roomName,
        Instant start,
        Instant end,
        AppointmentStatus status) {

    public static AppointmentResponse from(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getPatient().name(),
                a.getPatient().email(),
                a.getSpecialty(),
                a.getDoctorId(),
                a.getDoctorName(),
                a.getRoomId(),
                a.getRoomName(),
                a.getSlot().start(),
                a.getSlot().end(),
                a.getStatus());
    }
}
