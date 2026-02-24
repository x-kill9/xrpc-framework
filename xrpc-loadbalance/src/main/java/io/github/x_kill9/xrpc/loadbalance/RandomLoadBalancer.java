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
import java.util.concurrent.ThreadLocalRandom;

/**
 * Load balancer that selects a service instance randomly.
 *
 * <p>This implementation uses {@link ThreadLocalRandom} to ensure good performance
 * under high concurrency. Each selection is independent and uniformly distributed.
 *
 * @author x-kill9
 */
public class RandomLoadBalancer implements LoadBalancer {

    private static final Logger logger = LoggerFactory.getLogger(RandomLoadBalancer.class);

    @Override
    public String name() {
        return "random";
    }

    /**
     * Selects a random service instance from the given list.
     *
     * @param instances the list of available instances (may be empty or null)
     * @param context   the invocation context (used for logging)
     * @return a randomly selected instance, or {@code null} if the list is empty or null
     */
    @Override
    public ServiceInstance select(List<ServiceInstance> instances, InvocationContext context) {
        if (instances == null || instances.isEmpty()) {
            logger.warn("No service instances available for random selection, context: {}", context);
            return null;
        }

        int index = ThreadLocalRandom.current().nextInt(instances.size());
        ServiceInstance selected = instances.get(index);
        logger.debug("Randomly selected instance [{}/{}] for service {}: {}",
                index + 1, instances.size(), context.getInterfaceClass().getName(), selected);
        return selected;
    }
}