package com.movieticket.booking.exception;

public enum ErrorCode {

    SEAT_ALREADY_BOOKED,
    DUPLICATE_REQUEST,
    BOOKING_NOT_FOUND,
    SHOW_NOT_FOUND,
    SHOW_INACTIVE,
    PAST_SHOW,
    INVALID_STATE,
    ACCESS_DENIED,
    VALIDATION_ERROR,
    INTERNAL_SERVER_ERROR
}