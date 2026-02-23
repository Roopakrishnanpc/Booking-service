package com.movieticket.booking.exception;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ==========================================================
    // 409 - CONFLICT
    // ==========================================================

    @ExceptionHandler({
            SeatAlreadyLockedException.class,
            DuplicateBookingException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex) {

        log.warn("CONFLICT: {}", ex.getMessage());

        return buildResponse(
                ex,
                HttpStatus.CONFLICT,
                ErrorCode.SEAT_ALREADY_BOOKED
        );
    }

    // ==========================================================
    // 404 - NOT FOUND
    // ==========================================================

    @ExceptionHandler({
            BookingNotFoundException.class,
            ShowNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {

        log.warn("NOT_FOUND: {}", ex.getMessage());

        return buildResponse(
                ex,
                HttpStatus.NOT_FOUND,
                ErrorCode.BOOKING_NOT_FOUND
        );
    }

    // ==========================================================
    // 403 - FORBIDDEN
    // ==========================================================

    @ExceptionHandler({
            UnauthorizedBookingAccessException.class,
            ShowInactiveException.class
    })
    public ResponseEntity<ErrorResponse> handleForbidden(RuntimeException ex) {

        log.warn("FORBIDDEN: {}", ex.getMessage());

        return buildResponse(
                ex,
                HttpStatus.FORBIDDEN,
                ErrorCode.ACCESS_DENIED
        );
    }

    // ==========================================================
    // 400 - BAD REQUEST (Domain Errors)
    // ==========================================================

    @ExceptionHandler({
            PastShowBookingException.class,
            InvalidBookingStateException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {

        log.warn("BAD_REQUEST: {}", ex.getMessage());

        return buildResponse(
                ex,
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_STATE
        );
    }

    // ==========================================================
    // 400 - VALIDATION ERRORS (@Valid)
    // ==========================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex
    ) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        log.warn("VALIDATION_ERROR: {}", message);

        return buildResponse(
                ex,
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                message
        );
    }

    // ==========================================================
    // 500 - INTERNAL SERVER ERROR (Fallback)
    // ==========================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {

        log.error("INTERNAL_SERVER_ERROR", ex);

        return buildResponse(
                ex,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR,
                "Something went wrong"
        );
    }

    // ==========================================================
    // COMMON RESPONSE BUILDER
    // ==========================================================

    private ResponseEntity<ErrorResponse> buildResponse(
            Exception ex,
            HttpStatus status,
            ErrorCode code
    ) {
        return buildResponse(ex, status, code, ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            Exception ex,
            HttpStatus status,
            ErrorCode code,
            String message
    ) {

        String correlationId = MDC.get("correlationId");

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(code)   
                .message(ex.getMessage())
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .correlationId(correlationId)
                .build();

        return new ResponseEntity<>(response, status);
    }
}