/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.constants;

/**
 * Standard status codes for RPC responses.
 *
 * <p>These codes are inspired by HTTP status codes for familiarity and indicate
 * the outcome of a remote procedure call. They are used in {@link io.github.x_kill9.xrpc.core.message.Response}
 * to convey success or failure to the client.
 *
 * @author x-kill9
 */
public final class StatusCodes {

    private StatusCodes() {
        // Prevent instantiation
    }

    /**
     * Successful execution (200 OK).
     */
    public static final int SUCCESS = 200;

    /**
     * Request format error, e.g., deserialization failure (400 Bad Request).
     */
    public static final int BAD_REQUEST = 400;

    /**
     * Target service not found (404 Not Found).
     */
    public static final int SERVICE_NOT_FOUND = 404;

    /**
     * Requested method not found on the service (405 Method Not Allowed).
     */
    public static final int METHOD_NOT_FOUND = 405;

    /**
     * Request timeout (408 Request Timeout).
     */
    public static final int TIMEOUT = 408;

    /**
     * Business logic failure, e.g., the service implementation threw an exception (503 Service Unavailable).
     */
    public static final int FAILURE = 503;

    /**
     * Internal server error due to unexpected system failure (500 Internal Server Error).
     */
    public static final int INTERNAL_ERROR = 500;

    /**
     * Alias for {@link #INTERNAL_ERROR}, kept for backward compatibility.
     */
    public static final int SERVER_ERROR = INTERNAL_ERROR;
}