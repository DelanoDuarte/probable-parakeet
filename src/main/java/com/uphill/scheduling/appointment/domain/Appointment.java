package com.uphill.scheduling.appointment.domain;

import com.uphill.scheduling.appointment.AppointmentBooked;
import com.uphill.scheduling.doctor.Specialty;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.AbstractAggregateRoot;

/**
 * Appointment aggregate root — the heart of the scheduling context.
 *
 * <p>An appointment is created already <em>resolved</em>: by the time the
 * aggregate exists, an available doctor and room for the requested specialty and slot have been
 * chosen. The aggregate therefore guards its own internal invariants (non-null collaborators, valid
 * slot, immutable identity); the cross-aggregate "no double booking" rule is enforced at the
 * database level via the unique constraints declared below, which is the only race-safe place to do
 * it under concurrency.
 *
 * <p>Doctor and room are referenced by id (with a denormalised display name copied
 * at booking time) rather than by object reference: they live in other bounded contexts, so we
 * reference across the boundary by identity, not by association.
 */
@Entity
@Table(name = "appointments", uniqueConstraints = {
    @UniqueConstraint(name = "uq_doctor_slot", columnNames = {"doctor_id", "start_at"}),
    @UniqueConstraint(name = "uq_room_slot", columnNames = {"room_id", "start_at"})})
public class Appointment extends AbstractAggregateRoot<Appointment> {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Embedded
  private PatientInfo patient;

  @Enumerated(EnumType.STRING)
  @Column(name = "specialty", nullable = false)
  private Specialty specialty;

  @Embedded
  private TimeSlot slot;

  @Column(name = "doctor_id", nullable = false)
  private UUID doctorId;

  @Column(name = "doctor_name", nullable = false)
  private String doctorName;

  @Column(name = "room_id", nullable = false)
  private UUID roomId;

  @Column(name = "room_name", nullable = false)
  private String roomName;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private AppointmentStatus status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Version
  @Column(name = "version", nullable = false)
  private long version;

  protected Appointment() {
    // for JPA
  }

  private Appointment(UUID id, PatientInfo patient, Specialty specialty, TimeSlot slot,
      UUID doctorId, String doctorName, UUID roomId, String roomName) {
    this.id = id;
    this.patient = patient;
    this.specialty = specialty;
    this.slot = slot;
    this.doctorId = doctorId;
    this.doctorName = doctorName;
    this.roomId = roomId;
    this.roomName = roomName;
    this.status = AppointmentStatus.CONFIRMED;
    this.createdAt = Instant.now();
  }

  /**
   * Factory for a confirmed booking. All collaborators must be present — an appointment cannot
   * exist without a patient, a slot, a doctor and a room.
   */
  public static Appointment book(PatientInfo patient, Specialty specialty, TimeSlot slot,
      UUID doctorId, String doctorName, UUID roomId, String roomName) {
    requireNonNull(patient, "patient");
    requireNonNull(specialty, "specialty");
    requireNonNull(slot, "slot");
    requireNonNull(doctorId, "doctorId");
    requireNonNull(doctorName, "doctorName");
    requireNonNull(roomId, "roomId");
    requireNonNull(roomName, "roomName");

    // Domain Event
    Appointment appointment = new Appointment(UUID.randomUUID(), patient, specialty, slot, doctorId,
        doctorName, roomId, roomName);
    appointment.registerEvent(
        new AppointmentBooked(appointment.id, patient.name(), patient.email(), specialty, doctorId,
            doctorName, roomId, roomName, slot.start(), slot.end()));
    return appointment;
  }

  public void cancel() {
    if (status == AppointmentStatus.CANCELLED) {
      throw new IllegalStateException("Appointment is already cancelled");
    }
    this.status = AppointmentStatus.CANCELLED;
  }

  private static void requireNonNull(Object value, String field) {
    if (value == null) {
      throw new IllegalArgumentException(field + " must not be null");
    }
  }

  public UUID getId() {
    return id;
  }

  public PatientInfo getPatient() {
    return patient;
  }

  public Specialty getSpecialty() {
    return specialty;
  }

  public TimeSlot getSlot() {
    return slot;
  }

  public UUID getDoctorId() {
    return doctorId;
  }

  public String getDoctorName() {
    return doctorName;
  }

  public UUID getRoomId() {
    return roomId;
  }

  public String getRoomName() {
    return roomName;
  }

  public AppointmentStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
