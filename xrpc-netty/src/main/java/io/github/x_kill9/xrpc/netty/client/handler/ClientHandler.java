/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.netty.client.handler;

import io.github.x_kill9.xrpc.core.enums.MessageType;
import io.github.x_kill9.xrpc.core.message.Message;
import io.github.x_kill9.xrpc.core.message.Response;
import io.github.x_kill9.xrpc.core.serialize.Serializer;
import io.github.x_kill9.xrpc.serializer.factory.DefaultSerializerFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles incoming messages and manages pending requests for a Netty client.
 *
 * <p>This handler is {@link ChannelHandler.Sharable} and maintains a mapping from channels
 * to their pending requests (by request ID). It processes responses and heartbeats,
 * and cleans up resources when channels become inactive.
 *
 * @author x-kill9
 */
@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler<Message> {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    // Maps each channel to its pending requests: requestId -> CompletableFuture<Response>
    private final Map<Channel, Map<Long, CompletableFuture<Response>>> channelFutures = new ConcurrentHashMap<>();

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Unexpected exception from downstream on channel {}: {}", ctx.channel(), cause.getMessage(), cause);
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        MessageType messageType = MessageType.of(msg.getMessageType());
        long requestId = msg.getRequestId();
        Channel channel = ctx.channel();

        logger.trace("Received message type: {}, requestId: {}, channel: {}", messageType, requestId, channel);

        if (messageType == MessageType.RESPONSE) {
            Map<Long, CompletableFuture<Response>> futures = channelFutures.get(channel);
            if (futures != null) {
                CompletableFuture<Response> future = futures.remove(requestId);
                if (future != null) {
                    logger.debug("Found pending future for requestId: {}, deserializing response", requestId);
                    try {
                        byte serializeType = msg.getSerializeType();
                        Serializer serializer = DefaultSerializerFactory.getSerializer(serializeType);
                        Response response = serializer.deserialize(msg.getBodyBytes(), Response.class);
                        logger.debug("Response deserialized successfully for requestId: {}, status: {}",
                                requestId, response.getStatus());
                        future.complete(response);
                    } catch (IOException e) {
                        logger.error("Failed to deserialize response for requestId: {}", requestId, e);
                        future.completeExceptionally(e);
                    }
                } else {
                    logger.warn("No pending future found for requestId: {} on channel: {}", requestId, channel);
                }
            } else {
                logger.warn("No futures map found for channel: {}", channel);
            }
        } else if (messageType == MessageType.HEARTBEAT) {
            logger.trace("Received heartbeat response for requestId: {}", requestId);
        } else {
            logger.warn("Received unknown message type: {} for requestId: {}", messageType, requestId);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        logger.warn("Channel inactive: {}, cleaning up {} pending requests",
                channel, channelFutures.getOrDefault(channel, new ConcurrentHashMap<>()).size());

        Map<Long, CompletableFuture<Response>> futures = channelFutures.remove(channel);
        if (futures != null) {
            futures.forEach((requestId, future) -> {
                if (!future.isDone()) {
                    logger.debug("Completing requestId: {} exceptionally due to channel inactive", requestId);
                    future.completeExceptionally(
                            new IOException("Connection closed, requestId: " + requestId)
                    );
                }
            });
        }

        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                logger.trace("Writer idle detected, sending heartbeat to channel: {}", ctx.channel());
                Message heartbeat = new Message();
                heartbeat.setMessageType(MessageType.HEARTBEAT.getValue());
                heartbeat.setRequestId(0L);

                ctx.writeAndFlush(heartbeat);
                logger.trace("Heartbeat sent to channel: {}", ctx.channel());
            } else {
                logger.trace("Idle state event: {} on channel: {}", event.state(), ctx.channel());
            }
            return;
        }

        super.userEventTriggered(ctx, evt);
    }

    /**
     * Sends a request message over the given channel and returns a future for the response.
     *
     * @param channel the channel to send the request on
     * @param msg     the request message
     * @return a {@link CompletableFuture} that will complete with the response
     */
    public CompletableFuture<Response> sendRequest(Channel channel, Message msg) {
        long requestId = msg.getRequestId();
        logger.debug("Sending requestId: {} to channel: {}", requestId, channel);

        CompletableFuture<Response> future = new CompletableFuture<>();
        channelFutures.computeIfAbsent(channel, k -> {
            logger.trace("Creating new futures map for channel: {}", channel);
            return new ConcurrentHashMap<>();
        }).put(requestId, future);

        channel.writeAndFlush(msg);
        logger.trace("RequestId: {} written to channel pipeline", requestId);
        return future;
    }
}