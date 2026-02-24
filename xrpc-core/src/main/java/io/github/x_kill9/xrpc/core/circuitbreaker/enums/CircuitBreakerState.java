/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.circuitbreaker.enums;

/**
 * Represents the possible states of a circuit breaker.
 *
 * <p>The circuit breaker transitions between these states based on invocation results:
 * <ul>
 *   <li>{@link #CLOSED} - The circuit is closed, requests are allowed to pass normally.
 *       Failures are counted, and when a threshold is reached, the state transitions to {@link #OPEN}.</li>
 *   <li>{@link #OPEN} - The circuit is open, requests are blocked immediately.
 *       After a configured timeout, the state transitions to {@link #HALF_OPEN}.</li>
 *   <li>{@link #HALF_OPEN} - A limited number of requests are allowed to test if the underlying service has recovered.
 *       If a request succeeds, the circuit transitions back to {@link #CLOSED}; if it fails, it returns to {@link #OPEN}.</li>
 * </ul>
 *
 * @author x-kill9
 */
public enum CircuitBreakerState {
    CLOSED,
    OPEN,
    HALF_OPEN
}