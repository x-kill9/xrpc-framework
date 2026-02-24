/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.flowcontrol;

/**
 * Rate limiter interface for controlling request flow.
 *
 * <p>All rate limiter implementations in the framework must implement this interface.
 *
 * @author x-kill9
 */
public interface RateLimiter {

    /**
     * Attempts to acquire a permit without blocking.
     *
     * @return {@code true} if the permit was acquired and the request can proceed,
     *         {@code false} if the request is rate-limited
     */
    boolean tryAcquire();
}