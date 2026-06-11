package com.uphill.scheduling.appointment.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeSlotTest {

    private static final Instant T0 = Instant.parse("2026-07-01T09:00:00Z");
    private static final Instant T1 = Instant.parse("2026-07-01T09:30:00Z");

    @Test
    void rejectsNullBounds() {
        assertThatThrownBy(() -> new TimeSlot(null, T1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TimeSlot(T0, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNonPositiveDuration() {
        assertThatThrownBy(() -> new TimeSlot(T1, T0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TimeSlot(T0, T0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("after");
    }

    @Test
    void overlapsIsTrueForOverlappingIntervals() {
        TimeSlot a = new TimeSlot(T0, T1);
        TimeSlot b = new TimeSlot(T0.plus(15, ChronoUnit.MINUTES), T1.plus(15, ChronoUnit.MINUTES));
        assertThat(a.overlaps(b)).isTrue();
        assertThat(b.overlaps(a)).isTrue();
    }

    @Test
    void adjacentIntervalsDoNotOverlap() {
        // half-open [T0,T1) and [T1,T1+30) touch but do not overlap
        TimeSlot a = new TimeSlot(T0, T1);
        TimeSlot b = new TimeSlot(T1, T1.plus(30, ChronoUnit.MINUTES));
        assertThat(a.overlaps(b)).isFalse();
        assertThat(b.overlaps(a)).isFalse();
    }
}
