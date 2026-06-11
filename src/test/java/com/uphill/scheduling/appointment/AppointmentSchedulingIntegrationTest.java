package com.uphill.scheduling.appointment;

import com.uphill.scheduling.appointment.application.AppointmentBookingService;
import com.uphill.scheduling.appointment.application.AppointmentQueryService;
import com.uphill.scheduling.appointment.application.BookAppointmentCommand;
import com.uphill.scheduling.appointment.domain.Appointment;
import com.uphill.scheduling.appointment.domain.NoDoctorAvailableException;
import com.uphill.scheduling.doctor.Specialty;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end test of the booking use case against a real (H2) Spring context:
 * resource auto-assignment, persistence, admin listing, the overbooking guard,
 * and domain-event publication.
 *
 * <p>{@link RecordApplicationEvents} + {@link ApplicationEvents} captures the
 * {@link AppointmentBooked} event at publication time, proving the post-booking
 * seam fires without having to wait on the async listeners themselves.
 */
@SpringBootTest
@RecordApplicationEvents
class AppointmentSchedulingIntegrationTest {

    @Autowired
    AppointmentBookingService bookingService;

    @Autowired
    AppointmentQueryService queryService;

    @Autowired
    ApplicationEvents events;

    private static BookAppointmentCommand command(Specialty specialty, Instant start) {
        return new BookAppointmentCommand(
                "Maria Silva", "maria@example.pt",
                specialty, start, start.plus(30, ChronoUnit.MINUTES));
    }

    @Test
    void booksAppointmentAssigningDoctorAndRoomThenListsIt() {
        Instant start = Instant.parse("2026-07-01T09:00:00Z");

        Appointment booked = bookingService.book(command(Specialty.CARDIOLOGY, start));

        assertThat(booked.getDoctorId()).isNotNull();
        assertThat(booked.getDoctorName()).isNotBlank();
        assertThat(booked.getRoomId()).isNotNull();
        assertThat(booked.getRoomName()).isNotBlank();

        // visible to the admin listing API
        assertThat(queryService.findAll())
                .extracting(Appointment::getId)
                .contains(booked.getId());

        // the post-booking event was published exactly once
        assertThat(events.stream(AppointmentBooked.class).count()).isEqualTo(1L);
        AppointmentBooked event = events.stream(AppointmentBooked.class).findFirst().orElseThrow();
        assertThat(event.appointmentId()).isEqualTo(booked.getId());
        assertThat(event.patientEmail()).isEqualTo("maria@example.pt");
    }

    @Test
    void assignsDifferentDoctorsForConcurrentSlotThenRefusesWhenSpecialtyExhausted() {
        // Dermatology is seeded with exactly ONE doctor, so a second booking in the
        // same slot has no available doctor of that specialty and must be refused —
        // this is the "no overbooking of a doctor" rule at the application level.
        Instant start = Instant.parse("2026-07-02T10:00:00Z");

        Appointment first = bookingService.book(command(Specialty.DERMATOLOGY, start));
        assertThat(first.getId()).isNotNull();

        assertThatThrownBy(() -> bookingService.book(command(Specialty.DERMATOLOGY, start)))
                .isInstanceOf(NoDoctorAvailableException.class);
    }

    @Test
    void sameSpecialtySameSlotUsesTheSecondDoctorWhenOneRemains() {
        // Cardiology has two doctors: two bookings in the same slot should both
        // succeed, each getting a distinct doctor (and a distinct room).
        Instant start = Instant.parse("2026-07-03T11:00:00Z");

        Appointment a = bookingService.book(command(Specialty.CARDIOLOGY, start));
        Appointment b = bookingService.book(command(Specialty.CARDIOLOGY, start));

        assertThat(a.getDoctorId()).isNotEqualTo(b.getDoctorId());
        assertThat(a.getRoomId()).isNotEqualTo(b.getRoomId());
    }
}
