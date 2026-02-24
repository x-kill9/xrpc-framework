/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.circuitbreaker.algorithm.slidingwindow.config;

import io.github.x_kill9.xrpc.core.invocation.interceptor.config.InterceptorConfig;

/**
 * The Sliding Window Circuit Breaker Algorithm.
 *
 * @author x-kill9
 */
public class SlidingWindowConfig implements InterceptorConfig {
    private int failureThreshold = 5;
    private long timeoutMs = 10000;
    private int windowSize = 10000;

    public SlidingWindowConfig() {
    }

    @Override
    public void validate() {
        if (failureThreshold <= 0) {
            throw new IllegalArgumentException("failureThreshold must be positive");
        }
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("timeoutMs must be positive");
        }
        if (windowSize <= 0) {
            throw new IllegalArgumentException("windowSize must be positive");
        }
    }

    public int getFailureThreshold() {
        return failureThreshold;
    }

    public void setFailureThreshold(int failureThreshold) {
        this.failureThreshold = failureThreshold;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }
}