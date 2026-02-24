/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.proxy.jdk;

import io.github.x_kill9.xrpc.core.config.factory.ConfigFactory;
import io.github.x_kill9.xrpc.core.config.model.ClientConfig;
import io.github.x_kill9.xrpc.core.invocation.interceptor.Interceptor;
import io.github.x_kill9.xrpc.core.invocation.interceptor.factory.InterceptorFactory;
import io.github.x_kill9.xrpc.core.proxy.ProxyFactory;
import io.github.x_kill9.xrpc.core.transport.RpcClient;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JDK dynamic proxy implementation of {@link ProxyFactory}.
 *
 * <p>This factory creates JDK proxies for service interfaces. Each proxy is cached
 * to avoid repeated creation. The proxy instances are backed by {@link RpcInvocationHandler}
 * which applies the configured interceptor chain before sending the request.
 *
 * @author x-kill9
 */
public class JdkProxyFactory implements ProxyFactory {

    private final Map<Class<?>, Object> proxyCache = new ConcurrentHashMap<>();
    private final RpcClient client;

    /**
     * Constructs a new JDK proxy factory with the given RPC client.
     *
     * @param client the RPC client used to send requests
     */
    public JdkProxyFactory(RpcClient client) {
        this.client = client;
    }

    /**
     * Returns a proxy instance for the specified interface.
     *
     * <p>The proxy is created once and cached for subsequent requests.
     *
     * @param clazz the service interface class
     * @param <T>   the interface type
     * @return a proxy instance
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) proxyCache.computeIfAbsent(clazz, this::createProxy);
    }

    /**
     * Creates a new proxy instance for the given interface.
     *
     * @param interfaceClass the interface class
     * @param <T>            the interface type
     * @return a proxy instance
     */
    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<?> interfaceClass) {
        List<Interceptor<?>> interceptors = buildInterceptors();
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class[]{interfaceClass},
                new RpcInvocationHandler(interfaceClass, client, interceptors)
        );
    }

    /**
     * Builds the interceptor chain from the client configuration.
     *
     * <p>The interceptors are created via {@link InterceptorFactory} based on
     * the configuration provided in {@code xrpc.yaml}.
     *
     * @return a list of configured interceptors (may be empty)
     */
    private List<Interceptor<?>> buildInterceptors() {
        ClientConfig clientConfig = ConfigFactory.getConfig().getClient();
        List<Map<String, Object>> interceptorConfigs = clientConfig.getInterceptors();
        return InterceptorFactory.createInterceptors(interceptorConfigs);
    }
}