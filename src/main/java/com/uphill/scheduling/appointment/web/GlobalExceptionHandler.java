package com.uphill.scheduling.appointment.web;

import com.uphill.scheduling.appointment.domain.exceptions.NoDoctorAvailableException;
import com.uphill.scheduling.appointment.domain.exceptions.NoRoomAvailableException;
import com.uphill.scheduling.appointment.domain.exceptions.SlotAlreadyTakenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Translates domain and validation failures into RFC 7807 {@link ProblemDetail}
 * responses. Resource-contention failures map to 409, bad input to 400.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler({NoDoctorAvailableException.class, NoRoomAvailableException.class, SlotAlreadyTakenException.class})
    ProblemDetail handleConflict(RuntimeException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Slot unavailable");
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleBadRequest(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid request");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Validation failed");
        return problem;
    }
}
