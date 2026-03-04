package com.sankalpam.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GeoapifyRateLimiter Tests")
class GeoapifyRateLimiterTest {

    private GeoapifyRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new GeoapifyRateLimiter();
    }

    @Test
    @DisplayName("First call should be allowed")
    void tryAcquire_FirstCall_ReturnsTrue() {
        assertTrue(rateLimiter.tryAcquire());
    }

    @Test
    @DisplayName("Multiple calls within limit should be allowed")
    void tryAcquire_MultipleCalls_AllAllowed() {
        for (int i = 0; i < 100; i++) {
            assertTrue(rateLimiter.tryAcquire(), "Call " + i + " should be allowed");
        }
    }

    @Test
    @DisplayName("Remaining calls should decrease after each call")
    void getRemainingCalls_DecreasesAfterEachCall() {
        int initial = rateLimiter.getRemainingCalls();
        rateLimiter.tryAcquire();
        int afterOne = rateLimiter.getRemainingCalls();
        assertEquals(initial - 1, afterOne);
    }

    @Test
    @DisplayName("Remaining calls should return non-negative value")
    void getRemainingCalls_NeverNegative() {
        assertTrue(rateLimiter.getRemainingCalls() >= 0);
    }

    @Test
    @DisplayName("Initial remaining calls should be 2000")
    void getRemainingCalls_InitialValue() {
        assertEquals(2000, rateLimiter.getRemainingCalls());
    }
}

