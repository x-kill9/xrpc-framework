/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.config.loader;

import io.github.x_kill9.xrpc.core.config.model.XrpcConfig;

/**
 * Strategy interface for loading XRPC configuration from various sources.
 *
 * <p>Implementations are discovered via the SPI mechanism and are expected
 * to provide a concrete way to load configuration (e.g., from YAML, properties,
 * or a remote server).
 *
 * @author x-kill9
 */
public interface ConfigLoader {

    /**
     * Loads and returns the XRPC configuration.
     *
     * @return the loaded configuration object, never {@code null}
     */
    XrpcConfig load();
}