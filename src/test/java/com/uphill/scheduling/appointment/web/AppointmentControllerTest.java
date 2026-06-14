package com.uphill.scheduling.appointment.web;

import com.uphill.scheduling.appointment.application.AppointmentQueryService;
import com.uphill.scheduling.appointment.application.BookAppointmentUseCase;
import com.uphill.scheduling.appointment.domain.Appointment;
import com.uphill.scheduling.appointment.domain.PatientInfo;
import com.uphill.scheduling.appointment.domain.TimeSlot;
import com.uphill.scheduling.appointment.domain.exceptions.NoDoctorAvailableException;
import com.uphill.scheduling.appointment.domain.exceptions.NoRoomAvailableException;
import com.uphill.scheduling.appointment.domain.exceptions.SlotAlreadyTakenException;
import com.uphill.scheduling.doctor.Specialty;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer slice test for {@link AppointmentController}.
 *
 * <p>Uses {@link WebMvcTest} to load only the web layer (controller +
 * {@link GlobalExceptionHandler}); use-case dependencies are mocked so the
 * suite runs without a database or Spring Modulith context.
 *
 * <p>Covers: happy-path booking (201), validation failures (400), all three
 * domain conflict exceptions (409), and the admin list endpoint (200).
 */
@WebMvcTest
class AppointmentControllerTest {

    private static final String URL = "/api/v1/appointments";

    private static final TimeSlot SLOT = new TimeSlot(
            Instant.parse("2030-01-01T09:00:00Z"),
            Instant.parse("2030-01-01T09:30:00Z"));

    @Autowired
    MockMvc mvc;

    @MockitoBean
    BookAppointmentUseCase bookAppointment;

    @MockitoBean
    AppointmentQueryService queryService;

    // --- POST /api/v1/appointments ----------------------------------------

    @Test
    void bookAppointment_returnsCreated() throws Exception {
        Appointment booked = appointment(Specialty.GENERAL_PRACTICE);
        when(bookAppointment.book(any())).thenReturn(booked);

        mvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookRequestJson("GENERAL_PRACTICE")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(booked.getId().toString()))
                .andExpect(jsonPath("$.patientName").value("Carlos Fonseca"))
                .andExpect(jsonPath("$.patientEmail").value("carlos@example.pt"))
                .andExpect(jsonPath("$.specialty").value("GENERAL_PRACTICE"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.doctorId").isNotEmpty())
                .andExpect(jsonPath("$.roomId").isNotEmpty());
    }

    @Test
    void bookAppointment_missingEmail_returns400() throws Exception {
        mvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "patientName": "Carlos Fonseca",
                                  "specialty": "GENERAL_PRACTICE",
                                  "start": "2030-01-01T09:00:00Z",
                                  "end": "2030-01-01T09:30:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("patientEmail")));
    }

    @Test
    void bookAppointment_invalidEmail_returns400() throws Exception {
        mvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "patientName": "Carlos Fonseca",
                                  "patientEmail": "not-an-email",
                                  "specialty": "GENERAL_PRACTICE",
                                  "start": "2030-01-01T09:00:00Z",
                                  "end": "2030-01-01T09:30:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void bookAppointment_pastDate_returns400() throws Exception {
        mvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "patientName": "Carlos Fonseca",
                                  "patientEmail": "carlos@example.pt",
                                  "specialty": "GENERAL_PRACTICE",
                                  "start": "2020-01-01T09:00:00Z",
                                  "end": "2020-01-01T09:30:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void bookAppointment_noDoctorAvailable_returns409() throws Exception {
        when(bookAppointment.book(any()))
                .thenThrow(new NoDoctorAvailableException(Specialty.DERMATOLOGY, SLOT));

        mvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookRequestJson("DERMATOLOGY")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Slot unavailable"));
    }

    @Test
    void bookAppointment_noRoomAvailable_returns409() throws Exception {
        when(bookAppointment.book(any()))
                .thenThrow(new NoRoomAvailableException(SLOT));

        mvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookRequestJson("GENERAL_PRACTICE")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Slot unavailable"));
    }

    @Test
    void bookAppointment_slotAlreadyTaken_returns409() throws Exception {
        when(bookAppointment.book(any()))
                .thenThrow(new SlotAlreadyTakenException(SLOT, new RuntimeException("db constraint")));

        mvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookRequestJson("GENERAL_PRACTICE")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Slot unavailable"));
    }

    // --- GET /api/v1/appointments -----------------------------------------

    @Test
    void listAppointments_returnsAll() throws Exception {
        when(queryService.findAll()).thenReturn(List.of(
                appointment(Specialty.CARDIOLOGY),
                appointment(Specialty.DERMATOLOGY)));

        mvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].specialty").value("CARDIOLOGY"))
                .andExpect(jsonPath("$[1].specialty").value("DERMATOLOGY"));
    }

    @Test
    void listAppointments_empty_returnsEmptyArray() throws Exception {
        when(queryService.findAll()).thenReturn(List.of());

        mvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // --- helpers ----------------------------------------------------------

    private static Appointment appointment(Specialty specialty) {
        return Appointment.book(
                new PatientInfo("Carlos Fonseca", "carlos@example.pt"),
                specialty,
                SLOT,
                UUID.randomUUID(), "Dr. Costa",
                UUID.randomUUID(), "Room 1");
    }

    private static String bookRequestJson(String specialty) {
        return """
                {
                  "patientName": "Carlos Fonseca",
                  "patientEmail": "carlos@example.pt",
                  "specialty": "%s",
                  "start": "2030-01-01T09:00:00Z",
                  "end": "2030-01-01T09:30:00Z"
                }
                """.formatted(specialty);
    }
}
