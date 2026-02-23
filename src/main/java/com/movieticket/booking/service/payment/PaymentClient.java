package com.movieticket.booking.service.payment;

import com.movieticket.booking.dto.PaymentRequestDTO;
import com.movieticket.booking.dto.PaymentResponseDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentClient {

    private final WebClient.Builder builder;

    // =======================
    // CHARGE
    // =======================

    @CircuitBreaker(name = "paymentService", fallbackMethod = "chargeFallback")
    @Retry(name = "paymentService")
    public Mono<PaymentResponseDTO> charge(
            PaymentRequestDTO request,
            String token
    ) {

        log.info("Calling PAYMENT CHARGE bookingId={} amount={}",
                request.getBookingId(),
                request.getAmount());

        return builder.build()
                .post()
                .uri("http://localhost:8085/api/v1/payments/charge")
                .headers(headers -> headers.setBearerAuth(token)) // ✅ CORRECT
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponseDTO.class)
                .doOnSuccess(r ->
                        log.info("Charge response bookingId={} success={}",
                                r.getBookingId(),
                                r.isSuccess())
                );
    }

    // =======================
    // REFUND
    // =======================

    @CircuitBreaker(name = "paymentService", fallbackMethod = "refundFallback")
    @Retry(name = "paymentService")
    public Mono<PaymentResponseDTO> refund(
            PaymentRequestDTO request,
            String token
    ) {

        log.info("Calling PAYMENT REFUND bookingId={} amount={}",
                request.getBookingId(),
                request.getAmount());

        return builder.build()
                .post()
                .uri("http://localhost:8085/api/v1/payments/refund")
                .headers(headers -> headers.setBearerAuth(token)) // ✅ CORRECT
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponseDTO.class)
                .doOnSuccess(r ->
                        log.info("Refund response bookingId={} success={}",
                                r.getBookingId(),
                                r.isSuccess())
                );
    }

    // =======================
    // FALLBACKS
    // =======================

    public Mono<PaymentResponseDTO> chargeFallback(
            PaymentRequestDTO request,
            String token,
            Throwable ex
    ) {
        log.error("Charge FALLBACK bookingId={}", request.getBookingId(), ex);

        return Mono.just(
                PaymentResponseDTO.builder()
                        .bookingId(request.getBookingId())
                        .success(false)
                        .build()
        );
    }

    public Mono<PaymentResponseDTO> refundFallback(
            PaymentRequestDTO request,
            String token,
            Throwable ex
    ) {
        log.error("Refund FALLBACK bookingId={}", request.getBookingId(), ex);

        return Mono.just(
                PaymentResponseDTO.builder()
                        .bookingId(request.getBookingId())
                        .success(false)
                        .build()
        );
    }
}