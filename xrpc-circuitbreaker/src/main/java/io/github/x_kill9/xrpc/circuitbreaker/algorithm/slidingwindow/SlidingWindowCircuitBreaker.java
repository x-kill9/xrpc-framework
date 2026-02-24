/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.circuitbreaker.algorithm.slidingwindow;

import io.github.x_kill9.xrpc.circuitbreaker.algorithm.slidingwindow.config.SlidingWindowConfig;
import io.github.x_kill9.xrpc.core.circuitbreaker.CircuitBreaker;
import io.github.x_kill9.xrpc.core.circuitbreaker.enums.CircuitBreakerState;
import io.github.x_kill9.xrpc.core.util.MapToObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Sliding Window Circuit Breaker implementation.
 * Tracks failures within a fixed time window and trips the circuit when
 * the failure count reaches the configured threshold.
 *
 * @author x-kill9
 */
public class SlidingWindowCircuitBreaker implements CircuitBreaker {
    private static final Logger logger = LoggerFactory.getLogger(SlidingWindowCircuitBreaker.class);
    private final int failureThreshold;
    private final long timeoutMs;
    private final int windowSize;   // window size in milliseconds

    private final AtomicReference<CircuitBreakerState> state = new AtomicReference<>(CircuitBreakerState.CLOSED);
    private final AtomicLong lastFailureTime = new AtomicLong(0);

    // Sliding window: circular buffer of failure timestamps
    private final long[] timestamps;
    private final AtomicInteger tail = new AtomicInteger(0);  // next write position

    public SlidingWindowCircuitBreaker(Map<String, Object> params) {
        SlidingWindowConfig config = MapToObjectMapper.mapToObject(params, SlidingWindowConfig.class);
        this.failureThreshold = config.getFailureThreshold();
        this.timeoutMs = config.getTimeoutMs();
        this.windowSize = config.getWindowSize();
        this.timestamps = new long[failureThreshold];  // A threshold value is used as the buffer size
    }

    @Override
    public boolean allowRequest() {
        CircuitBreakerState current = state.get();
        if (current == CircuitBreakerState.CLOSED) {
            return true;
        } else if (current == CircuitBreakerState.OPEN) {
            // Check if timeout elapsed to try half-open
            if (System.currentTimeMillis() - lastFailureTime.get() > timeoutMs) {
                return state.compareAndSet(CircuitBreakerState.OPEN, CircuitBreakerState.HALF_OPEN);
            }
            return false;
        } else { // HALF_OPEN
            return true;
        }
    }

    @Override
    public void onSuccess() {
        CircuitBreakerState current = state.get();
        if (current == CircuitBreakerState.HALF_OPEN) {
            // A successful call in half-open state resets breaker to closed
            if (state.compareAndSet(CircuitBreakerState.HALF_OPEN, CircuitBreakerState.CLOSED)) {
                resetWindow();
            }
        }
    }

    @Override
    public void onFailure(Throwable t) {
        long now = System.currentTimeMillis();
        recordFailure(now);
        lastFailureTime.set(now);
        CircuitBreakerState current = state.get();
        logger.debug("onFailure: current state={}, failuresInWindow={}", current, countFailuresInWindow(now));
        if (current == CircuitBreakerState.HALF_OPEN) {
            state.set(CircuitBreakerState.OPEN);
            logger.debug("HALF_OPEN -> OPEN");
        } else if (current == CircuitBreakerState.CLOSED) {
            int failuresInWindow = countFailuresInWindow(now);
            if (failuresInWindow >= failureThreshold) {
                state.set(CircuitBreakerState.OPEN);
                logger.debug("CLOSED -> OPEN, failuresInWindow={}", failuresInWindow);
            }
        }
    }

    @Override
    public CircuitBreakerState getState() {
        return state.get();
    }

    @Override
    public String name() {
        return "slidingWindow";
    }

    /**
     * Records a failure timestamp into the sliding window.
     */
    private synchronized void recordFailure(long timestamp) {
        timestamps[tail.getAndUpdate(i -> (i + 1) % timestamps.length)] = timestamp;
    }

    /**
     * Counts failures that occurred within the last windowSize milliseconds.
     */
    private synchronized int countFailuresInWindow(long now) {
        int count = 0;
        long windowStart = now - windowSize;
        for (long ts : timestamps) {
            if (ts > windowStart) {
                count++;
            }
        }
        return count;
    }

    /**
     * Resets the sliding window (clears all timestamps).
     */
    private synchronized void resetWindow() {
        Arrays.fill(timestamps, 0);
        tail.set(0);
    }
}