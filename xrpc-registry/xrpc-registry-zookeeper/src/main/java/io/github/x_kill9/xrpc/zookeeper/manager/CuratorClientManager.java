/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.zookeeper.manager;

import io.github.x_kill9.xrpc.core.config.factory.ConfigFactory;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Curator Client Manager - provides a singleton {@link CuratorFramework} instance.
 *
 * @author x-kill9
 */
public final class CuratorClientManager {

    private static final Logger logger = LoggerFactory.getLogger(CuratorClientManager.class);
    private static volatile CuratorFramework client;

    private CuratorClientManager() {
    }

    /**
     * Returns the singleton CuratorFramework client.
     *
     * @return the Curator client instance
     * @throws RuntimeException      if the client fails to start
     * @throws IllegalStateException if the client is not in STARTED state
     */
    public static CuratorFramework getClient() {
        if (client == null) {
            logger.debug("Curator client not initialized, creating new instance");
            synchronized (CuratorClientManager.class) {
                if (client == null) {
                    String connectionString = ConfigFactory.getConfig().getRegistry().getAddress();
                    logger.info("Creating Curator client with connection string: {}", connectionString);

                    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
                    try {
                        client = CuratorFrameworkFactory.builder()
                                .connectString(connectionString)
                                .retryPolicy(retryPolicy)
                                .sessionTimeoutMs(15000)
                                .connectionTimeoutMs(5000)
                                .namespace("xrpc")
                                .build();
                        client.start();
                        logger.info("Curator client started successfully, state: {}", client.getState());
                    } catch (Exception e) {
                        logger.error("Failed to start Curator client with connection: {}", connectionString, e);
                        client = null;
                        throw new RuntimeException("Failed to start Curator client", e);
                    }
                } else {
                    logger.debug("Curator client already created by another thread");
                }
            }
        } else {
            logger.trace("Returning existing Curator client, state: {}", client.getState());
        }

        if (client.getState() != CuratorFrameworkState.STARTED) {
            logger.error("Curator client is not in STARTED state, current state: {}", client.getState());
            throw new IllegalStateException("Curator client is not started (state: " + client.getState() + ")");
        }
        return client;
    }

    /**
     * Closes the Curator client.
     */
    public static void close() {
        if (client != null) {
            logger.info("Closing Curator client, current state: {}", client.getState());
            client.close();
            logger.debug("Curator client closed");
            client = null;
        } else {
            logger.trace("Curator client already null, nothing to close");
        }
    }
}