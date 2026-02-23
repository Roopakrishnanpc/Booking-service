package com.movieticket.booking.service.command;

import com.movieticket.booking.service.payment.PaymentClient;
import com.movieticket.booking.client.TheatreClient;
import com.movieticket.booking.client.dto.ShowResponseDTO;
import com.movieticket.booking.discount.DiscountStrategy;
import com.movieticket.booking.service.queue.VirtualBookingQueue;
import com.movieticket.booking.dto.*;
import com.movieticket.booking.domain.Booking;
import com.movieticket.booking.domain.BookingStatus;
import com.movieticket.booking.domain.SeatLock;
import com.movieticket.booking.exception.*;
import com.movieticket.booking.repository.BookingRepository;
import com.movieticket.booking.repository.SeatLockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingCommandServiceImpl implements BookingCommandService {

    private final BookingRepository bookingRepository;
    private final SeatLockRepository seatLockRepository;
    private final TheatreClient theatreClient;
    private final PaymentClient paymentClient;
    private final DiscountStrategy discountStrategy;
    private final VirtualBookingQueue bookingQueue;
    private final CacheManager cacheManager; 
    // ============================================================
    // CREATE BOOKING
    // ============================================================

    
    public Mono<BookingResponseDTO> createBooking(
            String userId,
            String idempotencyKey,
            BookingRequestDTO request,
            String token) {

        return Mono.fromRunnable(() -> bookingQueue.enter())
                .subscribeOn(Schedulers.boundedElastic())
                .then(

                    Mono.fromCallable(() ->
                            bookingRepository.findByIdempotencyKey(idempotencyKey)
                    )
                    .subscribeOn(Schedulers.boundedElastic())
                    .flatMap(existingOpt -> {

                        if (existingOpt.isPresent()) {
                            log.warn("DUPLICATE_BOOKING idempotencyKey={}", idempotencyKey);
                            return Mono.just(mapToResponse(existingOpt.get()));
                        }

                        return theatreClient.getShowById(request.getShowId(), token)
                                .switchIfEmpty(Mono.error(
                                        new ShowNotFoundException("Show not found")
                                ))
                                .flatMap(this::validateShow)
                                .flatMap(show ->
                                        Mono.fromCallable(() ->
                                                createBookingEntity(userId, idempotencyKey, request, show)
                                        ).subscribeOn(Schedulers.boundedElastic())
                                )
                                .flatMap(booking -> processPayment(booking, token));
                    })
                )
                .doFinally(signal -> bookingQueue.exit());  // IMPORTANT
    }

    // ============================================================
    // CREATE BOOKING ENTITY
    // ============================================================

    private Booking createBookingEntity(
            String userId,
            String idempotencyKey,
            BookingRequestDTO request,
            ShowResponseDTO show) {

        if (request.getSeats() == null || request.getSeats().isEmpty()) {
            throw new IllegalArgumentException("No seats selected");
        }

        BigDecimal basePrice = BigDecimal.valueOf(200);

        BigDecimal totalAmount = discountStrategy.apply(
                basePrice,
                request.getSeats(),
                show.getShowTime()
        );

        // LOCK SEATS
        for (String seat : request.getSeats()) {

            SeatLock lock = seatLockRepository
                    .lockSeat(request.getShowId(), seat)
                    .orElseGet(() ->
                            seatLockRepository.save(
                                    SeatLock.builder()
                                            .showId(request.getShowId())
                                            .seatNumber(seat)
                                            .locked(false)
                                            .build()
                            )
                    );

            if (lock.isLocked()) {
                throw new SeatAlreadyLockedException("Seat already booked: " + seat);
            }

            lock.setLocked(true);
            seatLockRepository.save(lock);
        }

        Booking booking = Booking.builder()
                .userId(userId)
                .showId(request.getShowId())
                .seats(String.join(",", request.getSeats()))
                .amount(totalAmount)
                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .idempotencyKey(idempotencyKey)
                .build();

        bookingRepository.save(booking);

        log.info("BOOKING_CREATED bookingId={}", booking.getId());

        return booking;
    }

    // ============================================================
    // PAYMENT PROCESSING
    // ============================================================

    private Mono<BookingResponseDTO> processPayment(
            Booking booking,
            String token) {

        return paymentClient.charge(
                new PaymentRequestDTO(
                        booking.getId(),
                        booking.getUserId(),
                        booking.getAmount()
                ),
                token
        ).flatMap(response ->
                handlePaymentResult(booking, response)
        );
    }

    // ============================================================
    // HANDLE PAYMENT RESULT (FINAL CLEAN VERSION)
    // ============================================================

    private Mono<BookingResponseDTO> handlePaymentResult(
            Booking booking,
            PaymentResponseDTO response) {

        return Mono.fromCallable(() -> {

            String paymentStatus = response.getStatus();

            log.info("PAYMENT_RESPONSE bookingId={} status={} txId={}",
                    booking.getId(),
                    paymentStatus,
                    response.getTransactionId());

            switch (paymentStatus.toUpperCase()) {

                case "CHARGED":
                    booking.setStatus(BookingStatus.CONFIRMED);
                    booking.setPaymentTransactionId(response.getTransactionId());
                    Cache userCache = cacheManager.getCache("userBookings");
                    if (userCache != null) {
                        userCache.evict(booking.getUserId());
                    }

                    Cache seatCache = cacheManager.getCache("seatAvailability");
                    if (seatCache != null) {
                        seatCache.evict(booking.getShowId());
                    }
                    break;

                case "FAILED":
                    booking.setStatus(BookingStatus.FAILED);
                    releaseSeats(booking);
                    Cache userCach = cacheManager.getCache("userBookings");
                    if (userCach != null) {
                        userCach.evict(booking.getUserId());
                    }

                    Cache seatCach = cacheManager.getCache("seatAvailability");
                    if (seatCach != null) {
                        seatCach.evict(booking.getShowId());
                    }
                    break;

                case "PENDING":
                    booking.setStatus(BookingStatus.PENDING);
                    break;

                default:
                    log.error("UNKNOWN_PAYMENT_STATUS bookingId={} status={}",
                            booking.getId(),
                            paymentStatus);
                    booking.setStatus(BookingStatus.FAILED);
                    releaseSeats(booking);
            }

            bookingRepository.saveAndFlush(booking);
            log.error("After SAVE DB STATUS={}", bookingRepository.findById(booking.getId()).get().getStatus());

            return mapToResponse(booking);

        }).subscribeOn(Schedulers.boundedElastic());
    }

    // ============================================================
    // CANCEL BOOKING
    // ============================================================
    public Mono<Void> cancelBooking(
            Long bookingId,
            String userId,
            String token) {

        return Mono.fromCallable(() -> {

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() ->
                            new BookingNotFoundException("Booking not found")
                    );

            if (!booking.getUserId().equals(userId)) {
                throw new UnauthorizedBookingAccessException("Unauthorized");
            }

            if (booking.getStatus() != BookingStatus.CONFIRMED) {
                throw new InvalidBookingStateException(
                        "Only confirmed bookings can be cancelled"
                );
            }

            return booking;

        }).subscribeOn(Schedulers.boundedElastic())
          .flatMap(booking ->
                  paymentClient.refund(
                          new PaymentRequestDTO(
                                  booking.getId(),
                                  booking.getUserId(),
                                  booking.getAmount()
                          ),
                          token
                  ).flatMap(r -> {

                      booking.setStatus(BookingStatus.REFUNDED);
                      bookingRepository.save(booking);
                      releaseSeats(booking);

                      log.info("BOOKING_CANCELLED bookingId={}", booking.getId());
                      Cache userCache = cacheManager.getCache("userBookings");
                      if (userCache != null) {
                          userCache.evict(booking.getUserId());
                      }

                      Cache seatCache = cacheManager.getCache("seatAvailability");
                      if (seatCache != null) {
                          seatCache.evict(booking.getShowId());
                      }
                      return Mono.empty();
                  })
          );
    }

    // ============================================================
    // RELEASE SEATS
    // ============================================================

    private void releaseSeats(Booking booking) {

        List<String> seats = List.of(booking.getSeats().split(","));

        for (String seat : seats) {
            seatLockRepository
                    .findByShowIdAndSeatNumber(
                            booking.getShowId(),
                            seat
                    )
                    .ifPresent(lock -> {
                        lock.setLocked(false);
                        seatLockRepository.save(lock);
                    });
        }
    }

    // ============================================================
    // VALIDATE SHOW
    // ============================================================

    private Mono<ShowResponseDTO> validateShow(ShowResponseDTO show) {

        if (!show.isActive()) {
            return Mono.error(new ShowInactiveException("Show not active"));
        }

        if (show.getShowTime().isBefore(LocalTime.now())) {
            return Mono.error(new PastShowBookingException("Cannot book past show"));
        }

        return Mono.just(show);
    }

    // ============================================================
    // MAP RESPONSE
    // ============================================================

    private BookingResponseDTO mapToResponse(Booking booking) {

        return BookingResponseDTO.builder()
                .bookingId(booking.getId())
                .amount(booking.getAmount())
                .status(booking.getStatus())
                .build();
    }
}