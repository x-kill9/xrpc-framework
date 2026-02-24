/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.logging.config;

import io.github.x_kill9.xrpc.core.invocation.interceptor.config.InterceptorConfig;
import io.github.x_kill9.xrpc.logging.TraceInterceptor;

import java.util.Arrays;

/**
 * Configuration for {@link TraceInterceptor}.
 *
 * <p>Controls logging behavior including log level, whether to log method arguments,
 * and whether to log the return result.
 *
 * @author x-kill9
 */
public class TraceConfig implements InterceptorConfig {

    private String level = "INFO";      // Log level: INFO, DEBUG, WARN, ERROR
    private boolean logArgs = false;    // Whether to log method arguments
    private boolean logResult = true;   // Whether to log the return value

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public boolean isLogArgs() {
        return logArgs;
    }

    public void setLogArgs(boolean logArgs) {
        this.logArgs = logArgs;
    }

    public boolean isLogResult() {
        return logResult;
    }

    public void setLogResult(boolean logResult) {
        this.logResult = logResult;
    }

    @Override
    public void validate() {
        if (level == null || level.trim().isEmpty()) {
            throw new IllegalArgumentException("Log level must not be empty");
        }
        String upperLevel = level.trim().toUpperCase();
        if (!Arrays.asList("INFO", "DEBUG", "WARN", "ERROR").contains(upperLevel)) {
            throw new IllegalArgumentException("Invalid log level: " + level + ". Must be one of INFO, DEBUG, WARN, ERROR");
        }
    }
}