package com.movieticket.booking.controller;

import com.movieticket.booking.service.query.SeatQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("api/v1/bookings/query/seats")
@RequiredArgsConstructor
@Slf4j
public class SeatQueryController {

    private final SeatQueryService seatQueryService;

    @GetMapping("/{showId}")
    public Flux<String> getAvailableSeats(@PathVariable Long showId) {

        log.info("Request seat availability showId={}", showId);

        return seatQueryService.getAvailableSeats(showId);
    }
}