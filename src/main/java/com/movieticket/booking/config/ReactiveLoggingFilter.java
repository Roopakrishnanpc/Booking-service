package com.movieticket.booking.config;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

//@Component
public class ReactiveLoggingFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             WebFilterChain chain) {

        String traceId = UUID.randomUUID().toString();
        String spanId = UUID.randomUUID().toString();

        MDC.put("traceId", traceId);
        MDC.put("spanId", spanId);

        return chain.filter(exchange)
                .doFinally(signalType -> MDC.clear());
    }
}