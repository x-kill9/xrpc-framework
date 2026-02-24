/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.zookeeper.registry;

import io.github.x_kill9.xrpc.core.config.factory.ConfigFactory;
import io.github.x_kill9.xrpc.core.loadbalance.ServiceInstance;
import io.github.x_kill9.xrpc.core.registry.RegistryService;
import io.github.x_kill9.xrpc.core.registry.listener.NotifyListener;
import io.github.x_kill9.xrpc.core.serialize.Serializer;
import io.github.x_kill9.xrpc.core.spi.ExtensionLoader;
import io.github.x_kill9.xrpc.zookeeper.adapter.ZkSerializerAdapter;
import io.github.x_kill9.xrpc.zookeeper.manager.CuratorClientManager;
import io.github.x_kill9.xrpc.zookeeper.manager.ZkCacheManager;
import io.github.x_kill9.xrpc.zookeeper.support.ZkPathBuilder;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * ZooKeeper implementation of {@link RegistryService}.
 *
 * @author x-kill9
 */
public class ZookeeperRegistryService implements RegistryService {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperRegistryService.class);

    private final CuratorFramework client;
    private final ZkSerializerAdapter serializerAdapter;
    private final ZkCacheManager cacheManager;

    public ZookeeperRegistryService() {
        logger.info("Initializing ZookeeperRegistryService");
        this.client = CuratorClientManager.getClient();

        String serializerType = ConfigFactory.getConfig().getClient().getSerializer();
        logger.debug("Using serializer: {}", serializerType);

        Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                .getExtension(serializerType);
        this.serializerAdapter = new ZkSerializerAdapter(serializer);
        this.cacheManager = new ZkCacheManager(client, serializerAdapter);

        logger.info("ZookeeperRegistryService initialized successfully");
    }

    @Override
    public void register(ServiceInstance instance) throws Exception {
        String path = ZkPathBuilder.buildInstancePath(instance);
        logger.info("Registering service instance: {} at path: {}", instance, path);

        client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(path, serializerAdapter.serialize(instance));

        logger.info("Service registered successfully: {} at {}", instance.getServiceName(), path);
    }

    @Override
    public void unregister(ServiceInstance instance) throws Exception {
        String path = ZkPathBuilder.buildInstancePath(instance);
        logger.info("Unregistering service instance: {} from path: {}", instance, path);

        client.delete().guaranteed().forPath(path);

        logger.info("Service unregistered successfully: {} from {}", instance.getServiceName(), path);
    }

    @Override
    public List<ServiceInstance> lookup(String serviceName) {
        logger.debug("Looking up instances for service: {}", serviceName);
        List<ServiceInstance> instances = cacheManager.getInstances(serviceName);
        logger.debug("Found {} instances for service: {}", instances.size(), serviceName);
        return instances;
    }

    @Override
    public void subscribe(String serviceName, NotifyListener listener) {
        logger.info("Subscribing to service changes: {}", serviceName);
        cacheManager.subscribe(serviceName, listener);
        logger.debug("Subscription completed for service: {}", serviceName);
    }

    @Override
    public void unsubscribe(String serviceName, NotifyListener listener) {
        logger.info("Unsubscribing from service changes: {}", serviceName);
        cacheManager.unsubscribe(serviceName, listener);
        logger.debug("Unsubscription completed for service: {}", serviceName);
    }

    @Override
    public void destroy() {
        logger.info("Destroying ZookeeperRegistryService");
        cacheManager.closeAll();
        logger.info("ZookeeperRegistryService destroyed, cache manager closed");
        // Curator client is managed by CuratorClientManager, do not close here
    }
}