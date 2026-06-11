package com.uphill.scheduling.doctor.infrastructure;

import com.uphill.scheduling.doctor.Specialty;
import com.uphill.scheduling.doctor.domain.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface DoctorJpaRepository extends JpaRepository<Doctor, UUID> {

    List<Doctor> findBySpecialtyAndActiveIsTrueOrderByFullNameAsc(Specialty specialty);
}
