/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.config.factory;

import io.github.x_kill9.xrpc.core.config.loader.ConfigLoader;
import io.github.x_kill9.xrpc.core.config.model.XrpcConfig;
import io.github.x_kill9.xrpc.core.spi.ExtensionLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for obtaining the global XRPC configuration.
 *
 * <p>This class uses double-checked locking to lazily load the configuration
 * via SPI (by default using the "yaml" extension). The loaded configuration
 * is cached and reused for subsequent calls.
 *
 * @author x-kill9
 */
public class ConfigFactory {
    private static final Logger logger = LoggerFactory.getLogger(ConfigFactory.class);
    private static volatile XrpcConfig config;

    /**
     * Returns the global XRPC configuration instance.
     *
     * <p>The configuration is loaded on first access via {@link #loadConfig()}
     * and cached thereafter.
     *
     * @return the XRPC configuration
     */
    public static XrpcConfig getConfig() {
        if (config == null) {
            synchronized (ConfigFactory.class) {
                if (config == null) {
                    logger.debug("Loading configuration for the first time");
                    config = loadConfig();
                    logger.info("Configuration initialized successfully");
                }
            }
        }
        return config;
    }

    /**
     * Loads the configuration using the SPI extension mechanism.
     *
     * <p>It looks for a {@link ConfigLoader} extension with the name "yaml".
     * If found, it invokes its {@link ConfigLoader#load()} method.
     * If no loader is found, it returns a default empty {@link XrpcConfig}.
     *
     * @return the loaded configuration, never {@code null}
     */
    private static XrpcConfig loadConfig() {
        logger.debug("Loading configuration via SPI extension loader");
        ConfigLoader configLoader = ExtensionLoader.getExtensionLoader(ConfigLoader.class).getExtension("yaml");
        if (configLoader != null) {
            logger.debug("Using ConfigLoader: {}", configLoader.getClass().getName());
            XrpcConfig loadedConfig = configLoader.load();
            logger.info("Configuration loaded via ConfigLoader");
            return loadedConfig;
        }
        logger.warn("No ConfigLoader found for 'yaml', using default empty configuration");
        return new XrpcConfig();
    }
}