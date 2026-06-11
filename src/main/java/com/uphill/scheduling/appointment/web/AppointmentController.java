package com.uphill.scheduling.appointment.web;

import com.uphill.scheduling.appointment.application.AppointmentBookingService;
import com.uphill.scheduling.appointment.application.AppointmentQueryService;
import com.uphill.scheduling.appointment.application.BookAppointmentCommand;
import com.uphill.scheduling.appointment.web.dto.AppointmentResponse;
import com.uphill.scheduling.appointment.web.dto.BookAppointmentRequest;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Thin adapter from HTTP to the application use cases. */
@RestController
class AppointmentController implements AppointmentApi {

    private final AppointmentBookingService bookingService;
    private final AppointmentQueryService queryService;

    AppointmentController(AppointmentBookingService bookingService, AppointmentQueryService queryService) {
        this.bookingService = bookingService;
        this.queryService = queryService;
    }

    @Override
    public AppointmentResponse book(BookAppointmentRequest request) {
        var command = new BookAppointmentCommand(
                request.patientName(),
                request.patientEmail(),
                request.specialty(),
                request.start(),
                request.end());
        return AppointmentResponse.from(bookingService.book(command));
    }

    @Override
    public List<AppointmentResponse> list() {
        return queryService.findAll().stream()
                .map(AppointmentResponse::from)
                .toList();
    }
}
