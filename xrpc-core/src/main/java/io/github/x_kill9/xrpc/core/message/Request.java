/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.message;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * Represents an RPC request containing all necessary information to invoke a remote method.
 *
 * <p>This includes the interface name, method name, parameter types, actual arguments,
 * and optional attachments for passing additional metadata.
 *
 * @author x-kill9
 */
public class Request implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Fully qualified name of the service interface.
     */
    private String interfaceName;

    /**
     * Name of the method to invoke.
     */
    private String methodName;

    /**
     * Types of the method parameters (for overload resolution).
     */
    private Class<?>[] parameterTypes;

    /**
     * Actual arguments to pass to the method.
     */
    private Object[] parameters;

    /**
     * Additional metadata attached to the request (e.g., tracing info, authentication tokens).
     */
    private Map<String, String> attachments;

    public Request() {
    }

    public Request(String interfaceName, String methodName, Class<?>[] parameterTypes, Object[] parameters) {
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.parameters = parameters;
    }

    public Request(String interfaceName, String methodName, Class<?>[] parameterTypes, Object[] parameters,
                   Map<String, String> attachments) {
        this(interfaceName, methodName, parameterTypes, parameters);
        this.attachments = attachments;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, String> attachments) {
        this.attachments = attachments;
    }
}