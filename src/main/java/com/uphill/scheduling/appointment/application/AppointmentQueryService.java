package com.uphill.scheduling.appointment.application;

import com.uphill.scheduling.appointment.domain.Appointment;
import com.uphill.scheduling.appointment.domain.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Read-side use cases for appointments (admin listing). */
@Service
public class AppointmentQueryService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentQueryService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }
}
