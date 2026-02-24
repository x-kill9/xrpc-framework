/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.config.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Configuration settings for the RPC client.
 *
 * @author x-kill9
 */
public class ClientConfig {
    private String transport = "netty";
    private String serializer = "kryo";
    private String loadBalancer = "random";
    private int connectTimeout = 3000;
    private int callTimeout = 3000;
    private int heartbeatIntervalSeconds = 30;
    private List<Map<String, Object>> interceptors = new ArrayList<>();

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public String getSerializer() {
        return serializer;
    }

    public void setSerializer(String serializer) {
        this.serializer = serializer;
    }

    public String getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(String loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getCallTimeout() {
        return callTimeout;
    }

    public void setCallTimeout(int callTimeout) {
        this.callTimeout = callTimeout;
    }

    public int getHeartbeatIntervalSeconds() {
        return heartbeatIntervalSeconds;
    }

    public void setHeartbeatIntervalSeconds(int heartbeatIntervalSeconds) {
        this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
    }

    public List<Map<String, Object>> getInterceptors() {
        return interceptors;
    }

    public void setInterceptors(List<Map<String, Object>> interceptors) {
        this.interceptors = interceptors;
    }
}