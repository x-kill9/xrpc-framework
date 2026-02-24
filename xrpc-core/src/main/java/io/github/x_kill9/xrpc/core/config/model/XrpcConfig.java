/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.config.model;

/**
 * Root configuration object holding all XRPC settings.
 *
 * @author x-kill9
 */
public class XrpcConfig {

    private ClientConfig client = new ClientConfig();
    private ServerConfig server = new ServerConfig();
    private RegistryConfig registry = new RegistryConfig();

    public ClientConfig getClient() {
        return client;
    }

    public void setClient(ClientConfig client) {
        this.client = client;
    }

    public ServerConfig getServer() {
        return server;
    }

    public void setServer(ServerConfig server) {
        this.server = server;
    }

    public RegistryConfig getRegistry() {
        return registry;
    }

    public void setRegistry(RegistryConfig registry) {
        this.registry = registry;
    }
}