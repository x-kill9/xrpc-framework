/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.exception;

import java.io.Serial;

/**
 * Base exception class for all XRPC framework exceptions.
 *
 * @author x-kill9
 */
public class XRpcException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new XRPCException with no detail message.
     */
    public XRpcException() {
        super();
    }

    /**
     * Constructs a new XRPCException with the specified detail message.
     *
     * @param message the detail message
     */
    public XRpcException(String message) {
        super(message);
    }

    /**
     * Constructs a new XRpcException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public XRpcException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new XRpcException with the specified cause.
     *
     * @param cause the cause
     */
    public XRpcException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new XRpcException with the specified detail message,
     * cause, suppression enabled or disabled, and writable stack trace
     * enabled or disabled.
     *
     * @param message            the detail message
     * @param cause              the cause
     * @param enableSuppression  whether suppression is enabled
     * @param writableStackTrace whether the stack trace should be writable
     */
    public XRpcException(String message, Throwable cause,
                         boolean enableSuppression,
                         boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}