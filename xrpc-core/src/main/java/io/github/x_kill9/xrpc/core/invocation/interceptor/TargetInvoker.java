/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.invocation.interceptor;

import io.github.x_kill9.xrpc.core.invocation.context.InvocationContext;

/**
 * Functional interface for the final target invocation in an interceptor chain.
 *
 * @author x-kill9
 */
@FunctionalInterface
public interface TargetInvoker {

    /**
     * Invokes the target (usually the remote service) with the given context.
     *
     * @param context the invocation context
     * @return the result of the invocation
     * @throws Throwable if an error occurs during invocation
     */
    Object invoke(InvocationContext context) throws Throwable;
}