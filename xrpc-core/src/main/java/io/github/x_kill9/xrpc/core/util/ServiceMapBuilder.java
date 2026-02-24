/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.util;

import io.github.x_kill9.xrpc.core.container.RpcContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility to build a service map (interface name -> instance) from the container.
 * 
 * <p>This class extracts all registered implementation classes and uses their first
 * implemented interface as the service key. If a class implements multiple interfaces,
 * only the first one is used; for more control, use manual mapping.
 *
 * @author x-kill9
 */
public final class ServiceMapBuilder {

    private ServiceMapBuilder() {
    }

    /**
     * Builds a service map from all beans in the container.
     *
     * @param container the RPC container
     * @return a map where keys are fully qualified interface names and values are the corresponding service instances
     */
    public static Map<String, Object> buildFromContainer(RpcContainer container) {
        Map<String, Object> serviceMap = new HashMap<>();
        for (Class<?> clazz : container.getAllBeanClasses()) {
            Class<?>[] interfaces = clazz.getInterfaces();
            if (interfaces.length > 0) {
                // Use the first interface as the service key (customize if needed)
                String interfaceName = interfaces[0].getName();
                serviceMap.put(interfaceName, container.getBean(clazz));
            }
        }
        return serviceMap;
    }
}