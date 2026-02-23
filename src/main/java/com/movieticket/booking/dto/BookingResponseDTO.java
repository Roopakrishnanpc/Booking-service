package com.movieticket.booking.dto;

import com.movieticket.booking.domain.BookingStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponseDTO {

    private Long bookingId;

    private BigDecimal amount;

    private BookingStatus status;
}