package com.uphill.scheduling.appointment.application;

import com.uphill.scheduling.appointment.AppointmentBooked;
import com.uphill.scheduling.appointment.domain.Appointment;
import com.uphill.scheduling.appointment.domain.AppointmentRepository;
import com.uphill.scheduling.appointment.domain.NoDoctorAvailableException;
import com.uphill.scheduling.appointment.domain.NoRoomAvailableException;
import com.uphill.scheduling.appointment.domain.PatientInfo;
import com.uphill.scheduling.appointment.domain.SlotAlreadyTakenException;
import com.uphill.scheduling.appointment.domain.TimeSlot;
import com.uphill.scheduling.doctor.DoctorCatalog;
import com.uphill.scheduling.doctor.DoctorSnapshot;
import com.uphill.scheduling.room.RoomCatalog;
import com.uphill.scheduling.room.RoomSnapshot;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

/**
 * Booking use case. Orchestrates resource resolution and persistence; the
 * business rules themselves live in the aggregate and value objects.
 *
 * <p>Resolution strategy: read the set of doctors/rooms busy in the slot, then
 * pick the first catalogue entry not in that set. The final, authoritative guard
 * against double-booking is the database unique constraint — if two requests
 * resolve to the same free resource concurrently, exactly one insert wins and the
 * other is translated into a {@link SlotAlreadyTakenException}.
 */
@Service
public class AppointmentBookingService {

    private final DoctorCatalog doctorCatalog;
    private final RoomCatalog roomCatalog;
    private final AppointmentRepository appointmentRepository;
    private final ApplicationEventPublisher events;

    public AppointmentBookingService(DoctorCatalog doctorCatalog,
                                     RoomCatalog roomCatalog,
                                     AppointmentRepository appointmentRepository,
                                     ApplicationEventPublisher events) {
        this.doctorCatalog = doctorCatalog;
        this.roomCatalog = roomCatalog;
        this.appointmentRepository = appointmentRepository;
        this.events = events;
    }

    @Transactional
    public Appointment book(BookAppointmentCommand command) {
        TimeSlot slot = new TimeSlot(command.start(), command.end());
        PatientInfo patient = new PatientInfo(command.patientName(), command.patientEmail());

        DoctorSnapshot doctor = firstAvailableDoctor(command, slot);
        RoomSnapshot room = firstAvailableRoom(slot);

        Appointment appointment = Appointment.book(
                patient, command.specialty(), slot,
                doctor.id(), doctor.fullName(), room.id(), room.name());

        Appointment saved;
        try {
            saved = appointmentRepository.save(appointment);
        } catch (DataIntegrityViolationException ex) {
            // Lost the race for this doctor/room slot against a concurrent booking.
            throw new SlotAlreadyTakenException(slot, ex);
        }

        events.publishEvent(new AppointmentBooked(
                saved.getId(), patient.name(), patient.email(), command.specialty(),
                doctor.id(), doctor.fullName(), room.id(), room.name(),
                slot.start(), slot.end()));

        return saved;
    }

    private DoctorSnapshot firstAvailableDoctor(BookAppointmentCommand command, TimeSlot slot) {
        Set<UUID> busy = appointmentRepository.findBusyDoctorIds(slot.start(), slot.end());
        return doctorCatalog.findActiveBySpecialty(command.specialty()).stream()
                .filter(doctor -> !busy.contains(doctor.id()))
                .findFirst()
                .orElseThrow(() -> new NoDoctorAvailableException(command.specialty(), slot));
    }

    private RoomSnapshot firstAvailableRoom(TimeSlot slot) {
        Set<UUID> busy = appointmentRepository.findBusyRoomIds(slot.start(), slot.end());
        return roomCatalog.findAllActive().stream()
                .filter(room -> !busy.contains(room.id()))
                .findFirst()
                .orElseThrow(() -> new NoRoomAvailableException(slot));
    }
}
