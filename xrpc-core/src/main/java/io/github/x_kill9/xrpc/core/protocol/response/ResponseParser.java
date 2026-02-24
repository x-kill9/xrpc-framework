/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.protocol.response;

import io.github.x_kill9.xrpc.core.constants.StatusCodes;
import io.github.x_kill9.xrpc.core.exception.XRpcException;
import io.github.x_kill9.xrpc.core.message.Response;

/**
 * Utility class for parsing {@link Response} objects received from the server.
 *
 * <p>If the response indicates success, the result is returned; otherwise,
 * an exception is thrown containing the error status and message.
 *
 * @author x-kill9
 */
public final class ResponseParser {

    private ResponseParser() {
    }

    /**
     * Parses a response and returns the result or throws an exception on failure.
     *
     * @param response the response object received from the server
     * @return the result of the remote call if the status is {@link StatusCodes#SUCCESS}
     * @throws XRpcException if the response status is not successful, containing the error details
     */
    public static Object parse(Response response) {
        if (response.getStatus() == StatusCodes.SUCCESS) {
            return response.getResult();
        }
        String errorMsg = response.getMessage() != null ? response.getMessage() : "Unknown error";
        throw new XRpcException("Request failed with status " + response.getStatus() + ": " + errorMsg);
    }
}