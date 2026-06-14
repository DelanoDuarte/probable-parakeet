package com.uphill.scheduling.integration.application;

import com.uphill.scheduling.appointment.AppointmentBooked;
import com.uphill.scheduling.integration.domain.DoctorCalendarClient;
import com.uphill.scheduling.integration.domain.RoomReservationClient;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Drives the two external-system side effects of a booking: updating the doctor's
 * calendar and reserving the room.
 *
 * <p>These are modelled as <em>two separate</em> {@link ApplicationModuleListener}
 * methods on purpose. Each {@code @ApplicationModuleListener} gets its own entry in
 * the event-publication registry, so the calendar update and the room reservation
 * succeed, fail, and retry <em>independently</em>: if the calendar API is down but
 * the room API is up, only the calendar publication stays incomplete and gets
 * resubmitted — the room isn't reserved twice. The e-mail (in the notification
 * module) is a third such listener, for the same reason.
 */
@Component
class PostBookingIntegrationListener {

    private final DoctorCalendarClient doctorCalendarClient;
    private final RoomReservationClient roomReservationClient;

    PostBookingIntegrationListener(DoctorCalendarClient doctorCalendarClient,
                                   RoomReservationClient roomReservationClient) {
        this.doctorCalendarClient = doctorCalendarClient;
        this.roomReservationClient = roomReservationClient;
    }

    @ApplicationModuleListener
    void updateDoctorCalendar(AppointmentBooked event) {
        doctorCalendarClient.blockSlot(
                event.doctorId(), event.doctorName(), event.start(), event.end());
    }

    @ApplicationModuleListener
    void reserveRoom(AppointmentBooked event) {
        roomReservationClient.reserve(
                event.roomId(), event.roomName(), event.start(), event.end());
    }
}
