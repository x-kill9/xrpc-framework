/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.loadbalance;

import java.util.Map;

/**
 * Represents a concrete service instance (e.g., a server node) that provides an RPC service.
 *
 * <p>Contains information such as service name, host, port, weight, and optional metadata.
 *
 * @author x-kill9
 */
public class ServiceInstance {

    private String serviceName;
    private String host;
    private int port;
    private int weight;
    private Map<String, String> metadata;

    public ServiceInstance() {
    }

    public ServiceInstance(String serviceName, String host, int port) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
    }

    public ServiceInstance(String serviceName, String host, int port, int weight) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
        this.weight = weight;
    }

    public ServiceInstance(String serviceName, String host, int port, int weight, Map<String, String> metadata) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
        this.weight = weight;
        this.metadata = metadata;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}