package com.movieticket.booking.controller;

import com.movieticket.booking.dto.BookingRequestDTO;
import com.movieticket.booking.dto.BookingResponseDTO;
import com.movieticket.booking.service.command.BookingCommandService;

import jakarta.validation.Valid;

import com.movieticket.booking.config.ApiVersion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(ApiVersion.V1 + "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingCommandController {

    private final BookingCommandService bookingCommandService;
	    @PostMapping("/book")
	    public Mono<ResponseEntity<BookingResponseDTO>> createBooking(
	            @AuthenticationPrincipal Jwt jwt,  //@RequestHeader("X-User-Id") String userId,
	
	            @RequestHeader("Idempotency-Key") String idempotencyKey,
	            @RequestBody BookingRequestDTO request
	    ) {

        String userId = jwt.getSubject();
        String token = jwt.getTokenValue();
        log.info("Theatre token={}", token);
        System.out.print(token);
          
        log.info("Create booking request user={} showId={} seats={}",
                userId,
                request.getShowId(),
                request.getSeats());

        return bookingCommandService
                .createBooking(userId, idempotencyKey,request,token)
                .map(response -> {
                    log.info("Booking created bookingId={} user={}",
                            response.getBookingId(),
                            userId);

                    return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(response);
                });
    }

          //  @AuthenticationPrincipal(expression = "username") String userId) {


    @DeleteMapping("/{bookingId}")
    public Mono<ResponseEntity<Void>> cancelBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        String token = jwt.getTokenValue();

        log.info("Cancel booking request bookingId={} user={}",
                bookingId,
                userId);

        return bookingCommandService
                .cancelBooking(bookingId, userId, token)
                .then(Mono.fromSupplier(() ->
                        ResponseEntity.noContent().build()
                ));
    }
}

