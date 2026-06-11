package com.uphill.scheduling.appointment.infrastructure;

import com.uphill.scheduling.appointment.domain.Appointment;
import com.uphill.scheduling.appointment.domain.AppointmentRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Adapts the Spring Data repository to the domain {@link AppointmentRepository} port. */
@Component
class AppointmentRepositoryAdapter implements AppointmentRepository {

    private final AppointmentJpaRepository jpa;

    AppointmentRepositoryAdapter(AppointmentJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Appointment save(Appointment appointment) {
        return jpa.save(appointment);
    }

    @Override
    public Optional<Appointment> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public List<Appointment> findAll() {
        return jpa.findAll();
    }

    @Override
    public Set<UUID> findBusyDoctorIds(Instant start, Instant end) {
        return jpa.findBusyDoctorIds(start, end);
    }

    @Override
    public Set<UUID> findBusyRoomIds(Instant start, Instant end) {
        return jpa.findBusyRoomIds(start, end);
    }
}
