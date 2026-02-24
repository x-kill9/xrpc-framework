/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.exception;

import java.io.Serial;

/**
 * Thrown when an error occurs during message decoding.
 *
 * @author x-kill9
 */
public class DecoderException extends XRpcException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new DecoderException with the specified detail message.
     *
     * @param message the detail message
     */
    public DecoderException(String message) {
        super(message);
    }

    /**
     * Constructs a new DecoderException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public DecoderException(String message, Throwable cause) {
        super(message, cause);
    }
}