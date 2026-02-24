/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.invocation.interceptor.config;

/**
 * Marker interface for interceptor configuration objects.
 *
 * <p>Implementations can override {@link #validate()} to perform
 * custom validation of configuration parameters.
 *
 * @author x-kill9
 */
public interface InterceptorConfig {

    /**
     * Validates the configuration.
     *
     * @throws IllegalArgumentException if the configuration is invalid
     */
    default void validate() {
        // No-op by default
    }
}