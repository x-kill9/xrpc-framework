/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.config.exception;

import io.github.x_kill9.xrpc.core.exception.XRpcException;

/**
 * Thrown when configuration loading or validation fails.
 *
 * @author x-kill9
 */
public class ConfigurationException extends XRpcException {
    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
