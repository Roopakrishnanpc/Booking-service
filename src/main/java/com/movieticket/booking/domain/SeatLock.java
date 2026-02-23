package com.movieticket.booking.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seat_lock",
       uniqueConstraints = {
           @UniqueConstraint(
               name = "uq_show_seat",
               columnNames = {"showId", "seatNumber"}
           )
       },
       indexes = {
           @Index(name = "idx_seat_show", columnList = "showId")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long showId;

    private String seatNumber;

    private boolean locked;
}