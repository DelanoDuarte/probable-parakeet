package com.uphill.scheduling.integration.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Outbound port to the (external) doctor calendar system.
 *
 * <p>In a real deployment this would be an HTTP/SOAP client against the hospital's
 * scheduling platform. Here it is an interface with a logging adapter, but the
 * seam is genuine: the appointment module never knows this exists — it merely
 * publishes {@code AppointmentBooked}, and this module reacts.
 */
public interface DoctorCalendarClient {

    /**
     * Blocks the given slot on the doctor's external calendar.
     *
     * @throws ExternalSystemException if the remote call fails; the listener relies
     *         on this so Spring Modulith can retry the persisted event publication.
     */
    void blockSlot(UUID doctorId, String doctorName, Instant start, Instant end);
}
