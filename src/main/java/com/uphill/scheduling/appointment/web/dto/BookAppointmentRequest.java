package com.uphill.scheduling.appointment.web.dto;

import com.uphill.scheduling.doctor.Specialty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Schema(description = "Request to book a medical appointment.")
public record BookAppointmentRequest(

        @Schema(example = "Carlos Fonseca")
        @NotBlank
        String patientName,

        @Schema(example = "carlos.fonseca@example.pt")
        @NotBlank @Email
        String patientEmail,

        @Schema(example = "GENERAL_PRACTICE")
        @NotNull
        Specialty specialty,

        @Schema(description = "Slot start (inclusive), ISO-8601 instant", example = "2026-07-01T09:00:00Z")
        @NotNull @Future
        Instant start,

        @Schema(description = "Slot end (exclusive), ISO-8601 instant", example = "2026-07-01T09:30:00Z")
        @NotNull @Future
        Instant end) {
}
