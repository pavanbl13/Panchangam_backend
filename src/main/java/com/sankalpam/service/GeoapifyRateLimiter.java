package com.sankalpam.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Rate limiter for Geoapify API calls.
 * Limits to a configurable number of calls per day (default 2000).
 * Resets daily at midnight.
 */
@Slf4j
@Component
public class GeoapifyRateLimiter {

    private static final int MAX_CALLS_PER_DAY = 2000;

    private final AtomicInteger callCount = new AtomicInteger(0);
    private final AtomicReference<LocalDate> currentDate = new AtomicReference<>(LocalDate.now());

    /**
     * Try to acquire a permit for an API call.
     * @return true if the call is allowed, false if rate limit exceeded
     */
    public boolean tryAcquire() {
        resetIfNewDay();
        int count = callCount.incrementAndGet();
        if (count > MAX_CALLS_PER_DAY) {
            callCount.decrementAndGet();
            log.warn("Geoapify API rate limit exceeded. {}/{} calls used today.", count - 1, MAX_CALLS_PER_DAY);
            return false;
        }
        log.debug("Geoapify API call {}/{} for today", count, MAX_CALLS_PER_DAY);
        return true;
    }

    /**
     * Get the number of remaining API calls for today.
     * @return remaining calls
     */
    public int getRemainingCalls() {
        resetIfNewDay();
        return Math.max(0, MAX_CALLS_PER_DAY - callCount.get());
    }

    private void resetIfNewDay() {
        LocalDate today = LocalDate.now();
        LocalDate stored = currentDate.get();
        if (!today.equals(stored)) {
            if (currentDate.compareAndSet(stored, today)) {
                int previousCount = callCount.getAndSet(0);
                log.info("Geoapify rate limiter reset for new day. Previous day used {} calls.", previousCount);
            }
        }
    }
}

