package com.movieticket.booking.service.query;

import com.movieticket.booking.dto.BookingQueryResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookingQueryService {

    Flux<BookingQueryResponseDTO> getUserBookings(String userId);

    Mono<BookingQueryResponseDTO> getBookingById(Long bookingId,
                                                 String userId);
}