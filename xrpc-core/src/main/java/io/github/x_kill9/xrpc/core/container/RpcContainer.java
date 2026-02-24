/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.container;

import io.github.x_kill9.xrpc.core.exception.XRpcException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight dependency injection container (core module).
 *
 * <p>Features:
 * <ul>
 *   <li>Singleton bean management</li>
 *   <li>Bean retrieval by name or type</li>
 *   <li>Support for querying multiple implementations of an interface</li>
 *   <li>Manual bean registration</li>
 * </ul>
 *
 * <p>Note: This container does not perform scanning or dependency injection;
 * those are handled by higher-level modules (e.g., annotation module).
 *
 * @author x-kill9
 */
public class RpcContainer {

    private static volatile RpcContainer instance;

    // All singleton beans, keyed by implementation class or interface
    private final Map<Class<?>, Object> singletonBeans = new ConcurrentHashMap<>();

    // Interface -> list of implementation classes (for retrieving all implementations of an interface)
    private final Map<Class<?>, List<Class<?>>> interfaceToImpls = new ConcurrentHashMap<>();

    // Bean name -> implementation class
    private final Map<String, Class<?>> nameToClass = new ConcurrentHashMap<>();

    private RpcContainer() {
    }

    /**
     * Returns the singleton container instance (double-checked locking).
     *
     * @return the container instance
     */
    public static RpcContainer getInstance() {
        if (instance == null) {
            synchronized (RpcContainer.class) {
                if (instance == null) {
                    instance = new RpcContainer();
                }
            }
        }
        return instance;
    }

    /**
     * Registers a bean instance with a default name (simple class name with first letter lowercased).
     *
     * @param clazz    the actual type of the bean (must be an implementation class, not an interface)
     * @param instance the bean instance
     * @throws IllegalArgumentException if the instance is not of the given class
     */
    public void registerBean(Class<?> clazz, Object instance) {
        registerBean(clazz, instance, defaultBeanName(clazz));
    }

    /**
     * Registers a bean instance with a specified name.
     *
     * @param clazz    the actual type of the bean (must be an implementation class, not an interface)
     * @param instance the bean instance
     * @param name     the bean name (must be unique and non-empty)
     * @throws IllegalArgumentException if the instance is not of the given class, or the name is already in use
     */
    public void registerBean(Class<?> clazz, Object instance, String name) {
        if (!clazz.isInstance(instance)) {
            throw new IllegalArgumentException("Instance is not of type " + clazz);
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Bean name must not be empty");
        }
        if (nameToClass.containsKey(name)) {
            throw new IllegalArgumentException("Bean name '" + name + "' already exists");
        }

        nameToClass.put(name, clazz);
        singletonBeans.put(clazz, instance);

        for (Class<?> iface : clazz.getInterfaces()) {
            singletonBeans.putIfAbsent(iface, instance);
            interfaceToImpls.computeIfAbsent(iface, k -> new ArrayList<>()).add(clazz);
        }
    }

    /**
     * Generates a default bean name: simple class name with first letter lowercased.
     *
     * @param clazz the bean class
     * @return the default bean name
     */
    private String defaultBeanName(Class<?> clazz) {
        String simpleName = clazz.getSimpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    /**
     * Retrieves a bean by its name.
     *
     * @param name the bean name
     * @param <T>  the expected type
     * @return the bean instance
     * @throws XRpcException if no bean with the given name exists
     */
    @SuppressWarnings("unchecked")
    public <T> T getBeanByName(String name) {
        Class<?> clazz = nameToClass.get(name);
        if (clazz == null) {
            throw new XRpcException("Bean not found with name '" + name + "'");
        }
        return (T) getBean(clazz);
    }

    /**
     * Retrieves a bean by its type.
     *
     * <p>If the type is an interface and there are multiple implementations, an exception is thrown.
     * Use {@link #getBeansOfType(Class)} or {@link #getBeanByName(String)} for such cases.
     *
     * @param clazz the bean type (can be an interface or implementation class)
     * @param <T>   the type
     * @return the bean instance
     * @throws XRpcException if no bean is found or multiple implementations exist for an interface
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> clazz) {
        if (singletonBeans.containsKey(clazz)) {
            return (T) singletonBeans.get(clazz);
        }

        if (clazz.isInterface()) {
            List<Class<?>> impls = interfaceToImpls.get(clazz);
            if (impls == null || impls.isEmpty()) {
                throw new XRpcException("No implementation found for interface " + clazz.getName());
            }
            if (impls.size() > 1) {
                throw new XRpcException("Multiple implementations found for interface " + clazz.getName() +
                        ", use getBeansOfType() or getBeanByName() with specific name");
            }
            return (T) getBean(impls.get(0));
        }

        throw new XRpcException("Bean not found for type " + clazz.getName());
    }

    /**
     * Returns all beans that implement the given interface.
     *
     * @param interfaceType the interface class
     * @param <T>           the interface type
     * @return a list of bean instances (empty if none)
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getBeansOfType(Class<T> interfaceType) {
        if (!interfaceType.isInterface()) {
            throw new XRpcException("Type must be an interface");
        }
        List<Class<?>> impls = interfaceToImpls.get(interfaceType);
        if (impls == null) return Collections.emptyList();

        return impls.stream()
                .map(c -> (T) getBean(c))
                .toList();
    }

    /**
     * Returns all registered implementation classes (interfaces are not included).
     *
     * @return a set of implementation classes
     */
    public Set<Class<?>> getAllBeanClasses() {
        return new HashSet<>(nameToClass.values());
    }

    /**
     * Returns all registered bean names.
     *
     * @return a set of bean names
     */
    public Set<String> getBeanNames() {
        return new HashSet<>(nameToClass.keySet());
    }

    /**
     * Checks whether a bean of the given type (including interfaces) exists.
     *
     * @param clazz the type to check
     * @return true if a bean of this type exists, false otherwise
     */
    public boolean containsBean(Class<?> clazz) {
        return singletonBeans.containsKey(clazz);
    }

    /**
     * Checks whether a bean with the given name exists.
     *
     * @param name the bean name
     * @return true if a bean with this name exists, false otherwise
     */
    public boolean containsBeanName(String name) {
        return nameToClass.containsKey(name);
    }

    /**
     * Clears the container and destroys all beans.
     */
    public synchronized void clear() {
        singletonBeans.clear();
        interfaceToImpls.clear();
        nameToClass.clear();
    }
}