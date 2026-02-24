/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.exception;

import java.io.Serial;

/**
 * Thrown when an error occurs during serialization or deserialization.
 *
 * @author x-kill9
 */
public class SerializerException extends XRpcException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new SerializerException with the specified detail message.
     *
     * @param message the detail message
     */
    public SerializerException(String message) {
        super(message);
    }

    /**
     * Constructs a new SerializerException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public SerializerException(String message, Throwable cause) {
        super(message, cause);
    }
}