package com.uphill.scheduling.doctor.infrastructure;

import com.uphill.scheduling.doctor.DoctorRegistry;
import com.uphill.scheduling.doctor.DoctorView;
import com.uphill.scheduling.doctor.Specialty;
import com.uphill.scheduling.doctor.domain.Doctor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Implements the doctor module's public {@link DoctorRegistry} over JPA. */
@Component
class DoctorRegistryAdapter implements DoctorRegistry {

    private final DoctorJpaRepository repository;

    DoctorRegistryAdapter(DoctorJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<DoctorView> findActiveBySpecialty(Specialty specialty) {
        return repository.findBySpecialtyAndActiveIsTrueOrderByFullNameAsc(specialty)
                .stream()
                .map(DoctorRegistryAdapter::toView)
                .toList();
    }

    @Override
    public Optional<DoctorView> findById(UUID id) {
        return repository.findById(id).map(DoctorRegistryAdapter::toView);
    }

    private static DoctorView toView(Doctor doctor) {
        return new DoctorView(doctor.getId(), doctor.getFullName(), doctor.getSpecialty());
    }
}
