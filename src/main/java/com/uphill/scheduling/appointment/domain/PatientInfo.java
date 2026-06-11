package com.uphill.scheduling.appointment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object carrying the (lightweight) patient identity needed to book and
 * notify. The system does not own patient records, so this is intentionally
 * minimal; it self-validates name and e-mail.
 */
@Embeddable
public class PatientInfo {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Column(name = "patient_name", nullable = false)
    private String name;

    @Column(name = "patient_email", nullable = false)
    private String email;

    protected PatientInfo() {
        // for JPA
    }

    public PatientInfo(String name, String email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Patient name must not be blank");
        }
        if (email == null || !EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("Patient e-mail is not valid: " + email);
        }
        this.name = name.strip();
        this.email = email.strip();
    }

    public String name() {
        return name;
    }

    public String email() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PatientInfo other)) {
            return false;
        }
        return Objects.equals(name, other.name) && Objects.equals(email, other.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email);
    }
}
