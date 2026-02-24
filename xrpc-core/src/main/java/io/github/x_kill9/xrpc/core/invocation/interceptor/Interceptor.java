/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.invocation.interceptor;

import io.github.x_kill9.xrpc.core.invocation.context.InvocationContext;
import io.github.x_kill9.xrpc.core.invocation.interceptor.config.InterceptorConfig;

/**
 * Interceptor interface for processing RPC invocations.
 *
 * <p>Interceptors can perform pre-processing, post-processing, or modify the invocation flow.
 * They are arranged in a chain and each interceptor can decide to proceed with the chain or
 * short-circuit the execution.
 *
 * @param <C> the type of configuration object this interceptor accepts
 * @author x-kill9
 */
public interface Interceptor<C extends InterceptorConfig> {

    /**
     * Returns the name of this interceptor.
     *
     * @return the interceptor name
     */
    String getName();

    /**
     * Returns the configuration class type expected by this interceptor.
     *
     * @return the configuration class, or {@code null} if no configuration is needed
     */
    Class<C> getConfigClass();

    /**
     * Sets the configuration for this interceptor.
     *
     * @param config the configuration object
     */
    void setConfig(C config);

    /**
     * Returns the current configuration of this interceptor.
     *
     * @return the configuration object, or {@code null} if not set
     */
    C getConfig();

    /**
     * Intercepts an invocation.
     *
     * <p>Implementations may perform actions before and after invoking the next interceptor
     * via {@link InterceptorChain#proceed(InvocationContext)}. They may also short-circuit
     * the chain by returning a value directly or throwing an exception.
     *
     * @param context the invocation context
     * @param chain   the interceptor chain to continue the execution
     * @return the result of the invocation
     * @throws Throwable if an error occurs during interception or invocation
     */
    Object intercept(InvocationContext context, InterceptorChain chain) throws Throwable;
}