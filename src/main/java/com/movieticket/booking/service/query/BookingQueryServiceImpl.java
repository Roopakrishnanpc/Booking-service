package com.movieticket.booking.service.query;

import com.movieticket.booking.domain.Booking;
import com.movieticket.booking.dto.BookingQueryResponseDTO;
import com.movieticket.booking.exception.BookingNotFoundException;
import com.movieticket.booking.exception.UnauthorizedBookingAccessException;
import com.movieticket.booking.repository.BookingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingQueryServiceImpl implements BookingQueryService {

    private final BookingRepository bookingRepository;

    // ==============================
    // GET ALL BOOKINGS FOR USER
    // ==============================

    @Override
    @Cacheable(value="userBookings", key="#userId")
    public Flux<BookingQueryResponseDTO> getUserBookings(String userId) {

        return Mono.fromCallable(() -> {
                    log.info("Fetching bookings for user={}", userId);
                    return bookingRepository.findByUserId(userId);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(list -> Flux.fromIterable(list))
                .map(this::mapToDto);
    }

    // ==============================
    // GET SINGLE BOOKING
    // ==============================

    @Override
    public Mono<BookingQueryResponseDTO> getBookingById(Long bookingId,
                                                        String userId) {

        return Mono.fromCallable(() -> {

                    log.info("Fetching booking id={} user={}",
                            bookingId, userId);

                    Booking booking =
                            bookingRepository.findById(bookingId)
                                    .orElseThrow(() ->
                                            new BookingNotFoundException(
                                                    "Booking not found"));

                    if (!booking.getUserId().equals(userId)) {

                        log.warn("Unauthorized access bookingId={} user={}",
                                bookingId, userId);

                        throw new UnauthorizedBookingAccessException(
                                "Access denied");
                    }

                    return booking;

                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(this::mapToDto);
    }

    // ==============================
    // MAPPER
    // ==============================

    private BookingQueryResponseDTO mapToDto(Booking booking) {

        return BookingQueryResponseDTO.builder()
                .bookingId(booking.getId())
                .showId(booking.getShowId())
                .amount(booking.getAmount())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}