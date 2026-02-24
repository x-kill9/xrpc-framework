/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.message;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents an RPC response containing the result of a remote call or an error.
 *
 * <p>The {@code status} field indicates the outcome (see {@link io.github.x_kill9.xrpc.core.constants.StatusCodes}),
 * and the {@code message} field provides a human-readable description in case of error.
 *
 * @author x-kill9
 */
public class Response implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Status code indicating success or failure (see {@link io.github.x_kill9.xrpc.core.constants.StatusCodes}).
     */
    private int status;

    /**
     * Optional human-readable message, typically used for error details.
     */
    private String message;

    /**
     * The actual result of the remote call (if successful).
     */
    private Object result;

    public Response() {
    }

    /**
     * Creates a response with a status and a result (for success).
     *
     * @param status the status code (usually {@code StatusCodes.SUCCESS})
     * @param result the result object
     */
    public Response(int status, Object result) {
        this.status = status;
        this.result = result;
    }

    /**
     * Creates a response with a status and an error message (for failures).
     *
     * @param message the error message
     * @param status  the status code (usually a client or server error code)
     */
    public Response(String message, int status) {
        this.message = message;
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}