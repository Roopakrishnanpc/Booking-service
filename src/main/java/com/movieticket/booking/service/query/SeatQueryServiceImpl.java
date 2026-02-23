package com.movieticket.booking.service.query;

import com.movieticket.booking.repository.SeatLockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatQueryServiceImpl implements SeatQueryService {

    private final SeatLockRepository seatLockRepository;

    @Override
    @Cacheable(value = "seatAvailability", key = "#showId")
    public Flux<String> getAvailableSeats(Long showId) {

        log.info("Fetching seat availability from DB for show={}", showId);

        return Mono.fromCallable(() ->{
        		log.info("Fetching seat availablility from DB for show={}", showId);
                      return  seatLockRepository.findByShowId(showId)
                                .stream()
                                .filter(seat -> !seat.isLocked())
                                .map(seat -> seat.getSeatNumber())
                                .toList();
        })
        .subscribeOn(Schedulers.boundedElastic())
        		.flatMapMany(Flux::fromIterable);
    }
}