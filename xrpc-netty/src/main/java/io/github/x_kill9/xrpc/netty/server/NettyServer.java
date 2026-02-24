/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.netty.server;

import io.github.x_kill9.xrpc.core.config.factory.ConfigFactory;
import io.github.x_kill9.xrpc.core.loadbalance.ServiceInstance;
import io.github.x_kill9.xrpc.core.registry.RegistryService;
import io.github.x_kill9.xrpc.core.spi.ExtensionLoader;
import io.github.x_kill9.xrpc.netty.codec.Decoder;
import io.github.x_kill9.xrpc.netty.codec.Encoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Netty-based RPC server.
 *
 * <p>Listens on a configured host and port, accepts incoming connections,
 * and delegates requests to the appropriate service instances from the provided service map.
 * It also registers itself with the configured registry and unregisters on shutdown.
 *
 * @author x-kill9
 */
public class NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private final String host;
    private final int port;
    private final Map<String, Object> serviceMap;

    public NettyServer(String host, int port, Map<String, Object> serviceMap) {
        this.host = host;
        this.port = port;
        this.serviceMap = serviceMap;
        logger.debug("NettyServer created - host: {}, port: {}, services: {}", host, port, serviceMap.keySet());
    }

    public void start() throws Exception {
        logger.info("Starting XRPC server on {}:{}", host, port);
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            logger.trace("Initializing server channel pipeline for {}", ch);
                            ch.pipeline()
                                    .addLast(new LengthFieldBasedFrameDecoder(
                                            10 * 1024 * 1024,
                                            14, 4, 0, 0))
                                    .addLast(new Decoder())
                                    .addLast(new Encoder())
                                    .addLast(new ServerHandler(serviceMap));
                            logger.trace("Server channel pipeline initialized for {}", ch);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.bind(port).sync();
            logger.info("XRPC server started and listening on port {}", port);

            // Register services with the registry
            String registryType = ConfigFactory.getConfig().getRegistry().getType();
            logger.debug("Using registry type: {}", registryType);
            RegistryService registry = ExtensionLoader.getExtensionLoader(RegistryService.class)
                    .getExtension(registryType);

            List<ServiceInstance> registeredInstances = new ArrayList<>();
            for (Map.Entry<String, Object> entry : serviceMap.entrySet()) {
                ServiceInstance instance = new ServiceInstance();
                instance.setServiceName(entry.getKey());
                instance.setHost(host);
                instance.setPort(port);
                instance.setWeight(1);
                logger.debug("Registering service: {} to registry at {}:{}", entry.getKey(), host, port);
                registry.register(instance);
                registeredInstances.add(instance);
                logger.info("Registered service: {} to registry", entry.getKey());
            }

            // Shutdown hook to unregister services
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown hook triggered, unregistering {} services", registeredInstances.size());
                for (ServiceInstance instance : registeredInstances) {
                    try {
                        logger.debug("Unregistering service: {}", instance.getServiceName());
                        registry.unregister(instance);
                        logger.info("Unregistered service: {}", instance.getServiceName());
                    } catch (Exception e) {
                        logger.error("Error unregistering service: {}", instance.getServiceName(), e);
                    }
                }
            }));

            logger.debug("Waiting for server channel close");
            future.channel().closeFuture().sync();
        } finally {
            logger.info("Shutting down server event loops");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            logger.info("Server shutdown complete");
        }
    }
}