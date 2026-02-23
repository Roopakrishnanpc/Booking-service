package com.movieticket.booking.client.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ShowResponseDTO {

    private Long id;
    private Long theatreId;
    private String movieName;
    private String language;
    private String genre;
    private LocalDate showDate;
    private LocalTime showTime;
    private boolean active;
}