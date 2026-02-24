/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.loadbalance;

import io.github.x_kill9.xrpc.core.invocation.context.InvocationContext;

import java.util.List;

/**
 * Strategy interface for load balancing among service instances.
 *
 * <p>Implementations of this interface select a target service instance from a list
 * of available instances based on a specific algorithm (e.g., round-robin, random,
 * consistent hashing).
 *
 * @author x-kill9
 */
public interface LoadBalancer {

    /**
     * Returns the name of the load balancer implementation.
     *
     * @return the load balancer name
     */
    String name();

    /**
     * Selects a service instance from the given list.
     *
     * @param instances the list of available service instances (non-null)
     * @param context   the invocation context, which may contain information such as
     *                  interface name, method, parameters for advanced strategies like consistent hashing
     * @return the selected service instance, never {@code null}
     */
    ServiceInstance select(List<ServiceInstance> instances, InvocationContext context);
}