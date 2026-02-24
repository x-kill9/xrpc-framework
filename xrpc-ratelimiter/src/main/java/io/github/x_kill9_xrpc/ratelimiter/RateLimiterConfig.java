/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9_xrpc.ratelimiter;

import io.github.x_kill9.xrpc.core.invocation.interceptor.config.InterceptorConfig;

import java.util.Map;

/**
 * Configuration for the rate limiter interceptor.
 *
 * @author x-kill9
 */
public class RateLimiterConfig implements InterceptorConfig {

    /**
     * Type of rate limiter algorithm (e.g., "tokenBucket").
     */
    private String type;

    /**
     * Algorithm-specific parameters.
     */
    private Map<String, Object> params;

    @Override
    public void validate() {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Rate limiter type must not be empty");
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}