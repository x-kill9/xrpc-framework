/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.circuitbreaker.exception;

/**
 * Thrown when a request is rejected because the circuit breaker is OPEN.
 *
 * @author x-kill9
 */
public class CircuitBreakerOpenException extends RuntimeException {
    public CircuitBreakerOpenException(String message) {
        super(message);
    }

    public CircuitBreakerOpenException(String message, Throwable cause) {
        super(message, cause);
    }
}
