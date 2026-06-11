package com.uphill.scheduling.doctor.infrastructure;

import com.uphill.scheduling.doctor.Specialty;
import com.uphill.scheduling.doctor.domain.Doctor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Seeds a small roster on startup. With an in-memory H2 database this is the
 * simplest way to have bookable resources; in production this data would live in
 * a managed store and be administered through its own API.
 */
@Configuration
class DoctorSeeder {

    @Bean
    ApplicationRunner seedDoctors(DoctorJpaRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            repository.saveAll(List.of(
                    Doctor.register("Ana Silva", Specialty.GENERAL_PRACTICE),
                    Doctor.register("Joao Pereira", Specialty.GENERAL_PRACTICE),
                    Doctor.register("Maria Santos", Specialty.CARDIOLOGY),
                    Doctor.register("Rui Costa", Specialty.CARDIOLOGY),
                    Doctor.register("Ines Carvalho", Specialty.DERMATOLOGY),
                    Doctor.register("Pedro Oliveira", Specialty.PEDIATRICS),
                    Doctor.register("Sofia Martins", Specialty.ORTHOPEDICS)
            ));
        };
    }
}
