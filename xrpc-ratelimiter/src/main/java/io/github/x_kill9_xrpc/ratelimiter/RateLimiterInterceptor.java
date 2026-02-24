/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9_xrpc.ratelimiter;

import io.github.x_kill9.xrpc.core.flowcontrol.RateLimiter;
import io.github.x_kill9.xrpc.core.invocation.context.InvocationContext;
import io.github.x_kill9.xrpc.core.invocation.interceptor.Interceptor;
import io.github.x_kill9.xrpc.core.invocation.interceptor.InterceptorChain;
import io.github.x_kill9.xrpc.core.spi.ExtensionLoader;
import io.github.x_kill9_xrpc.ratelimiter.exception.RateLimitException;

import java.util.Map;

/**
 * Interceptor that applies rate limiting to RPC invocations.
 *
 * <p>Uses a {@link RateLimiter} implementation obtained via SPI based on the configuration.
 *
 * @author x-kill9
 */
public class RateLimiterInterceptor implements Interceptor<RateLimiterConfig> {

    private RateLimiterConfig config;
    private RateLimiter limiter;

    @Override
    public String getName() {
        return "rateLimiter";
    }

    @Override
    public Class<RateLimiterConfig> getConfigClass() {
        return RateLimiterConfig.class;
    }

    @Override
    public void setConfig(RateLimiterConfig config) {
        this.config = config;
        this.limiter = createRateLimiter(config);
    }

    @Override
    public RateLimiterConfig getConfig() {
        return config;
    }

    private RateLimiter createRateLimiter(RateLimiterConfig config) {
        String type = config.getType();
        Map<String, Object> params = config.getParams();
        return ExtensionLoader.getExtensionLoader(RateLimiter.class).newExtension(type, params);
    }

    @Override
    public Object intercept(InvocationContext context, InterceptorChain chain) throws Throwable {
        if (!limiter.tryAcquire()) {
            throw new RateLimitException("Rate limited");
        }
        return chain.proceed(context);
    }
}