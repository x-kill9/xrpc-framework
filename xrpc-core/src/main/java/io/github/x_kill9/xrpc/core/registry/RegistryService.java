/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.registry;

import io.github.x_kill9.xrpc.core.loadbalance.ServiceInstance;
import io.github.x_kill9.xrpc.core.registry.listener.NotifyListener;

import java.util.List;

/**
 * Service registry interface for service registration, discovery, and subscription.
 *
 * <p>Implementations connect to a registry (e.g., ZooKeeper, Nacos) to manage service instances.
 * Clients use this interface to register their own instances and to discover instances of other services.
 *
 * @author x-kill9
 */
public interface RegistryService {

    /**
     * Registers a service instance with the registry.
     *
     * @param serviceInstance the service instance to register
     * @throws Exception if registration fails (e.g., network error, invalid data)
     */
    void register(ServiceInstance serviceInstance) throws Exception;

    /**
     * Unregisters a previously registered service instance.
     *
     * @param serviceInstance the service instance to unregister
     * @throws Exception if unregistration fails
     */
    void unregister(ServiceInstance serviceInstance) throws Exception;

    /**
     * Subscribes to changes for a specific service.
     *
     * <p>When the list of instances for the given service changes, the provided listener
     * will be notified with the updated list.
     *
     * @param serviceName the fully qualified name of the service interface
     * @param listener    the listener to receive change notifications
     */
    void subscribe(String serviceName, NotifyListener listener);

    /**
     * Unsubscribes a listener from service change notifications.
     *
     * @param serviceName the fully qualified name of the service interface
     * @param listener    the listener to remove
     */
    void unsubscribe(String serviceName, NotifyListener listener);

    /**
     * Queries all available instances for a given service.
     *
     * @param serviceName the fully qualified name of the service interface
     * @return a list of currently available service instances (may be empty)
     */
    List<ServiceInstance> lookup(String serviceName);

    /**
     * Destroys the registry client and releases any associated resources.
     *
     * <p>This method should be called when the application shuts down to clean up connections.
     */
    void destroy();
}