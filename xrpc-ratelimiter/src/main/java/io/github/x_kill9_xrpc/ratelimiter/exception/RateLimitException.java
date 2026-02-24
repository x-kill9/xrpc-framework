/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9_xrpc.ratelimiter.exception;

import io.github.x_kill9.xrpc.core.exception.XRpcException;
import java.io.Serial;

/**
 * Thrown when a request is rejected by a rate limiter.
 *
 * @author x-kill9
 */
public class RateLimitException extends XRpcException {

    @Serial
    private static final long serialVersionUID = 1L;

    public RateLimitException(String message) {
        super(message);
    }

    public RateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}