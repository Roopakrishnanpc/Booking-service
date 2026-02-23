package com.movieticket.booking.discount;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public interface DiscountStrategy {

    BigDecimal apply(BigDecimal currentTotal,
                     List<String> seats,
                     LocalTime showTime);
}