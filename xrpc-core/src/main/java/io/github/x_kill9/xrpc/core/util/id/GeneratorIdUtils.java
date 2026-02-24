/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.util.id;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility for generating unique, incrementing IDs per class.
 *
 * <p>Each class gets its own ID sequence, ensuring no interference. The initial ID
 * is set to {@value #START_ID} and increments by one for each call to {@link #nextId(Class)}.
 *
 * @author x-kill9
 */
public final class GeneratorIdUtils {

    private static final long START_ID = 100_000_000_000L;
    private static final Map<Class<?>, AtomicLong> ID_MAP = new ConcurrentHashMap<>();

    private GeneratorIdUtils() {
        // Prevent instantiation
    }

    /**
     * Returns the next unique ID for the given class.
     *
     * @param clazz the class for which an ID is requested
     * @return the next ID
     */
    public static long nextId(Class<?> clazz) {
        AtomicLong counter = ID_MAP.computeIfAbsent(clazz, k -> new AtomicLong(START_ID));
        return counter.incrementAndGet();
    }

    /**
     * Removes the ID counter for the specified class.
     *
     * @param clazz the class to remove
     */
    public static void remove(Class<?> clazz) {
        ID_MAP.remove(clazz);
    }

    /**
     * Removes all ID counters.
     */
    public static void removeAll() {
        ID_MAP.clear();
    }
}