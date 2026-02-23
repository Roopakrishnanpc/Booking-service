package com.movieticket.booking.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {

    private Long bookingId;
    private String status;
    private boolean success;

    private Long transactionId;
}