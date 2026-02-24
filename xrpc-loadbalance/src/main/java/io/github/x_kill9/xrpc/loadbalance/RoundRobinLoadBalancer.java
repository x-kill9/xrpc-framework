/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.loadbalance;

import io.github.x_kill9.xrpc.core.invocation.context.InvocationContext;
import io.github.x_kill9.xrpc.core.loadbalance.LoadBalancer;
import io.github.x_kill9.xrpc.core.loadbalance.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Load balancer that selects service instances in a round-robin fashion.
 *
 * <p>A separate counter is maintained per service (identified by its interface class)
 * to ensure sequential selection across all instances of that service.
 *
 * @author x-kill9
 */
public class RoundRobinLoadBalancer implements LoadBalancer {

    private static final Logger logger = LoggerFactory.getLogger(RoundRobinLoadBalancer.class);

    // Maintains the current round-robin index for each service interface
    private final Map<Class<?>, AtomicInteger> indexMap = new ConcurrentHashMap<>();

    @Override
    public String name() {
        return "round";
    }

    /**
     * Selects the next service instance in round-robin order.
     *
     * @param instances the list of available instances (must not be empty)
     * @param context   the invocation context, used to identify the service
     * @return the selected instance, or {@code null} if the list is empty
     */
    @Override
    public ServiceInstance select(List<ServiceInstance> instances, InvocationContext context) {
        if (instances == null || instances.isEmpty()) {
            logger.warn("No service instances available for round-robin selection, service: {}",
                    context.getInterfaceClass().getName());
            return null;
        }

        Class<?> serviceClass = context.getInterfaceClass();
        AtomicInteger counter = indexMap.computeIfAbsent(serviceClass, k -> {
            logger.debug("Creating new round-robin counter for service: {}", serviceClass.getName());
            return new AtomicInteger(0);
        });

        int currentIndex = counter.getAndIncrement();
        // Compute the index within the list size, handling negative overflow (unlikely but safe)
        int index = Math.floorMod(currentIndex, instances.size());
        ServiceInstance selected = instances.get(index);

        logger.debug("Round-robin selected instance [{}/{}] (counter: {}) for service {}: {}",
                index + 1, instances.size(), currentIndex, serviceClass.getName(), selected);
        return selected;
    }
}