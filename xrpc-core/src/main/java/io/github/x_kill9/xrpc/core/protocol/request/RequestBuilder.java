/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.protocol.request;

import io.github.x_kill9.xrpc.core.message.Request;

import java.lang.reflect.Method;

/**
 * Utility class for building {@link Request} objects from invocation information.
 *
 * <p>This class provides static methods to create a request containing the interface name,
 * method name, parameter types, and arguments for a remote call.
 *
 * @author x-kill9
 */
public final class RequestBuilder {

    private RequestBuilder() {
    }

    /**
     * Builds an RPC request from the given invocation details.
     *
     * @param interfaceClass the service interface class
     * @param method         the method being invoked
     * @param args           the arguments to pass to the method
     * @return a new {@link Request} instance containing the necessary information
     */
    public static Request buildRequest(Class<?> interfaceClass, Method method, Object[] args) {
        return new Request(
                interfaceClass.getName(),
                method.getName(),
                method.getParameterTypes(),
                args
        );
    }
}