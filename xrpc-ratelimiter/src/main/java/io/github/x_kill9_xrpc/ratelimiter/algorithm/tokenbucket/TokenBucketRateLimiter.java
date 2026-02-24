/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9_xrpc.ratelimiter.algorithm.tokenbucket;

import io.github.x_kill9.xrpc.core.flowcontrol.RateLimiter;
import io.github.x_kill9.xrpc.core.util.MapToObjectMapper;
import io.github.x_kill9_xrpc.ratelimiter.algorithm.tokenbucket.config.TokenBucketConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Token bucket rate limiter implementation.
 *
 * <p>The bucket is initially full. Each {@link #tryAcquire()} consumes one token if available.
 * Tokens are replenished continuously based on the refill rate.
 *
 * @author x-kill9
 */
public class TokenBucketRateLimiter implements RateLimiter {

    private static final Logger logger = LoggerFactory.getLogger(TokenBucketRateLimiter.class);

    private int capacity = 100;
    private double refillRate = 10.0;
    private double tokens;
    private long lastRefillTime;

    public TokenBucketRateLimiter() {
    }

    public TokenBucketRateLimiter(Map<String, Object> params) {
        TokenBucketConfig config = MapToObjectMapper.mapToObject(params, TokenBucketConfig.class);
        this.capacity = config.getCapacity();
        this.refillRate = config.getRefillRate();
        this.tokens = capacity;
        this.lastRefillTime = System.nanoTime();
    }

    @Override
    public synchronized boolean tryAcquire() {
        refill();
        if (tokens >= 1) {
            tokens -= 1;
            logger.debug("tryAcquire: true, tokens left: {}", tokens);
            return true;
        } else {
            logger.debug("tryAcquire: false, tokens: {}", tokens);
            return false;
        }
    }

    /**
     * Refills tokens based on elapsed time.
     */
    private void refill() {
        long now = System.nanoTime();
        double elapsedSeconds = (now - lastRefillTime) / 1_000_000_000.0;
        double tokensToAdd = elapsedSeconds * refillRate;
        tokens = Math.min(capacity, tokens + tokensToAdd);
        lastRefillTime = now;
    }
}