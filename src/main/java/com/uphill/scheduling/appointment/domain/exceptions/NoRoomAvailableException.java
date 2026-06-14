package com.uphill.scheduling.appointment.domain.exceptions;

import com.uphill.scheduling.appointment.domain.TimeSlot;

/** No consultation room is free for the requested slot. */
public class NoRoomAvailableException extends RuntimeException {

    public NoRoomAvailableException(TimeSlot slot) {
        super("No room is available for " + slot);
    }
}
