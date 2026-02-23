package com.movieticket.booking.event;

public record PaymentCompletedEvent(
        Long bookingId,
        boolean success
) {}