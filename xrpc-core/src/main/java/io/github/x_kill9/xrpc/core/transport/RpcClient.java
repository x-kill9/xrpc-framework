/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.transport;

import io.github.x_kill9.xrpc.core.invocation.context.InvocationContext;
import io.github.x_kill9.xrpc.core.message.Request;
import io.github.x_kill9.xrpc.core.message.Response;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Transport layer interface for sending RPC requests.
 *
 * <p>Implementations handle the actual network communication with remote servers,
 * managing connections and delivering requests asynchronously.
 *
 * @author x-kill9
 */
public interface RpcClient {

    /**
     * Sends an RPC request asynchronously.
     *
     * @param request the request object containing service and method details
     * @param context the invocation context (may contain additional metadata)
     * @return a {@link CompletableFuture} that will complete with the response
     * @throws IOException if an I/O error occurs while sending the request
     */
    CompletableFuture<Response> sendRequest(Request request, InvocationContext context) throws IOException;
}