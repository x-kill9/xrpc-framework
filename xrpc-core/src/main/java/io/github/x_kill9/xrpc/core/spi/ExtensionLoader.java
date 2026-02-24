/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.spi;

import io.github.x_kill9.xrpc.core.exception.NoSuchExtensionException;
import io.github.x_kill9.xrpc.core.exception.XRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI (Service Provider Interface) loader for XRPC extensions.
 *
 * <p>This class loads extension implementations from {@code META-INF/xrpc/} files.
 * Each extension interface has its own loader instance, which caches both class definitions
 * and singleton instances.
 *
 * @param <T> the extension interface type
 * @author x-kill9
 */
public class ExtensionLoader<T> {

    private static final Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);

    // Cache of loader instances per extension interface
    private static final Map<Class<?>, ExtensionLoader<?>> LOADERS = new ConcurrentHashMap<>();

    // Cache of extension implementation classes keyed by extension name
    private final Map<String, Class<?>> classCache = new ConcurrentHashMap<>();

    // Cache of singleton extension instances keyed by extension name
    private final Map<String, T> instanceCache = new ConcurrentHashMap<>();

    private static final String PREFIX = "META-INF/xrpc/";

    private final Class<T> type;
    private volatile boolean loaded = false;

    /**
     * Creates a new extension loader for the given type.
     *
     * @param type the extension interface class
     */
    public ExtensionLoader(Class<T> type) {
        this.type = type;
        logger.debug("ExtensionLoader created for type: {}", type.getName());
    }

    /**
     * Returns the extension loader for the specified interface.
     *
     * @param type the extension interface class
     * @param <T>  the interface type
     * @return the loader instance
     * @throws NoSuchExtensionException if the type is null or not an interface
     */
    @SuppressWarnings("unchecked")
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null) {
            logger.error("Extension type is null");
            throw new NoSuchExtensionException("type cannot be null");
        }
        if (!type.isInterface()) {
            logger.error("Extension type is not interface: {}", type.getName());
            throw new NoSuchExtensionException("Extension type must be interface");
        }

        ExtensionLoader<?> loader = LOADERS.get(type);
        if (loader == null) {
            logger.debug("Creating new ExtensionLoader for type: {}", type.getName());
            LOADERS.putIfAbsent(type, new ExtensionLoader<>(type));
            loader = LOADERS.get(type);
        }
        return (ExtensionLoader<T>) loader;
    }

    /**
     * Creates a new extension instance with the given name and varargs constructor parameters.
     *
     * @param name the extension name
     * @param args constructor arguments (optional)
     * @return a new instance
     * @throws XRpcException if instantiation fails
     */
    @SuppressWarnings("unchecked")
    public T newExtension(String name, Object... args) {
        checkInit();
        Class<?> clazz = classCache.get(name);
        if (clazz == null) {
            throw new IllegalArgumentException("No such extension: " + name);
        }
        try {
            if (args.length > 0) {
                return (T) clazz.getDeclaredConstructor().newInstance(args);
            }
            return (T) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new XRpcException("Failed to create extension instance for: " + name, e);
        }
    }

    /**
     * Creates a new extension instance with the given name and a map of parameters.
     *
     * @param name   the extension name
     * @param params configuration parameters as a map
     * @return a new instance
     * @throws XRpcException if the extension does not have a Map constructor or instantiation fails
     */
    @SuppressWarnings("unchecked")
    public T newExtension(String name, Map<String, Object> params) {
        checkInit();
        Class<?> clazz = classCache.get(name);
        if (clazz == null) {
            throw new IllegalArgumentException("No such extension: " + name);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor(Map.class);
            return (T) constructor.newInstance(params);
        } catch (NoSuchMethodException e) {
            throw new XRpcException("Extension " + name + " must have a constructor with Map<String,Object>", e);
        } catch (Exception e) {
            throw new XRpcException("Failed to create extension instance for: " + name, e);
        }
    }

    /**
     * Loads extension classes from configuration files.
     */
    private synchronized void loadExtensionClasses() {
        if (loaded) {
            logger.trace("Extension classes already loaded for type: {}", type.getName());
            return;
        }

        String fileName = PREFIX + type.getName();
        logger.debug("Loading extension classes from file: {}", fileName);

        try {
            Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(fileName);
            int urlCount = 0;
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                urlCount++;
                logger.debug("Found extension config URL [{}]: {}", urlCount, url);
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                    String line;
                    int lineNum = 0;
                    while ((line = reader.readLine()) != null) {
                        lineNum++;
                        logger.trace("Parsing line {} from {}: {}", lineNum, url, line);
                        parseLine(line);
                    }
                    logger.debug("Parsed {} lines from {}", lineNum, url);
                }
            }
            logger.info("Loaded {} extension config files for type: {}, found {} extensions",
                    urlCount, type.getName(), classCache.size());
        } catch (IOException e) {
            logger.error("Failed to load extension classes for type: {} from file: {}", type.getName(), fileName, e);
        } finally {
            loaded = true;
        }
    }

    /**
     * Parses a single line from an extension configuration file.
     *
     * @param line the line content
     */
    private void parseLine(String line) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) {
            logger.trace("Skipping empty or comment line: {}", line);
            return;
        }

        int eqIndex = line.indexOf('=');
        if (eqIndex <= 0) {
            logger.warn("Invalid extension config line (no '=' found): {}", line);
            return;
        }

        String name = line.substring(0, eqIndex).trim();
        String className = line.substring(eqIndex + 1).trim();
        logger.debug("Parsed extension mapping: {} -> {}", name, className);

        try {
            Class<?> clazz = Class.forName(className);
            classCache.put(name, clazz);
            logger.debug("Loaded extension class: {} for name: {}", className, name);
        } catch (ClassNotFoundException e) {
            logger.error("Extension class not found: {} for name: {}", className, name, e);
        }
    }

    /**
     * Returns a singleton extension instance for the given name.
     *
     * @param name the extension name
     * @return the singleton instance
     */
    public T getExtension(String name) {
        logger.debug("Getting extension: {} for type: {}", name, type.getName());
        checkInit();
        return instanceCache.computeIfAbsent(name, this::createExtension);
    }

    /**
     * Returns the set of all supported extension names.
     *
     * @return a set of extension names
     */
    public Set<String> getSupportedExtensions() {
        checkInit();
        return classCache.keySet();
    }

    /**
     * Ensures that extension classes have been loaded.
     */
    private void checkInit() {
        if (!loaded) {
            logger.trace("Extension not loaded yet, triggering load for type: {}", type.getName());
            loadExtensionClasses();
        }
    }

    /**
     * Creates a new singleton instance for the given extension name.
     *
     * @param name the extension name
     * @return the created instance
     * @throws IllegalStateException if creation fails
     */
    private T createExtension(String name) {
        Class<?> clazz = classCache.get(name);
        if (clazz == null) {
            logger.error("No such extension: {} for type: {}", name, type.getName());
            throw new IllegalArgumentException("No such extension: " + name);
        }
        try {
            logger.debug("Creating extension instance: {} for type: {}", name, type.getName());
            T instance = type.cast(clazz.getDeclaredConstructor().newInstance());
            logger.info("Created extension instance: {} -> {} for type: {}", name, clazz.getName(), type.getName());
            return instance;
        } catch (Exception e) {
            logger.error("Failed to create extension: {} for type: {}", name, type.getName(), e);
            throw new IllegalStateException("Failed to create extension: " + name, e);
        }
    }
}