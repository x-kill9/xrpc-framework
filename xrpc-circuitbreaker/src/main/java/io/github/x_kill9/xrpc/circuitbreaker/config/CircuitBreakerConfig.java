/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.circuitbreaker.config;

import io.github.x_kill9.xrpc.core.invocation.interceptor.config.InterceptorConfig;

import java.util.Map;

/**
 * XRPC Circuit breaker configuration.
 *
 * @author x-kill9
 */
public class CircuitBreakerConfig implements InterceptorConfig {
    private String type = "slidingWindow";
    private Map<String, Object> params;

    @Override
    public void validate() {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("CircuitBreaker type must not be empty");
        }
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}