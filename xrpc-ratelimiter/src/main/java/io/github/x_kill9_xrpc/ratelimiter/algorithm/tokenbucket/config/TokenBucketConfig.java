/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9_xrpc.ratelimiter.algorithm.tokenbucket.config;

/**
 * Configuration for the token bucket rate limiter algorithm.
 *
 * @author x-kill9
 */
public class TokenBucketConfig {

    /**
     * Maximum number of tokens the bucket can hold.
     */
    private int capacity;

    /**
     * Number of tokens added per second.
     */
    private double refillRate;

    public TokenBucketConfig() {
    }

    public TokenBucketConfig(int capacity, double refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getRefillRate() {
        return refillRate;
    }

    public void setRefillRate(double refillRate) {
        this.refillRate = refillRate;
    }
}