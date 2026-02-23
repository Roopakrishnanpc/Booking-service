package com.movieticket.booking.dto;

import lombok.*;

import java.time.LocalTime;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequestDTO {

    @NotNull(message = "Show ID is required")
    private Long showId;
    @NotEmpty(message = "Seats cannot be empty")
    @Size(max = 10, message = "Cannot book more than 10 seats")
    private List<@NotEmpty(message = "Seat number cannot be empty") String> seats;

    private LocalTime showTime;

    private String idempotencyKey;
}
