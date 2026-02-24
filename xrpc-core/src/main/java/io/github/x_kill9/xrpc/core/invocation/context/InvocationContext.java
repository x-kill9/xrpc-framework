/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.invocation.context;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Context object holding information about a single RPC invocation.
 *
 * <p>This context is passed through the interceptor chain and contains
 * the interface class, method, arguments, and optional custom attributes.
 *
 * @author x-kill9
 */
public class InvocationContext {

    private Class<?> interfaceClass;
    private Method method;
    private Object[] args;
    private Map<String, Object> attributes;

    public InvocationContext() {
    }

    public InvocationContext(Class<?> interfaceClass, Method method, Object[] args) {
        this.interfaceClass = interfaceClass;
        this.method = method;
        this.args = args;
    }

    public InvocationContext(Class<?> interfaceClass, Method method, Object[] args, Map<String, Object> attributes) {
        this.interfaceClass = interfaceClass;
        this.method = method;
        this.args = args;
        this.attributes = attributes;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}