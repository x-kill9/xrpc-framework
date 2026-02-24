/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.invocation.interceptor.factory;

import io.github.x_kill9.xrpc.core.exception.XRpcException;
import io.github.x_kill9.xrpc.core.invocation.interceptor.Interceptor;
import io.github.x_kill9.xrpc.core.invocation.interceptor.config.InterceptorConfig;
import io.github.x_kill9.xrpc.core.spi.ExtensionLoader;
import io.github.x_kill9.xrpc.core.util.MapToObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating interceptor instances from configuration.
 *
 * <p>Interceptors are created via SPI and configured with properties from the
 * configuration file. Configuration objects are injected if the interceptor
 * provides a configuration class.
 *
 * @author x-kill9
 */
public class InterceptorFactory {
    private static final Logger logger = LoggerFactory.getLogger(InterceptorFactory.class);
    @SuppressWarnings("rawtypes")
    private static final ExtensionLoader<Interceptor> LOADER = ExtensionLoader.getExtensionLoader(Interceptor.class);

    private InterceptorFactory() {
    }

    /**
     * Creates a list of interceptors from the given configuration maps.
     *
     * @param interceptorConfigs list of interceptor configurations (each map contains "name" and optionally "properties")
     * @return a list of instantiated and configured interceptors (may be empty)
     */
    public static List<Interceptor<?>> createInterceptors(List<Map<String, Object>> interceptorConfigs) {
        List<Interceptor<?>> interceptors = new ArrayList<>();
        if (interceptorConfigs == null || interceptorConfigs.isEmpty()) {
            logger.debug("No interceptor configurations found, returning empty list.");
            return interceptors;
        }

        for (Map<String, Object> configMap : interceptorConfigs) {
            Interceptor<?> interceptor = createInterceptor(configMap);
            if (interceptor != null) {
                interceptors.add(interceptor);
            }
        }

        logger.info("Successfully created {} interceptor(s).", interceptors.size());
        return interceptors;
    }

    /**
     * Creates a single interceptor from a configuration map.
     *
     * @param configMap the configuration map (must contain "name" and optionally "properties")
     * @return the configured interceptor, or {@code null} if configuration is empty
     * @throws XRpcException if the "name" field is missing or interceptor creation fails
     */
    public static Interceptor<?> createInterceptor(Map<String, Object> configMap) {
        if (configMap == null || configMap.isEmpty()) {
            logger.debug("Interceptor configuration is empty, skipping.");
            return null;
        }

        String name = (String) configMap.get("name");
        if (name == null) {
            throw new XRpcException("Interceptor config missing required field 'name'");
        }

        Map<String, Object> properties = getInterceptorProperties(configMap);
        logger.debug("Creating interceptor instance for name: {}", name);

        Interceptor<?> interceptor = LOADER.newExtension(name);
        Class<?> configClass = interceptor.getConfigClass();
        if (configClass != null) {
            injectConfig(interceptor, properties);
        } else {
            logger.debug("Interceptor '{}' does not provide a config class, skipping configuration injection.", name);
        }

        return interceptor;
    }

    /**
     * Extracts the "properties" map from the configuration.
     *
     * @param configMap the configuration map
     * @return the properties map (never {@code null}, empty if not present)
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> getInterceptorProperties(Map<String, Object> configMap) {
        Object props = configMap.getOrDefault("properties", Collections.emptyMap());
        return (props instanceof Map) ? (Map<String, Object>) props : Collections.emptyMap();
    }

    /**
     * Injects configuration properties into the interceptor.
     *
     * @param interceptor the interceptor instance
     * @param properties  the configuration properties
     * @param <C>         the configuration class type
     */
    private static <C extends InterceptorConfig> void injectConfig(
            Interceptor<C> interceptor, Map<String, Object> properties) {
        Class<C> configClass = interceptor.getConfigClass();
        if (configClass == null) {
            logger.warn("Interceptor '{}' has no config class, cannot inject configuration.", interceptor.getName());
            return;
        }

        C config = MapToObjectMapper.mapToObject(properties, configClass);
        config.validate();
        interceptor.setConfig(config);

        logger.info("Successfully injected configuration for interceptor '{}': {}", interceptor.getName(), config);
    }
}