package com.uphill.scheduling.appointment.application;

import com.uphill.scheduling.appointment.domain.Appointment;
import com.uphill.scheduling.appointment.domain.AppointmentRepository;
import com.uphill.scheduling.appointment.domain.PatientInfo;
import com.uphill.scheduling.appointment.domain.TimeSlot;
import com.uphill.scheduling.appointment.domain.exceptions.NoDoctorAvailableException;
import com.uphill.scheduling.appointment.domain.exceptions.NoRoomAvailableException;
import com.uphill.scheduling.appointment.domain.exceptions.SlotAlreadyTakenException;
import com.uphill.scheduling.doctor.DoctorRegistry;
import com.uphill.scheduling.doctor.DoctorView;
import com.uphill.scheduling.room.RoomRegistry;
import com.uphill.scheduling.room.RoomView;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

/**
 * Transactional boundary for a single booking attempt. Isolated here so
 * {@link BookAppointmentUseCase} can retry on constraint violations without
 * self-invocation proxy issues — each call to {@link #execute} runs in its
 * own transaction with a fresh read of available resources.
 */
@Component
class BookingTransaction {

    private final DoctorRegistry doctorRegistry;
    private final RoomRegistry roomRegistry;
    private final AppointmentRepository appointmentRepository;

    BookingTransaction(DoctorRegistry doctorRegistry,
                       RoomRegistry roomRegistry,
                       AppointmentRepository appointmentRepository) {
        this.doctorRegistry = doctorRegistry;
        this.roomRegistry = roomRegistry;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    Appointment execute(BookAppointmentCommand command) {
        TimeSlot slot = new TimeSlot(command.start(), command.end());
        PatientInfo patient = new PatientInfo(command.patientName(), command.patientEmail());

        DoctorView doctor = firstAvailableDoctor(command, slot);
        RoomView room = firstAvailableRoom(slot);

        Appointment appointment = Appointment.book(
                patient, command.specialty(), slot,
                doctor.id(), doctor.fullName(), room.id(), room.name());

        try {
            return appointmentRepository.save(appointment);
        } catch (DataIntegrityViolationException ex) {
            throw new SlotAlreadyTakenException(slot, ex);
        }
    }

    private DoctorView firstAvailableDoctor(BookAppointmentCommand command, TimeSlot slot) {
        Set<UUID> busy = appointmentRepository.findBusyDoctorIds(slot.start(), slot.end());
        return doctorRegistry.findActiveBySpecialty(command.specialty()).stream()
                .filter(doctor -> !busy.contains(doctor.id()))
                .findFirst()
                .orElseThrow(() -> new NoDoctorAvailableException(command.specialty(), slot));
    }

    private RoomView firstAvailableRoom(TimeSlot slot) {
        Set<UUID> busy = appointmentRepository.findBusyRoomIds(slot.start(), slot.end());
        return roomRegistry.findAllActive().stream()
                .filter(room -> !busy.contains(room.id()))
                .findFirst()
                .orElseThrow(() -> new NoRoomAvailableException(slot));
    }
}
