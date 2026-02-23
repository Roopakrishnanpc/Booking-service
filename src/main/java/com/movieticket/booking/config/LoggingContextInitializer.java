package com.movieticket.booking.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LoggingContextInitializer {

    @PostConstruct
    public void init() {
        MDC.put("traceId", UUID.randomUUID().toString());
        MDC.put("spanId", UUID.randomUUID().toString());
    }
}