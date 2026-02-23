package com.movieticket.booking.service.query;

import reactor.core.publisher.Flux;

public interface SeatQueryService {

    Flux<String> getAvailableSeats(Long showId);
}