package com.uphill.scheduling.doctor;

/**
 * Medical specialty. Part of the doctor module's public API because a patient
 * requests an appointment <em>by specialty</em>, and doctors are classified by it.
 */
public enum Specialty {
    GENERAL_PRACTICE("General Practice"),
    CARDIOLOGY("Cardiology"),
    DERMATOLOGY("Dermatology"),
    PEDIATRICS("Pediatrics"),
    ORTHOPEDICS("Orthopedics");

    private final String displayName;

    Specialty(String displayName) {
        this.displayName = displayName;
    }

    /** Human-readable label used in confirmation e-mails and external-system payloads. */
    public String displayName() {
        return displayName;
    }
}
