package com.uphill.scheduling.appointment.domain.exceptions;

import com.uphill.scheduling.appointment.domain.TimeSlot;

/**
 * Raised when a concurrent booking won the race for the same doctor/room slot,
 * surfacing as a database uniqueness violation. Distinct from the "no resource
 * available" cases because here a resource <em>was</em> chosen but lost the race.
 */
public class SlotAlreadyTakenException extends RuntimeException {

    public SlotAlreadyTakenException(TimeSlot slot, Throwable cause) {
        super("The selected doctor or room was just taken for " + slot, cause);
    }
}
