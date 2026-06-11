package com.uphill.scheduling.appointment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.Instant;
import java.util.Objects;

/**
 * Value object representing a half-open time interval {@code [start, end)}.
 * Self-validating: a slot is meaningless unless it has positive duration.
 */
@Embeddable
public class TimeSlot {

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    protected TimeSlot() {
        // for JPA
    }

    public TimeSlot(Instant start, Instant end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Time slot start and end must not be null");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Time slot end must be strictly after start");
        }
        this.startAt = start;
        this.endAt = end;
    }

    /** Two half-open intervals overlap iff each starts before the other ends. */
    public boolean overlaps(TimeSlot other) {
        return startAt.isBefore(other.endAt) && other.startAt.isBefore(endAt);
    }

    public Instant start() {
        return startAt;
    }

    public Instant end() {
        return endAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TimeSlot other)) {
            return false;
        }
        return Objects.equals(startAt, other.startAt) && Objects.equals(endAt, other.endAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startAt, endAt);
    }

    @Override
    public String toString() {
        return startAt + " -> " + endAt;
    }
}
