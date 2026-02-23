package com.movieticket.booking.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking",
       indexes = {
           @Index(name = "idx_booking_user", columnList = "userId"),
           @Index(name = "idx_booking_idempotency", columnList = "idempotencyKey")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long showId;

    private String userId;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Column(unique = true, nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, length = 1000)
    private String seats;  // comma separated: "A1,A2,A3"
    
    @Column(name="payment_transaction_id")
    private Long paymentTransactionId;
}