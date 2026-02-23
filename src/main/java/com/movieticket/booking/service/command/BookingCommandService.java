package com.movieticket.booking.service.command;

import com.movieticket.booking.dto.BookingRequestDTO;
import com.movieticket.booking.dto.BookingResponseDTO;
import reactor.core.publisher.Mono;

public interface BookingCommandService {

    Mono<BookingResponseDTO> createBooking(String userId, String idempotencyKey,
                                           BookingRequestDTO request, String token);

    Mono<Void> cancelBooking(Long bookingId,
                             String userId, String token);
}