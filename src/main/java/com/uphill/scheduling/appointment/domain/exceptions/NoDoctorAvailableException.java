package com.uphill.scheduling.appointment.domain.exceptions;

import com.uphill.scheduling.appointment.domain.TimeSlot;
import com.uphill.scheduling.doctor.Specialty;

/** No doctor of the requested specialty is free for the requested slot. */
public class NoDoctorAvailableException extends RuntimeException {

    public NoDoctorAvailableException(Specialty specialty, TimeSlot slot) {
        super("No %s doctor is available for %s".formatted(specialty, slot));
    }
}
