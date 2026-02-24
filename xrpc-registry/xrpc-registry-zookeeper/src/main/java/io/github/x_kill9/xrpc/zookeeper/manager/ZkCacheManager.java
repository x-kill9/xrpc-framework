/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.zookeeper.manager;

import io.github.x_kill9.xrpc.core.exception.XRpcException;
import io.github.x_kill9.xrpc.core.loadbalance.ServiceInstance;
import io.github.x_kill9.xrpc.core.registry.listener.NotifyListener;
import io.github.x_kill9.xrpc.zookeeper.adapter.ZkSerializerAdapter;
import io.github.x_kill9.xrpc.zookeeper.support.ZkPathBuilder;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages per-service {@link CuratorCache} instances for watching child nodes.
 *
 * <p>Maintains local caches of service instances and notifies registered listeners
 * when changes occur.
 *
 * @author x-kill9
 */
public class ZkCacheManager {

    private static final Logger logger = LoggerFactory.getLogger(ZkCacheManager.class);

    private final CuratorFramework client;
    private final ZkSerializerAdapter serializerAdapter;
    private final Map<String, CuratorCache> caches = new ConcurrentHashMap<>();
    private final Map<String, List<NotifyListener>> listeners = new ConcurrentHashMap<>();
    private final Map<String, List<ServiceInstance>> localCache = new ConcurrentHashMap<>();

    public ZkCacheManager(CuratorFramework client, ZkSerializerAdapter serializerAdapter) {
        this.client = client;
        this.serializerAdapter = serializerAdapter;
        logger.debug("ZkCacheManager created");
    }

    /**
     * Subscribes to changes of a specific service.
     *
     * @param serviceName the fully qualified service name
     * @param listener    the listener to be notified when instances change
     */
    public void subscribe(String serviceName, NotifyListener listener) {
        logger.debug("Subscribing to service: {}", serviceName);

        listeners.computeIfAbsent(serviceName, k -> new CopyOnWriteArrayList<>()).add(listener);
        logger.trace("Added listener for service: {}, total listeners: {}", serviceName, listeners.get(serviceName).size());

        // Ensure the Zookeeper path exists (CuratorCache requires the parent path)
        String path = ZkPathBuilder.buildServicePath(serviceName);
        try {
            client.create().creatingParentsIfNeeded().forPath(path);
            logger.debug("Created/ensured path for service: {}", path);
        } catch (KeeperException.NodeExistsException e) {
            logger.trace("Path already exists for service: {}", serviceName);
        } catch (Exception e) {
            logger.error("Failed to create path for service: {}", serviceName, e);
            throw new XRpcException("Failed to create path for service: " + serviceName, e);
        }

        // Initialize local cache with current instances
        List<ServiceInstance> initialInstances = doFetchInstances(serviceName);
        localCache.put(serviceName, initialInstances);
        logger.debug("Initial instances loaded for service: {}, count: {}", serviceName, initialInstances.size());

        // Notify the new listener immediately
        listener.notify(initialInstances);
        logger.trace("Notified initial instances to new listener for service: {}", serviceName);

        // Create and start a cache for this service if not already present
        caches.computeIfAbsent(serviceName, k -> {
            logger.debug("Creating CuratorCache for service: {}", serviceName);
            CuratorCache cache = CuratorCache.build(client, path);
            cache.listenable().addListener(
                    CuratorCacheListener.builder()
                            .forPathChildrenCache(
                                    path,
                                    client,
                                    (curatorFramework, event) -> {
                                        logger.trace("Received cache event: {} for service: {}", event.getType(), serviceName);
                                        handleCacheEvent(event);
                                    }
                            )
                            .build()
            );
            try {
                cache.start();
                logger.info("CuratorCache started for service: {}", serviceName);
            } catch (Exception e) {
                logger.error("Failed to start CuratorCache for service: {}", serviceName, e);
                throw new XRpcException("Failed to start CuratorCache for " + serviceName, e);
            }
            return cache;
        });
    }

    /**
     * Handles a cache event: updates local cache and notifies listeners.
     *
     * @param event the cache event
     */
    private void handleCacheEvent(PathChildrenCacheEvent event) {
        String serviceName = extractServiceName(event.getData().getPath());
        logger.debug("Handling cache event for service: {}, event type: {}", serviceName, event.getType());

        List<ServiceInstance> instances = doFetchInstances(serviceName);
        localCache.put(serviceName, instances);
        logger.debug("Updated local cache for service: {}, new instance count: {}", serviceName, instances.size());

        notifyListeners(serviceName, instances);
    }

