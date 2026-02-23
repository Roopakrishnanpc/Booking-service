//package com.movieticket.booking.service.queue;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Component;
//
//import java.time.Duration;
//import java.util.UUID;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class DistributedBookingQueue {
//
//    private final StringRedisTemplate redisTemplate;
//
//    private static final String QUEUE_KEY = "booking:queue";
//    private static final String ACTIVE_KEY = "booking:active";
//
//    private static final int MAX_CONCURRENT = 50;
//
//    public String enter() {
//
//        String ticketId = UUID.randomUUID().toString();
//
//        // Add user to queue
//        redisTemplate.opsForList().rightPush(QUEUE_KEY, ticketId);
//
//        Long activeCount = redisTemplate.opsForValue()
//                .increment(ACTIVE_KEY);
//
//        if (activeCount != null && activeCount > MAX_CONCURRENT) {
//
//            // Exceeded limit → revert increment
//            redisTemplate.opsForValue().decrement(ACTIVE_KEY);
//
//            throw new RuntimeException(
//                    "High demand. You are placed in virtual queue.");
//        }
//
//        log.info("User entered distributed booking queue. Active={}", activeCount);
//
//        return ticketId;
//    }
//
//    public void exit() {
//
//        redisTemplate.opsForValue().decrement(ACTIVE_KEY);
//
//        log.info("User exited distributed booking queue");
//    }
//
//    public Long getActiveCount() {
//        return redisTemplate.opsForValue().getOperations()
//                .opsForValue()
//                .increment(ACTIVE_KEY, 0);
//    }
//}