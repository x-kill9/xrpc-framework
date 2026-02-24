/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.circuitbreaker;

import io.github.x_kill9.xrpc.circuitbreaker.config.CircuitBreakerConfig;
import io.github.x_kill9.xrpc.circuitbreaker.exception.CircuitBreakerOpenException;
import io.github.x_kill9.xrpc.core.circuitbreaker.CircuitBreaker;
import io.github.x_kill9.xrpc.core.invocation.context.InvocationContext;
import io.github.x_kill9.xrpc.core.invocation.interceptor.Interceptor;
import io.github.x_kill9.xrpc.core.invocation.interceptor.InterceptorChain;
import io.github.x_kill9.xrpc.core.spi.ExtensionLoader;

import java.util.Map;

/**
 * The circuit breaker.
 *
 * @author x-kill9
 */
public class CircuitBreakerInterceptor implements Interceptor<CircuitBreakerConfig> {
    private CircuitBreakerConfig config;
    private CircuitBreaker breaker;

    @Override
    public String getName() {
        return "circuitBreaker";
    }

    @Override
    public Class<CircuitBreakerConfig> getConfigClass() {
        return CircuitBreakerConfig.class;
    }

    @Override
    public void setConfig(CircuitBreakerConfig config) {
        this.config = config;
        this.breaker = createCircuitBreaker(config);
    }

    @Override
    public CircuitBreakerConfig getConfig() {
        return config;
    }

    private CircuitBreaker createCircuitBreaker(CircuitBreakerConfig config) {
        String type = config.getType();
        Map<String, Object> params = config.getParams();
        return ExtensionLoader.getExtensionLoader(CircuitBreaker.class)
                .newExtension(type, params); // Call the SPI method with a Map parameter
    }

    @Override
    public Object intercept(InvocationContext context, InterceptorChain chain) throws Throwable {
        if (!breaker.allowRequest()) {
            throw new CircuitBreakerOpenException("Circuit breaker is OPEN, request blocked");
        }
        try {
            Object result = chain.proceed(context);
            breaker.onSuccess();
            return result;
        } catch (Throwable t) {
            breaker.onFailure(t);
            throw t;
        }
    }
}