package com.movieticket.booking.event;

import com.movieticket.booking.dto.PaymentRequestDTO;
import com.movieticket.booking.dto.PaymentResponseDTO;
import com.movieticket.booking.service.payment.PaymentClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

//@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentListener {

    private final PaymentClient paymentClient;
    private final ApplicationEventPublisher publisher;

    @Async
    //@EventListener
    public void handle(BookingCreatedEvent event, String token) {

        log.info("Processing payment bookingId={}", event.bookingId());

        boolean success = false;

        try {

            PaymentResponseDTO response =
                    paymentClient.charge(
                            new PaymentRequestDTO(
                                    event.bookingId(),
                                    event.userId(),
                                    event.amount()
                            ), token
                    ).block();

            success = response != null && response.isSuccess();

        } catch (Exception ex) {
            log.error("Payment exception bookingId={}", event.bookingId(), ex);
            success = false;
        }

        log.info("Payment result bookingId={} success={}",
                event.bookingId(), success);

        publisher.publishEvent(
                new PaymentCompletedEvent(
                        event.bookingId(),
                        success
                )
        );
    }
}