/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.netty.client;

import io.github.x_kill9.xrpc.core.config.factory.ConfigFactory;
import io.github.x_kill9.xrpc.core.config.model.XrpcConfig;
import io.github.x_kill9.xrpc.core.message.Message;
import io.github.x_kill9.xrpc.core.message.Response;
import io.github.x_kill9.xrpc.netty.client.handler.ClientHandler;
import io.github.x_kill9.xrpc.netty.client.manager.ConnectionManager;
import io.github.x_kill9.xrpc.netty.codec.Decoder;
import io.github.x_kill9.xrpc.netty.codec.Encoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Netty-based RPC client.
 *
 * <p>Manages a connection pool via {@link ConnectionManager} and uses a shared
 * {@link ClientHandler} to send requests and handle responses.
 *
 * @author x-kill9
 */
public class NettyClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private final EventLoopGroup group;
    private final ConnectionManager connectionManager;
    private final ClientHandler clientHandler;

    public NettyClient() {
        XrpcConfig config = ConfigFactory.getConfig();
        int heartbeatSeconds = config.getClient().getHeartbeatIntervalSeconds();
        int connectTimeout = config.getClient().getConnectTimeout();

        Bootstrap bootstrap = new Bootstrap();
        this.group = new NioEventLoopGroup();
        this.clientHandler = new ClientHandler();
        this.connectionManager = new ConnectionManager(bootstrap);

        logger.debug("Initializing NettyClient with connectTimeout: {}ms, heartbeat: {}s", connectTimeout, heartbeatSeconds);

        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        logger.trace("Initializing channel pipeline for {}", ch);
                        ch.pipeline()
                                .addLast(new LengthFieldBasedFrameDecoder(
                                        10 * 1024 * 1024,
                                        14, 4, 0, 0))
                                .addLast(new IdleStateHandler(0, heartbeatSeconds, 0, TimeUnit.SECONDS))
                                .addLast(new Decoder())
                                .addLast(new Encoder())
                                .addLast(clientHandler);
                        logger.trace("Channel pipeline initialized for {}", ch);
                    }
                });

        logger.info("NettyClient initialized successfully");
    }

    /**
     * Sends a request to the specified server and returns a future for the response.
     *
     * @param host the server host
     * @param port the server port
     * @param msg  the request message
     * @return a future that will complete with the response
     */
    public CompletableFuture<Response> sendRequest(String host, int port, Message msg) {
        long requestId = msg.getRequestId();
        logger.debug("Sending requestId: {} to {}:{}", requestId, host, port);

        return connectionManager.getChannel(host, port)
                .thenCompose(channel -> {
                    logger.trace("Channel acquired for requestId: {}, sending via ClientHandler", requestId);
                    return clientHandler.sendRequest(channel, msg);
                });
    }

    /**
     * Closes all connections and shuts down the client.
     */
    public void closeAll() {
        logger.info("Shutting down NettyClient, closing all connections");
        connectionManager.closeChannelAll();
        group.shutdownGracefully();
        logger.info("NettyClient shutdown complete");
    }
}