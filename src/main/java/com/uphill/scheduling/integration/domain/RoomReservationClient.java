package com.uphill.scheduling.integration.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Outbound port to the (external) room reservation / facilities system.
 *
 * @see DoctorCalendarClient for the rationale behind modelling these external
 *      systems as ports reacting to the {@code AppointmentBooked} event.
 */
public interface RoomReservationClient {

    /**
     * Reserves the given room for the slot in the external facilities system.
     *
     * @throws ExternalSystemException if the remote call fails.
     */
    void reserve(UUID roomId, String roomName, Instant start, Instant end);
}
