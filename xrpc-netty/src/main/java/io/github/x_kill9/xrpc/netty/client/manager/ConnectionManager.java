/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.netty.client.manager;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages Netty channels for client connections.
 *
 * <p>Provides asynchronous channel acquisition with caching and automatic cleanup
 * when channels become inactive. Maintains a map of pending connection futures
 * to avoid duplicate connection attempts.
 *
 * @author x-kill9
 */
public class ConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    private final Bootstrap bootstrap;
    private final Map<String, Channel> channelMap = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<Channel>> channelFutures = new ConcurrentHashMap<>();

    public ConnectionManager(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
        logger.debug("ConnectionManager initialized");
    }

    /**
     * Returns a {@link CompletableFuture} that completes with a channel connected to the given address.
     *
     * <p>If a valid channel already exists, it is reused. Otherwise, a new connection is established.
     *
     * @param host the server host
     * @param port the server port
     * @return a future that will complete with the channel
     */
    public CompletableFuture<Channel> getChannel(String host, int port) {
        String key = buildKey(host, port);
        logger.debug("Getting channel for {}", key);

        return channelFutures.compute(key, (k, existingFuture) -> {
            if (existingFuture != null && isChannelValid(existingFuture)) {
                logger.trace("Reusing existing valid channel for {}", key);
                return existingFuture;
            }
            logger.debug("Creating new channel connection for {}", key);
            CompletableFuture<Channel> newFuture = new CompletableFuture<>();
            doConnect(host, port, newFuture);
            return newFuture;
        });
    }

    /**
     * Checks whether the channel associated with a future is still valid (connected and active).
     *
     * @param future the future holding the channel
     * @return {@code true} if the channel is valid, {@code false} otherwise
     */
    private boolean isChannelValid(CompletableFuture<Channel> future) {
        if (!future.isDone() || future.isCompletedExceptionally()) {
            logger.trace("Channel future not done or completed exceptionally");
            return false;
        }
        try {
            Channel channel = future.getNow(null);
            boolean valid = channel != null && channel.isActive();
            if (!valid) {
                logger.trace("Channel {} is not active", channel);
            }
            return valid;
        } catch (Exception e) {
            logger.trace("Exception checking channel validity: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Initiates a connection to the given address and completes the future with the result.
     *
     * @param host   the server host
     * @param port   the server port
     * @param future the future to complete when connected
     */
    private void doConnect(String host, int port, CompletableFuture<Channel> future) {
        String key = buildKey(host, port);
        logger.debug("Connecting to {}:{}", host, port);

        ChannelFuture connectFuture = bootstrap.connect(host, port);
        connectFuture.addListener((ChannelFutureListener) f -> {
            if (f.isSuccess()) {
                Channel channel = f.channel();
                logger.info("Connected successfully to {}, channel: {}", key, channel);
                channel.closeFuture().addListener(closeFuture -> {
                    logger.warn("Channel closed for {}, removing from cache", key);
                    channelFutures.remove(key, future);
                });
                future.complete(channel);
            } else {
                logger.error("Failed to connect to {}: {}", key, f.cause().getMessage());
                channelFutures.remove(key, future);
                future.completeExceptionally(f.cause());
            }
        });
    }

    /**
     * Closes the channel associated with the given address.
     *
     * @param host the server host
     * @param port the server port
     */
    public void closeChannel(String host, int port) {
        String key = buildKey(host, port);
        Channel channel = channelMap.get(key);
        if (channel == null) {
            logger.trace("No channel found to close for {}", key);
            return;
        }
        logger.debug("Closing channel for {}", key);
        channelMap.remove(key);
        channel.close();
    }

    /**
     * Closes all channels and clears caches.
     */
    public void closeChannelAll() {
        int count = channelMap.size();
        logger.info("Closing all {} channels", count);
        channelMap.values().forEach(Channel::close);
        channelMap.clear();
        channelFutures.clear();
        logger.debug("All channels closed and caches cleared");
    }

    /**
     * Builds a cache key from host and port.
     *
     * @param host the host
     * @param port the port
     * @return the key string
     */
    private String buildKey(String host, int port) {
        return host + ":" + port;
    }
}