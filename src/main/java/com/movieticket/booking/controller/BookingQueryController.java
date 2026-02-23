package com.movieticket.booking.controller;

import com.movieticket.booking.dto.BookingQueryResponseDTO;
import com.movieticket.booking.service.query.BookingQueryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/bookings/query")
@RequiredArgsConstructor
@Slf4j
public class BookingQueryController {

    private final BookingQueryService bookingQueryService;

    @GetMapping
    public Mono<ResponseEntity<Flux<BookingQueryResponseDTO>>> getUserBookings(
            @AuthenticationPrincipal Jwt jwt) {
    	String userId=jwt.getSubject();
        log.info("Fetch bookings for user={}", userId);

        Flux<BookingQueryResponseDTO> bookings =
                bookingQueryService.getUserBookings(userId);

        return Mono.just(ResponseEntity.ok(bookings));
    }
}