package com.uphill.scheduling.doctor.domain;

import com.uphill.scheduling.doctor.Specialty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Doctor aggregate root. A doctor belongs to exactly one specialty and may be
 * (de)activated without being deleted, so historical appointments remain valid.
 */
@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "specialty", nullable = false)
    private Specialty specialty;

    @Column(name = "active", nullable = false)
    private boolean active;

    protected Doctor() {
        // for JPA
    }

    private Doctor(UUID id, String fullName, Specialty specialty, boolean active) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Doctor name must not be blank");
        }
        if (specialty == null) {
            throw new IllegalArgumentException("Doctor specialty must not be null");
        }
        this.id = id;
        this.fullName = fullName;
        this.specialty = specialty;
        this.active = active;
    }

    public static Doctor register(String fullName, Specialty specialty) {
        return new Doctor(UUID.randomUUID(), fullName, specialty, true);
    }

    public void deactivate() {
        this.active = false;
    }

    public UUID getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public Specialty getSpecialty() {
        return specialty;
    }

    public boolean isActive() {
        return active;
    }
}
