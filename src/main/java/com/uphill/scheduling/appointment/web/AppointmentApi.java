package com.uphill.scheduling.appointment.web;

import com.uphill.scheduling.appointment.web.dto.AppointmentResponse;
import com.uphill.scheduling.appointment.web.dto.BookAppointmentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * REST contract for appointments. The interface holds <em>both</em> the Spring MVC
 * mapping annotations and the OpenAPI documentation, so the controller stays a
 * thin implementation and the API surface is described in one place. Swagger UI is
 * served at {@code /swagger-ui.html} and the spec at {@code /v3/api-docs}.
 */
@Tag(name = "Appointments", description = "Book and list medical appointments")
@RequestMapping(path = "/api/v1/appointments", produces = MediaType.APPLICATION_JSON_VALUE)
public interface AppointmentApi {

    @Operation(
            summary = "Book an appointment",
            description = "Assigns an available doctor of the requested specialty and an "
                    + "available room for the requested slot, then confirms the booking.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Appointment booked"),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "No doctor or room available for the slot",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    AppointmentResponse book(@Valid @RequestBody BookAppointmentRequest request);

    @Operation(summary = "List all appointments", description = "Administrative listing of every scheduled appointment.")
    @ApiResponse(responseCode = "200", description = "Appointments returned")
    @GetMapping
    List<AppointmentResponse> list();
}
