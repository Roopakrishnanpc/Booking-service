package com.movieticket.booking.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.movieticket.booking.client.dto.ShowResponseDTO;

import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TheatreClient {

    private final WebClient.Builder webClient;

    @CircuitBreaker(name = "theatreService", fallbackMethod = "fallback")
    public Mono<ShowResponseDTO> getShowById(Long showId, String token) {
        return webClient.build().get()
                .uri("http://localhost:8082/api/v1/tickets/query/show/{id}", showId)
                .header("Authorization", "Bearer "+token)
                //.headers(headers -> headers.setBearerAuth(token))  
                .retrieve()
                .bodyToMono(ShowResponseDTO.class)
                .doOnNext(System.out::println);
    }

    private Mono<ShowResponseDTO> fallback(Long showId, String token, Throwable ex) {
        return Mono.error(new RuntimeException("Theatre service unavailable"));
    }
}

