/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.registry.listener;

import io.github.x_kill9.xrpc.core.loadbalance.ServiceInstance;

import java.util.List;

/**
 * Listener interface for receiving notifications about service instance changes.
 *
 * <p>Implementations can be registered with a {@link io.github.x_kill9.xrpc.core.registry.RegistryService}
 * to be notified when the list of available instances for a subscribed service changes.
 *
 * @author x-kill9
 */
public interface NotifyListener {

    /**
     * Called when the list of service instances changes.
     *
     * @param instances the updated list of service instances (may be empty)
     */
    void notify(List<ServiceInstance> instances);
}