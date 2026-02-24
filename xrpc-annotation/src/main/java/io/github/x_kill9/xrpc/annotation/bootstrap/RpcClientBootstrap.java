/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.annotation.bootstrap;

import io.github.x_kill9.xrpc.annotation.RpcComponent;
import io.github.x_kill9.xrpc.annotation.RpcReference;
import io.github.x_kill9.xrpc.core.config.factory.ConfigFactory;
import io.github.x_kill9.xrpc.core.container.RpcContainer;
import io.github.x_kill9.xrpc.core.exception.XRpcException;
import io.github.x_kill9.xrpc.core.proxy.jdk.JdkProxyFactory;
import io.github.x_kill9.xrpc.core.spi.ExtensionLoader;
import io.github.x_kill9.xrpc.core.transport.RpcClient;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Bootstrap class for the client side.
 * Scans for classes annotated with {@link RpcComponent}, registers them in the container,
 * and injects RPC proxies for fields annotated with {@link RpcReference}.
 *
 * @author x-kill9
 */
public class RpcClientBootstrap {

    /**
     * Starts the client, scans the package of the main class for {@code @RpcComponent} beans,
     * injects RPC proxies, and returns the main class instance.
     *
     * @param mainClass the main startup class annotated with {@code @RpcComponent}
     * @param <T>       the type of the main class
     * @return the main class instance with all dependencies injected
     * @throws Exception if any error occurs during startup
     */
    public static <T> T run(Class<T> mainClass) throws Exception {
        RpcContainer container = RpcContainer.getInstance();
        String basePackage = mainClass.getPackage().getName();

        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> componentClasses = reflections.getTypesAnnotatedWith(RpcComponent.class);

        List<Object> beans = new ArrayList<>();

        for (Class<?> clazz : componentClasses) {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            container.registerBean(clazz, instance);
            beans.add(instance);
        }

        for (Object bean : beans) {
            processRpcReferences(bean);
        }

        T mainBean = container.getBean(mainClass);
        if (mainBean == null) {
            throw new XRpcException("Main class instance not found: " + mainClass.getName());
        }
        return mainBean;
    }

    /**
     * Processes {@code @RpcReference} fields in the given bean and injects RPC proxies.
     *
     * @param bean the bean instance
     * @throws Exception if reflection or proxy creation fails
     */
    private static void processRpcReferences(Object bean) throws Exception {
        Class<?> clazz = bean.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(RpcReference.class)) {
                final String transportType = ConfigFactory.getConfig().getClient().getTransport();
                RpcClient client = ExtensionLoader.getExtensionLoader(RpcClient.class)
                        .getExtension(transportType);

                JdkProxyFactory proxyFactory = new JdkProxyFactory(client);
                Object proxy = proxyFactory.getProxy(field.getType());

                field.setAccessible(true);
                field.set(bean, proxy);
            }
        }
    }
}