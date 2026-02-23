package com.movieticket.booking.event;

import java.math.BigDecimal;

public record BookingCreatedEvent(
        Long bookingId,
        String userId,
        BigDecimal amount
) {}