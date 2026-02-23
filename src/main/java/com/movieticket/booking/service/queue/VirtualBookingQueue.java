package com.movieticket.booking.service.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

@Component
@Slf4j
public class VirtualBookingQueue {

    private static final int MAX_ACTIVE_BOOKINGS = 50;

    private final Semaphore semaphore =
            new Semaphore(MAX_ACTIVE_BOOKINGS, true);

    private final Queue<String> waitingQueue =
            new ConcurrentLinkedQueue<>();

    public String enter() {

        String ticketId = UUID.randomUUID().toString();

        waitingQueue.add(ticketId);

        log.info("User added to virtual queue ticketId={}", ticketId);

        boolean acquired = semaphore.tryAcquire();

        if (!acquired) {
            log.warn("Queue full. User must wait. ticketId={}", ticketId);
            throw new RuntimeException("High demand. Please try again.");
        }

        // Remove from waiting once allowed
        waitingQueue.remove(ticketId);

        log.info("User allowed to proceed ticketId={} active={}",
                ticketId,
                MAX_ACTIVE_BOOKINGS - semaphore.availablePermits());

        return ticketId;
    }

    public void exit() {
        semaphore.release();
        log.info("User exited. Active now={}",
                MAX_ACTIVE_BOOKINGS - semaphore.availablePermits());
    }

    public int getActiveCount() {
        return MAX_ACTIVE_BOOKINGS - semaphore.availablePermits();
    }

    public int getWaitingCount() {
        return waitingQueue.size();
    }
}