package com.uphill.scheduling.appointment.domain;

/** No consultation room is free for the requested slot. */
public class NoRoomAvailableException extends RuntimeException {

    public NoRoomAvailableException(TimeSlot slot) {
        super("No room is available for " + slot);
    }
}
