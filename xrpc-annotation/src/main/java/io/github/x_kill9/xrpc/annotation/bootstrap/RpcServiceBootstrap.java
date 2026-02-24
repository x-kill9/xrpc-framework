/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.annotation.bootstrap;

import io.github.x_kill9.xrpc.annotation.RpcService;
import io.github.x_kill9.xrpc.core.container.RpcContainer;
import io.github.x_kill9.xrpc.core.exception.XRpcException;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * Bootstrap class for the server side.
 * Scans specified packages for classes annotated with {@link RpcService} and registers them
 * in the container.
 *
 * @author x-kill9
 */
public class RpcServiceBootstrap {

    /**
     * Starts the server container, scans the given packages, and registers all classes
     * annotated with {@code @RpcService}.
     *
     * @param basePackages one or more package names to scan
     * @throws XRpcException if no package is specified or if registration fails
     */
    public static void start(String... basePackages) {
        if (basePackages == null || basePackages.length == 0) {
            throw new XRpcException("At least one package must be specified");
        }

        RpcContainer container = RpcContainer.getInstance();

        for (String packageName : basePackages) {
            Reflections reflections = new Reflections(
                    new ConfigurationBuilder()
                            .forPackage(packageName)
                            .filterInputsBy(new FilterBuilder().includePackage(packageName)));

            Set<Class<?>> classes = reflections.getTypesAnnotatedWith(RpcService.class);

            for (Class<?> clazz : classes) {
                if (clazz.isInterface()) {
                    throw new XRpcException("@RpcService cannot be annotated on an interface: " + clazz.getName());
                }

                if (clazz.getInterfaces().length == 0) {
                    throw new XRpcException("@RpcService class must implement at least one interface: " + clazz.getName());
                }

                try {
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    container.registerBean(clazz, instance);
                } catch (InstantiationException | IllegalAccessException |
                         InvocationTargetException | NoSuchMethodException e) {
                    throw new XRpcException("Failed to instantiate class: " + clazz.getName(), e);
                }
            }
        }
    }
}