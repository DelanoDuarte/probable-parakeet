package com.uphill.scheduling.doctor.infrastructure;

import com.uphill.scheduling.doctor.DoctorCatalog;
import com.uphill.scheduling.doctor.DoctorSnapshot;
import com.uphill.scheduling.doctor.Specialty;
import com.uphill.scheduling.doctor.domain.Doctor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Implements the doctor module's public {@link DoctorCatalog} over JPA. */
@Component
class DoctorCatalogAdapter implements DoctorCatalog {

    private final DoctorJpaRepository repository;

    DoctorCatalogAdapter(DoctorJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<DoctorSnapshot> findActiveBySpecialty(Specialty specialty) {
        return repository.findBySpecialtyAndActiveIsTrueOrderByFullNameAsc(specialty)
                .stream()
                .map(DoctorCatalogAdapter::toSnapshot)
                .toList();
    }

    @Override
    public Optional<DoctorSnapshot> findById(UUID id) {
        return repository.findById(id).map(DoctorCatalogAdapter::toSnapshot);
    }

    private static DoctorSnapshot toSnapshot(Doctor doctor) {
        return new DoctorSnapshot(doctor.getId(), doctor.getFullName(), doctor.getSpecialty());
    }
}
