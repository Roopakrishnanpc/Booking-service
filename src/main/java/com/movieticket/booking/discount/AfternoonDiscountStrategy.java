package com.movieticket.booking.discount;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Component
@Slf4j
public class AfternoonDiscountStrategy implements DiscountStrategy {

    private static final LocalTime AFTERNOON_LIMIT = LocalTime.of(16, 0);

    @Override
    public BigDecimal apply(BigDecimal currentTotal,
                            List<String> seats,
                            LocalTime showTime) {

        if (showTime.isBefore(AFTERNOON_LIMIT)) {

            BigDecimal discounted = currentTotal.multiply(BigDecimal.valueOf(0.8));

            log.debug("Applying 20% afternoon discount. Final={}", discounted);

            return discounted;
        }

        return currentTotal;
    }
}