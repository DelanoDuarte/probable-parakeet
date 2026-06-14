package com.uphill.scheduling.appointment.application;

import com.uphill.scheduling.appointment.domain.Appointment;
import com.uphill.scheduling.appointment.domain.exceptions.SlotAlreadyTakenException;
import org.springframework.stereotype.Service;

/**
 * Use case for booking an appointment.
 *
 * <p>Resource resolution strategy: read doctors/rooms busy in the requested
 * slot, pick the first free one, and persist. The database unique constraints
 * ({@code uq_doctor_slot}, {@code uq_room_slot}) are the authoritative guard
 * against double-booking. When two requests resolve to the same free resource
 * concurrently, exactly one insert wins; the other receives a
 * {@link SlotAlreadyTakenException} and is retried — each retry re-reads
 * availability with a fresh transaction and may resolve to a different resource.
 */
@Service
public class BookAppointmentUseCase {

    private static final int MAX_ATTEMPTS = 3;

    private final BookingTransaction bookingTransaction;

    public BookAppointmentUseCase(BookingTransaction bookingTransaction) {
        this.bookingTransaction = bookingTransaction;
    }

    public Appointment book(BookAppointmentCommand command) {
        SlotAlreadyTakenException lastException = null;
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                return bookingTransaction.execute(command);
            } catch (SlotAlreadyTakenException ex) {
                lastException = ex;
            }
        }
        throw lastException;
    }
}
