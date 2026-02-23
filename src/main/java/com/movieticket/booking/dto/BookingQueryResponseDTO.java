package com.movieticket.booking.dto;

import com.movieticket.booking.domain.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingQueryResponseDTO {

    private Long bookingId;

    private Long showId;

    private BigDecimal amount;

    private BookingStatus status;

    private LocalDateTime createdAt;
}