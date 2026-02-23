package com.movieticket.booking.discount;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.List;
@Component
@RequiredArgsConstructor
@Slf4j
@Primary
public class CompositeDiscountStrategy implements DiscountStrategy {

    private final ThirdTicketDiscountStrategy thirdTicketDiscount;
    private final AfternoonDiscountStrategy afternoonDiscount;

    @Override
    public BigDecimal apply(BigDecimal basePrice,
                            List<String> seats,
                            LocalTime showTime) {

        BigDecimal total =
                basePrice.multiply(BigDecimal.valueOf(seats.size()));

        total = thirdTicketDiscount.apply(total, seats, showTime);
        total = afternoonDiscount.apply(total, seats, showTime);

        log.info("Final discounted amount={}", total);

        return total;
    }
}