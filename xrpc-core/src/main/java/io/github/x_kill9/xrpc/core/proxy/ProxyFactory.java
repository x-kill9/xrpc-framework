/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.proxy;

/**
 * SPI interface for creating proxy objects for RPC service interfaces.
 *
 * <p>Implementations of this interface are responsible for generating proxy instances
 * that intercept method calls and route them to the remote service via the configured
 * {@link io.github.x_kill9.xrpc.core.transport.RpcClient}.
 *
 * @param <T> the type of the proxy object
 * @author x-kill9
 */
public interface ProxyFactory {

    /**
     * Returns a proxy instance for the given service interface.
     *
     * @param clazz the service interface class
     * @param <T>   the interface type
     * @return a proxy instance implementing the interface
     */
    <T> T getProxy(Class<T> clazz);
}