/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.zookeeper.support;

import io.github.x_kill9.xrpc.core.loadbalance.ServiceInstance;

/**
 * Utility for building ZooKeeper node paths.
 *
 * @author x-kill9
 */
public final class ZkPathBuilder {

    private static final String BASE_PATH = "/services";

    private ZkPathBuilder() {
    }

    /**
     * Builds the path for a service (parent node for all instances).
     *
     * @param serviceName the fully qualified service name
     * @return the Zookeeper path (e.g., /services/com.example.Service)
     */
    public static String buildServicePath(String serviceName) {
        return BASE_PATH + "/" + serviceName;
    }

    /**
     * Builds the path for a specific service instance.
     *
     * @param instance the service instance
     * @return the Zookeeper path (e.g., /services/com.example.Service/127.0.0.1:8080)
     */
    public static String buildInstancePath(ServiceInstance instance) {
        return buildServicePath(instance.getServiceName()) + "/"
                + instance.getHost() + ":" + instance.getPort();
    }

    /**
     * Extracts the host:port part from an instance path.
     *
     * @param path the full instance path
     * @return the host and port (e.g., "127.0.0.1:8080")
     */
    public static String extractHostPort(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }
}