/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.proxy.jdk;

import io.github.x_kill9.xrpc.core.config.factory.ConfigFactory;
import io.github.x_kill9.xrpc.core.config.model.ClientConfig;
import io.github.x_kill9.xrpc.core.invocation.context.InvocationContext;
import io.github.x_kill9.xrpc.core.invocation.context.InvocationContextBuilder;
import io.github.x_kill9.xrpc.core.invocation.interceptor.Interceptor;
import io.github.x_kill9.xrpc.core.invocation.interceptor.InterceptorChain;
import io.github.x_kill9.xrpc.core.invocation.interceptor.TargetInvoker;
import io.github.x_kill9.xrpc.core.message.Request;
import io.github.x_kill9.xrpc.core.message.Response;
import io.github.x_kill9.xrpc.core.protocol.request.RequestBuilder;
import io.github.x_kill9.xrpc.core.protocol.response.ResponseParser;
import io.github.x_kill9.xrpc.core.transport.RpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * {@link InvocationHandler} implementation for JDK dynamic proxies.
 *
 * <p>Handles method calls on the proxy by passing them through an interceptor chain
 * and finally sending the request via {@link RpcClient}. Methods inherited from
 * {@link Object} (equals, hashCode, toString) are handled locally.
 *
 * @author x-kill9
 */
public class RpcInvocationHandler implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(RpcInvocationHandler.class);

    private final Class<?> interfaceClass;
    private final RpcClient client;
    private final List<Interceptor<?>> interceptors;

    /**
     * Constructs a new invocation handler.
     *
     * @param interfaceClass the service interface being proxied
     * @param client         the RPC client for sending requests
     * @param interceptors   the list of interceptors to apply
     */
    public RpcInvocationHandler(Class<?> interfaceClass, RpcClient client, List<Interceptor<?>> interceptors) {
        this.interfaceClass = interfaceClass;
        this.client = client;
        this.interceptors = interceptors;
        logger.debug("RpcInvocationHandler created for interface: {}", interfaceClass.getName());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        logger.trace("Invoking method: {}.{} with {} args",
                interfaceClass.getSimpleName(), method.getName(), args != null ? args.length : 0);

        // Handle methods from Object class locally
        if (method.getDeclaringClass() == Object.class) {
            return handleObjectMethod(proxy, method, args);
        }

        ClientConfig clientConfig = ConfigFactory.getConfig().getClient();
        InvocationContext context = InvocationContextBuilder.build(interfaceClass, method, args);
        TargetInvoker targetInvoker = buildTargetInvoker(method, args, clientConfig.getCallTimeout());

        logger.debug("Starting interceptor chain execution for {}.{}", interfaceClass.getSimpleName(), method.getName());
        InterceptorChain chain = new InterceptorChain(interceptors, targetInvoker);
        Object result = chain.proceed(context);

        logger.trace("Method {}.{} executed successfully", interfaceClass.getSimpleName(), method.getName());
        return result;
    }

    /**
     * Handles methods inherited from {@link Object} (equals, hashCode, toString).
     *
     * @param proxy  the proxy instance
     * @param method the method being invoked
     * @param args   the method arguments
     * @return the appropriate result for the Object method
     * @throws UnsupportedOperationException if the method is not one of the supported ones
     */
    private Object handleObjectMethod(Object proxy, Method method, Object[] args) {
        String methodName = method.getName();
        logger.trace("Handling Object method: {}", methodName);

        return switch (methodName) {
            case "equals" -> proxy == args[0];
            case "hashCode" -> System.identityHashCode(proxy);
            case "toString" -> proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy));
            default -> throw new UnsupportedOperationException("Unsupported Object method: " + methodName);
        };
    }

    /**
     * Creates the {@link TargetInvoker} that will actually send the request.
     *
     * @param method      the method being invoked
     * @param args        the method arguments
     * @param callTimeout the call timeout in milliseconds
     * @return a target invoker that sends the request and returns the parsed response
     */
    private TargetInvoker buildTargetInvoker(Method method, Object[] args, int callTimeout) {
        return context -> {
            logger.trace("Building request for method: {}", method.getName());
            Request request = RequestBuilder.buildRequest(interfaceClass, method, args);

            logger.debug("Sending RPC request - service: {}, method: {}", interfaceClass.getName(), method.getName());
            long start = System.currentTimeMillis();

            CompletableFuture<Response> future = client.sendRequest(request, context);
            logger.trace("Request sent, waiting for response with timeout: {}ms", callTimeout);

            Response response = future.get(callTimeout, TimeUnit.MILLISECONDS);
            long cost = System.currentTimeMillis() - start;
            logger.debug("Received response, status: {}, cost: {}ms", response.getStatus(), cost);

            return ResponseParser.parse(response);
        };
    }
}