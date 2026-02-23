package com.movieticket.booking.event;

import com.movieticket.booking.domain.*;
import com.movieticket.booking.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingResultListener {

    private final BookingRepository bookingRepository;
    private final SeatLockRepository seatLockRepository;

    @Transactional
   // @EventListener
    public void handle(PaymentCompletedEvent event) {

        log.info("Handling payment result bookingId={} success={}",
                event.bookingId(),
                event.success());

        Booking booking =
                bookingRepository.findById(event.bookingId())
                        .orElseThrow();

        if (event.success()) {

            booking.setStatus(BookingStatus.CONFIRMED);
            log.info("Booking confirmed bookingId={}",
                    booking.getId());

        } else {

            booking.setStatus(BookingStatus.FAILED);

            seatLockRepository.findAll()
                    .stream()
                    .filter(seat ->
                            seat.getShowId()
                                    .equals(booking.getShowId()))
                    .forEach(seat ->
                            seat.setLocked(false));

            log.warn("Booking failed. Seats released bookingId={}",
                    booking.getId());
        }

        bookingRepository.save(booking);
    }
}