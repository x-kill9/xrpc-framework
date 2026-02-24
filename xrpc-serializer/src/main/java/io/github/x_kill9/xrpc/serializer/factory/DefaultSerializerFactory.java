/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.serializer.factory;

import io.github.x_kill9.xrpc.core.serialize.Serializer;
import io.github.x_kill9.xrpc.core.spi.ExtensionLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default factory for obtaining {@link Serializer} instances by type ID.
 *
 * <p>All available serializers are loaded via SPI at class initialization and cached
 * in a map keyed by their type ID. The {@link #getSerializer(byte)} method provides
 * fast lookup.
 *
 * @author x-kill9
 */
public final class DefaultSerializerFactory {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSerializerFactory.class);
    private static final Map<Byte, Serializer> SERIALIZER_MAP = new ConcurrentHashMap<>();

    static {
        logger.debug("Initializing DefaultSerializerFactory, loading Serializer extensions");
        ExtensionLoader<Serializer> loader = ExtensionLoader.getExtensionLoader(Serializer.class);
        int count = 0;
        for (String name : loader.getSupportedExtensions()) {
            Serializer serializer = loader.getExtension(name);
            SERIALIZER_MAP.put(serializer.getTypeId(), serializer);
            logger.debug("Registered serializer: {} with typeId: {}", name, serializer.getTypeId());
            count++;
        }
        logger.info("DefaultSerializerFactory initialized with {} serializers", count);
    }

    private DefaultSerializerFactory() {
        // Prevent instantiation
    }

    /**
     * Returns a {@link Serializer} for the given type ID.
     *
     * @param serializeType the type ID of the serializer
     * @return the serializer instance
     * @throws IllegalArgumentException if no serializer is registered for the given type ID
     */
    public static Serializer getSerializer(byte serializeType) {
        logger.trace("Getting serializer for typeId: {}", serializeType);
        Serializer serializer = SERIALIZER_MAP.get(serializeType);
        if (serializer == null) {
            logger.error("No serializer found for typeId: {}", serializeType);
            throw new IllegalArgumentException("No serializer found for typeId: " + serializeType);
        }
        logger.trace("Returning serializer: {} for typeId: {}", serializer.getClass().getSimpleName(), serializeType);
        return serializer;
    }
}