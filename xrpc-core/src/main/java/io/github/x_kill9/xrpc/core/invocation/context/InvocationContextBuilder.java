/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.invocation.context;

import java.lang.reflect.Method;

/**
 * Builder for creating {@link InvocationContext} instances.
 *
 * @author x-kill9
 */
public final class InvocationContextBuilder {

    private InvocationContextBuilder() {
    }

    /**
     * Builds an invocation context with the given parameters.
     *
     * @param interfaceClass the service interface class
     * @param method         the method being invoked
     * @param args           the method arguments
     * @return a new invocation context
     */
    public static InvocationContext build(Class<?> interfaceClass, Method method, Object[] args) {
        return new InvocationContext(interfaceClass, method, args);
    }
}