    /**
     * Extracts the service name from a Zookeeper path.
     *
     * @param path the full path (e.g., /services/com.example.Service/127.0.0.1:8080)
     * @return the service name
     */
    private String extractServiceName(String path) {
        String[] parts = path.split("/");
        // parts[0] is empty, parts[1] = "services", parts[2] = serviceName
        return parts[2];
    }

    /**
     * Unsubscribes a listener from a service.
     *
     * @param serviceName the service name
     * @param listener    the listener to remove
     */
    public void unsubscribe(String serviceName, NotifyListener listener) {
        logger.debug("Unsubscribing listener from service: {}", serviceName);

        listeners.computeIfPresent(serviceName, (k, list) -> {
            list.remove(listener);
            logger.trace("Removed listener from service: {}, remaining: {}", serviceName, list.size());
            return list.isEmpty() ? null : list;
        });

        if (!listeners.containsKey(serviceName)) {
            logger.debug("No more listeners for service: {}, closing cache", serviceName);
            CuratorCache cache = caches.remove(serviceName);
            if (cache != null) {
                try {
                    cache.close();
                    logger.info("Closed CuratorCache for service: {}", serviceName);
                } catch (Exception e) {
                    logger.error("Error closing CuratorCache for service: {}", serviceName, e);
                }
            }
        }
    }

    /**
     * Returns the current list of instances for a service (from local cache).
     *
     * @param serviceName the service name
     * @return list of service instances (may be empty)
     */
    public List<ServiceInstance> getInstances(String serviceName) {
        List<ServiceInstance> instances = localCache.get(serviceName);
        if (instances == null) {
            logger.trace("No local cache for service: {}, fetching from Zookeeper", serviceName);
            instances = doFetchInstances(serviceName);
            localCache.put(serviceName, instances);
        } else {
            logger.trace("Returning cached instances for service: {}, count: {}", serviceName, instances.size());
        }
        return instances;
    }

    /**
     * Fetches the latest instances directly from Zookeeper.
     *
     * @param serviceName the service name
     * @return list of service instances (never null)
     */
    private List<ServiceInstance> doFetchInstances(String serviceName) {
        try {
            String path = ZkPathBuilder.buildServicePath(serviceName);
            List<String> children = client.getChildren().forPath(path);
            logger.trace("Fetched {} children from path: {}", children.size(), path);

            List<ServiceInstance> instances = new ArrayList<>(children.size());
            for (String child : children) {
                String fullPath = path + "/" + child;
                byte[] data = client.getData().forPath(fullPath);
                ServiceInstance instance = serializerAdapter.deserialize(data);
                if (instance.getServiceName() == null) {
                    instance.setServiceName(serviceName);
                }
                instances.add(instance);
                logger.trace("Deserialized instance: {} at path: {}", instance, fullPath);
            }
            return instances;
        } catch (KeeperException.NoNodeException e) {
            logger.debug("No node found for service: {}, returning empty list", serviceName);
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Failed to fetch instances for service: {}", serviceName, e);
            return Collections.emptyList();
        }
    }

    /**
     * Notifies all listeners registered for a service.
     *
     * @param serviceName the service name
     * @param instances   the updated instance list
     */
    private void notifyListeners(String serviceName, List<ServiceInstance> instances) {
        List<NotifyListener> list = listeners.get(serviceName);
        if (list != null) {
            logger.debug("Notifying {} listeners for service: {} with {} instances",
                    list.size(), serviceName, instances.size());
            for (NotifyListener listener : list) {
                try {
                    listener.notify(instances);
                    logger.trace("Notified listener for service: {}", serviceName);
                } catch (Exception e) {
                    logger.error("Error notifying listener for service: {}", serviceName, e);
                }
            }
        }
    }

    /**
     * Closes all caches and clears internal maps.
     */
    public void closeAll() {
        logger.info("Closing all {} CuratorCaches", caches.size());
        caches.values().forEach(cache -> {
            try {
                cache.close();
                logger.trace("Closed CuratorCache");
            } catch (Exception e) {
                logger.error("Error closing CuratorCache", e);
            }
        });
        caches.clear();
        listeners.clear();
        localCache.clear();
        logger.info("ZkCacheManager closed and cleared all caches");
    }
}