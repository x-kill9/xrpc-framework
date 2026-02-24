/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.invocation.interceptor;

import io.github.x_kill9.xrpc.core.invocation.context.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Chain of interceptors that processes an invocation sequentially.
 *
 * <p>The chain maintains an index of the current interceptor and invokes them in order.
 * After all interceptors have been processed, the target invoker is called.
 *
 * @author x-kill9
 */
public class InterceptorChain {
    private static final Logger logger = LoggerFactory.getLogger(InterceptorChain.class);

    private final List<Interceptor<?>> interceptors;
    private final TargetInvoker targetInvoker;
    private int currentIndex = 0;

    /**
     * Creates a new interceptor chain.
     *
     * @param interceptors  the list of interceptors (may be empty)
     * @param targetInvoker the final target invoker
     */
    public InterceptorChain(List<Interceptor<?>> interceptors, TargetInvoker targetInvoker) {
        this.interceptors = interceptors != null ? interceptors : List.of();
        this.targetInvoker = targetInvoker;
        logger.debug("InterceptorChain created with {} interceptors", this.interceptors.size());
    }

    /**
     * Proceeds with the next interceptor in the chain, or invokes the target if all are done.
     *
     * @param context the invocation context
     * @return the result of the invocation
     * @throws Throwable if an error occurs
     */
    public Object proceed(InvocationContext context) throws Throwable {
        if (currentIndex < interceptors.size()) {
            Interceptor<?> interceptor = interceptors.get(currentIndex);
            currentIndex++;
            logger.trace("Executing interceptor [{}/{}]: {}",
                    currentIndex, interceptors.size(), interceptor.getClass().getSimpleName());
            return interceptor.intercept(context, this);
        } else {
            logger.trace("All interceptors executed, invoking target");
            return targetInvoker.invoke(context);
        }
    }
}