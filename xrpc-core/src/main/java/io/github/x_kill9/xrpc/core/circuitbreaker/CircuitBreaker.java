/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.circuitbreaker;

import io.github.x_kill9.xrpc.core.circuitbreaker.enums.CircuitBreakerState;

/**
 * Defines the contract for a circuit breaker implementation.
 *
 * <p>A circuit breaker monitors the success/failure of calls to a remote service
 * and temporarily blocks requests when the failure rate exceeds a threshold,
 * allowing the service time to recover.
 *
 * @author x-kill9
 */
public interface CircuitBreaker {

    /**
     * Determines whether a request should be allowed to proceed.
     *
     * @return {@code true} if the request is allowed, {@code false} if the circuit is open and the request should be blocked
     */
    boolean allowRequest();

    /**
     * Notifies the circuit breaker of a successful call.
     *
     * <p>This may influence the state transition, e.g., moving from {@code HALF_OPEN} back to {@code CLOSED}.
     */
    void onSuccess();

    /**
     * Notifies the circuit breaker of a failed call.
     *
     * <p>The failure is recorded and may trigger a state transition (e.g., from {@code CLOSED} to {@code OPEN}
     * if the failure threshold is reached).
     *
     * @param t the exception or throwable that caused the failure
     */
    void onFailure(Throwable t);

    /**
     * Returns the current state of the circuit breaker.
     *
     * @return the current state ({@link CircuitBreakerState})
     */
    CircuitBreakerState getState();

    /**
     * Returns the name of this circuit breaker implementation.
     *
     * @return a unique identifier for the circuit breaker type (e.g., "slidingWindow", "countBased")
     */
    String name();
}