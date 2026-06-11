package com.uphill.scheduling.appointment.domain;

import com.uphill.scheduling.doctor.Specialty;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppointmentTest {

    private static final TimeSlot SLOT = new TimeSlot(
            Instant.parse("2026-07-01T09:00:00Z"),
            Instant.parse("2026-07-01T09:30:00Z"));
    private static final PatientInfo PATIENT = new PatientInfo("Maria Silva", "maria@example.pt");

    @Test
    void bookCreatesConfirmedAppointment() {
        UUID doctorId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();

        Appointment appointment = Appointment.book(
                PATIENT, Specialty.CARDIOLOGY, SLOT,
                doctorId, "Dr. Ana Costa", roomId, "Room 101");

        assertThat(appointment.getId()).isNotNull();
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(appointment.getDoctorId()).isEqualTo(doctorId);
        assertThat(appointment.getRoomName()).isEqualTo("Room 101");
        assertThat(appointment.getCreatedAt()).isNotNull();
    }

    @Test
    void bookRejectsMissingCollaborators() {
        assertThatThrownBy(() -> Appointment.book(
                PATIENT, Specialty.CARDIOLOGY, SLOT,
                null, "Dr. Ana Costa", UUID.randomUUID(), "Room 101"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("doctorId");
    }

    @Test
    void cancelTransitionsToCancelled() {
        Appointment appointment = Appointment.book(
                PATIENT, Specialty.CARDIOLOGY, SLOT,
                UUID.randomUUID(), "Dr. Ana Costa", UUID.randomUUID(), "Room 101");

        appointment.cancel();
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    void cancelIsRejectedWhenAlreadyCancelled() {
        Appointment appointment = Appointment.book(
                PATIENT, Specialty.CARDIOLOGY, SLOT,
                UUID.randomUUID(), "Dr. Ana Costa", UUID.randomUUID(), "Room 101");
        appointment.cancel();

        assertThatThrownBy(appointment::cancel)
                .isInstanceOf(IllegalStateException.class);
    }
}
