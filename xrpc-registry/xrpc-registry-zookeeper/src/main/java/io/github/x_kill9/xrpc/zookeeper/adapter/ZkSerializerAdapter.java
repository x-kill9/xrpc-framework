/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.zookeeper.adapter;

import io.github.x_kill9.xrpc.core.exception.XRpcException;
import io.github.x_kill9.xrpc.core.loadbalance.ServiceInstance;
import io.github.x_kill9.xrpc.core.serialize.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Adapter for serializing/deserializing {@link ServiceInstance} with a {@link Serializer}.
 *
 * @author x-kill9
 */
public class ZkSerializerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ZkSerializerAdapter.class);
    private final Serializer serializer;

    public ZkSerializerAdapter(Serializer serializer) {
        this.serializer = serializer;
        logger.debug("ZkSerializerAdapter created with serializer: {}", serializer.getClass().getName());
    }

    /**
     * Serializes a service instance into a byte array.
     *
     * @param instance the service instance to serialize
     * @return the serialized bytes
     * @throws XRpcException if serialization fails
     */
    public byte[] serialize(ServiceInstance instance) {
        logger.trace("Serializing ServiceInstance: {}", instance);
        try {
            byte[] data = serializer.serialize(instance);
            logger.trace("Serialized ServiceInstance, size: {} bytes", data.length);
            return data;
        } catch (IOException e) {
            logger.error("Failed to serialize ServiceInstance: {}", instance, e);
            throw new XRpcException("Failed to serialize ServiceInstance", e);
        }
    }

    /**
     * Deserializes a byte array into a service instance.
     *
     * @param data the serialized bytes
     * @return the deserialized service instance
     * @throws XRpcException if deserialization fails
     */
    public ServiceInstance deserialize(byte[] data) {
        logger.trace("Deserializing ServiceInstance from {} bytes", data.length);
        try {
            ServiceInstance instance = serializer.deserialize(data, ServiceInstance.class);
            logger.debug("Deserialized ServiceInstance: {}", instance);
            return instance;
        } catch (IOException e) {
            logger.error("Failed to deserialize ServiceInstance data, length: {}", data.length, e);
            throw new XRpcException("Failed to deserialize ServiceInstance", e);
        }
    }
}