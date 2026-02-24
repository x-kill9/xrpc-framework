/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.netty.client;

import io.github.x_kill9.xrpc.core.config.factory.ConfigFactory;
import io.github.x_kill9.xrpc.core.enums.CompressType;
import io.github.x_kill9.xrpc.core.enums.MessageType;
import io.github.x_kill9.xrpc.core.exception.NoAvailableInstanceException;
import io.github.x_kill9.xrpc.core.invocation.context.InvocationContext;
import io.github.x_kill9.xrpc.core.loadbalance.LoadBalancer;
import io.github.x_kill9.xrpc.core.loadbalance.ServiceInstance;
import io.github.x_kill9.xrpc.core.message.Message;
import io.github.x_kill9.xrpc.core.message.Request;
import io.github.x_kill9.xrpc.core.message.Response;
import io.github.x_kill9.xrpc.core.registry.RegistryService;
import io.github.x_kill9.xrpc.core.serialize.Serializer;
import io.github.x_kill9.xrpc.core.spi.ExtensionLoader;
import io.github.x_kill9.xrpc.core.transport.RpcClient;
import io.github.x_kill9.xrpc.core.util.id.GeneratorIdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * {@link RpcClient} implementation using Netty as the transport layer.
 *
 * <p>This client performs service discovery via a registry, applies load balancing,
 * serializes requests using the configured serializer, and sends them via a shared {@link NettyClient}.
 *
 * @author x-kill9
 */
public class NettyRpcClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyRpcClient.class);
    private static final NettyClient NETTY_CLIENT = new NettyClient();

    public NettyRpcClient() {
        logger.debug("NettyRpcClient created");
    }

    @Override
    public CompletableFuture<Response> sendRequest(Request request, InvocationContext context) throws IOException {
        String interfaceName = request.getInterfaceName();
        String methodName = request.getMethodName();
        logger.debug("Sending RPC request for {}.{}", interfaceName, methodName);

        // Get serializer from configuration
        String serializerType = ConfigFactory.getConfig().getClient().getSerializer();
        Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                .getExtension(serializerType);

        byte[] bodyBytes = serializer.serialize(request);
        logger.trace("Serialized request body, size: {} bytes", bodyBytes.length);

        long requestId = GeneratorIdUtils.nextId(Request.class);
        Message message = new Message(
                MessageType.REQUEST.getValue(),
                serializer.getTypeId(),
                CompressType.NONE.getValue(),         // compression can be configured later
                requestId,
                bodyBytes.length,
                bodyBytes
        );
        logger.debug("Created XMessage, requestId: {}, type: {}, serializeType: {}, compressType: {}",
                requestId, MessageType.REQUEST, serializer.getTypeId(), CompressType.NONE);

        // Discover available service instances from registry
        String registryType = ConfigFactory.getConfig().getRegistry().getType();
        logger.trace("Looking up registry service: {}", registryType);
        RegistryService registry = ExtensionLoader.getExtensionLoader(RegistryService.class)
                .getExtension(registryType);

        List<ServiceInstance> serviceInstances = registry.lookup(interfaceName);
        logger.debug("Found {} service instances for {}",
                serviceInstances != null ? serviceInstances.size() : 0, interfaceName);

        if (serviceInstances == null || serviceInstances.isEmpty()) {
            logger.error("No available instances found for service: {}", interfaceName);
            throw new NoAvailableInstanceException("No available instance for service: " + interfaceName);
        }

        // Select an instance using configured load balancer
        String balancer = ConfigFactory.getConfig().getClient().getLoadBalancer();
        logger.trace("Using load balancer: {}", balancer);
        LoadBalancer loadBalancer = ExtensionLoader.getExtensionLoader(LoadBalancer.class)
                .getExtension(balancer);
        ServiceInstance instance = loadBalancer.select(serviceInstances, context);

        if (instance == null) {
            logger.error("Load balancer returned null instance for service: {}", interfaceName);
            throw new NoAvailableInstanceException("No available instance for service: " + interfaceName);
        }

        logger.info("Selected instance {}:{} for service {}.{} via {} load balancer",
                instance.getHost(), instance.getPort(), interfaceName, methodName, balancer);
        logger.debug("Sending requestId: {} to {}:{}", requestId, instance.getHost(), instance.getPort());

        return NETTY_CLIENT.sendRequest(instance.getHost(), instance.getPort(), message);
    }

    /**
     * Shuts down the underlying Netty client, releasing all resources.
     */
    public static void shutdown() {
        logger.info("Shutting down NettyRpcClient");
        NETTY_CLIENT.closeAll();
    }
}