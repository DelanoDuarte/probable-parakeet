package com.uphill.scheduling.appointment.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PatientInfoTest {

    @Test
    void acceptsValidNameAndEmail() {
        PatientInfo info = new PatientInfo("  Maria Silva  ", "  maria@example.pt ");
        assertThat(info.name()).isEqualTo("Maria Silva");
        assertThat(info.email()).isEqualTo("maria@example.pt");
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> new PatientInfo("   ", "maria@example.pt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    void rejectsInvalidEmail() {
        assertThatThrownBy(() -> new PatientInfo("Maria", "not-an-email"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("e-mail");
        assertThatThrownBy(() -> new PatientInfo("Maria", "missing@domain"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
