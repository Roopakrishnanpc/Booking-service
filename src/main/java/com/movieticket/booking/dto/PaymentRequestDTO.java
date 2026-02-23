package com.movieticket.booking.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDTO {

    private Long bookingId;
    private String userId;
    private BigDecimal amount;
    
}

