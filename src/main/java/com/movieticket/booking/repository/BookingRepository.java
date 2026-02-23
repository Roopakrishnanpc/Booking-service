package com.movieticket.booking.repository;

import com.movieticket.booking.domain.Booking;
import com.movieticket.booking.domain.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByIdempotencyKey(String idempotencyKey);

    List<Booking> findByUserId(String userId);

    List<Booking> findByStatus(BookingStatus status);
    
}