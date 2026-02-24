/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.logging;

import io.github.x_kill9.xrpc.core.invocation.context.InvocationContext;
import io.github.x_kill9.xrpc.core.invocation.interceptor.Interceptor;
import io.github.x_kill9.xrpc.core.invocation.interceptor.InterceptorChain;
import io.github.x_kill9.xrpc.logging.config.TraceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Interceptor that logs method invocation details, including execution time,
 * arguments (optional), result (optional), and errors.
 *
 * <p>The logging behavior is controlled by {@link TraceConfig}.
 *
 * @author x-kill9
 */
public class TraceInterceptor implements Interceptor<TraceConfig> {

    private static final Logger logger = LoggerFactory.getLogger(TraceInterceptor.class);

    private TraceConfig config;

    @Override
    public String getName() {
        return "trace";
    }

    @Override
    public Class<TraceConfig> getConfigClass() {
        return TraceConfig.class;
    }

    @Override
    public void setConfig(TraceConfig config) {
        this.config = config;
    }

    @Override
    public TraceConfig getConfig() {
        return config;
    }

    @Override
    public Object intercept(InvocationContext context, InterceptorChain chain) throws Throwable {
        long start = System.currentTimeMillis();
        String interfaceName = context.getInterfaceClass().getName();
        String methodName = context.getMethod().getName();

        StringBuilder logPrefix = new StringBuilder();
        if (config.isLogArgs()) {
            Object[] args = context.getArgs();
            logPrefix.append("args=").append(Arrays.toString(args)).append(" ");
        }

        try {
            Object result = chain.proceed(context);
            long cost = System.currentTimeMillis() - start;
            if (config.isLogResult()) {
                logWithLevel("{}Invoke {}.{} success, cost: {}ms, result: {}",
                        logPrefix.toString(), interfaceName, methodName, cost, result);
            } else {
                logWithLevel("{}Invoke {}.{} success, cost: {}ms",
                        logPrefix.toString(), interfaceName, methodName, cost);
            }
            return result;
        } catch (Throwable t) {
            long cost = System.currentTimeMillis() - start;
            logWithLevel("{}Invoke {}.{} failed, cost: {}ms, error: {}",
                    logPrefix.toString(), interfaceName, methodName, cost, t.getMessage());
            throw t;
        }
    }

    /**
     * Logs a message at the level specified in the configuration.
     *
     * @param format    the message format string
     * @param arguments the arguments for the format string
     */
    private void logWithLevel(String format, Object... arguments) {
        String level = config != null ? config.getLevel() : "INFO";
        switch (level.toUpperCase()) {
            case "DEBUG":
                logger.debug(format, arguments);
                break;
            case "WARN":
                logger.warn(format, arguments);
                break;
            case "ERROR":
                logger.error(format, arguments);
                break;
            default:
                logger.info(format, arguments);
        }
    }
}