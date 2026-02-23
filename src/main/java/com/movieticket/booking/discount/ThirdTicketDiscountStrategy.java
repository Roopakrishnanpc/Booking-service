package com.movieticket.booking.discount;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Component
@Slf4j
public class ThirdTicketDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal BASE_PRICE = BigDecimal.valueOf(200);

    @Override
    public BigDecimal apply(BigDecimal currentTotal,
                            List<String> seats,
                            LocalTime showTime) {

        if (seats.size() < 3) {
            return currentTotal;
        }

        BigDecimal discount = BASE_PRICE.multiply(BigDecimal.valueOf(0.5));

        log.debug("Applying 50% discount on 3rd ticket. Discount={}", discount);

        return currentTotal.subtract(discount);
    }
}