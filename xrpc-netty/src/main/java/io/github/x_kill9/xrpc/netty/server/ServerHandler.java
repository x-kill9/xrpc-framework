/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.netty.server;

import io.github.x_kill9.xrpc.core.constants.StatusCodes;
import io.github.x_kill9.xrpc.core.container.RpcContainer;
import io.github.x_kill9.xrpc.core.enums.MessageType;
import io.github.x_kill9.xrpc.core.message.Message;
import io.github.x_kill9.xrpc.core.message.Request;
import io.github.x_kill9.xrpc.core.message.Response;
import io.github.x_kill9.xrpc.core.serialize.Serializer;
import io.github.x_kill9.xrpc.core.util.ServiceMapBuilder;
import io.github.x_kill9.xrpc.serializer.factory.DefaultSerializerFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles incoming RPC requests, invokes the corresponding service method,
 * and sends back a response.
 *
 * <p>This handler is {@link ChannelHandler.Sharable} and processes messages
 * of type {@link MessageType#REQUEST} and {@link MessageType#HEARTBEAT}.
 * It properly handles exceptions and returns appropriate error responses.
 *
 * @author x-kill9
 */
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<Message> {

    private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    // Mapping from interface name to service implementation instance
    private final Map<String, Object> serviceMap;

    public ServerHandler(Map<String, Object> serviceMap) {
        this.serviceMap = serviceMap;
        logger.debug("ServerHandler created with {} services: {}", serviceMap.size(), serviceMap.keySet());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        MessageType msgType = MessageType.of(msg.getMessageType());
        long requestId = msg.getRequestId();
        logger.trace("Received message type: {}, requestId: {} from channel: {}", msgType, requestId, ctx.channel());

        if (msgType == MessageType.REQUEST) {
            byte serializeType = msg.getSerializeType();
            logger.trace("Deserializing request with serializeType: {}", serializeType);

            Serializer serializer = DefaultSerializerFactory.getSerializer(serializeType);
            Request request;
            try {
                request = serializer.deserialize(msg.getBodyBytes(), Request.class);
            } catch (Exception e) {
                logger.error("Failed to deserialize request, requestId: {}", requestId, e);
                sendResponse(ctx, msg, new Response(StatusCodes.BAD_REQUEST, "Invalid request format"), serializeType);
                return;
            }

            String interfaceName = request.getInterfaceName();
            String methodName = request.getMethodName();
            logger.debug("Processing RPC request - requestId: {}, service: {}.{}, parameterTypes: {}",
                    requestId, interfaceName, methodName, request.getParameterTypes());

            Object instance = serviceMap.get(interfaceName);
            if (instance == null) {
                logger.error("No service instance found for interface: {}", interfaceName);
                sendResponse(ctx, msg,
                        new Response(StatusCodes.SERVICE_NOT_FOUND, "No available instance for service: " + interfaceName),
                        serializeType);
                return;
            }

            logger.trace("Invoking method {} on instance: {}", methodName, instance.getClass().getName());

            Method method;
            try {
                method = instance.getClass().getMethod(methodName, request.getParameterTypes());
            } catch (NoSuchMethodException e) {
                logger.error("Method not found: {}.{}", interfaceName, methodName, e);
                sendResponse(ctx, msg,
                        new Response(StatusCodes.METHOD_NOT_FOUND, "Method not found: " + methodName),
                        serializeType);
                return;
            }

            Object result;
            try {
                result = method.invoke(instance, request.getParameters());
            } catch (IllegalAccessException e) {
                logger.error("Illegal access to method: {}.{}", interfaceName, methodName, e);
                sendResponse(ctx, msg,
                        new Response(StatusCodes.SERVER_ERROR, "Illegal access to method: " + methodName),
                        serializeType);
                return;
            } catch (InvocationTargetException e) {
                // Business method threw an exception
                Throwable cause = e.getCause();
                logger.error("Business method threw exception: {}.{}, cause: {}", interfaceName, methodName,
                        cause.getMessage(), cause);
                sendResponse(ctx, msg,
                        new Response(StatusCodes.FAILURE, cause.getMessage()),
                        serializeType);
                return;
            } catch (Exception e) {
                logger.error("Unexpected error while invoking method: {}.{}", interfaceName, methodName, e);
                sendResponse(ctx, msg,
                        new Response(StatusCodes.INTERNAL_ERROR, "Internal server error: " + e.getMessage()),
                        serializeType);
                return;
            }

            logger.trace("Method {}.{} invoked successfully, result: {}", interfaceName, methodName, result);
            sendResponse(ctx, msg, new Response(StatusCodes.SUCCESS, result), serializeType);
            logger.debug("Response sent for requestId: {}, status: 200", requestId);

        } else if (msgType == MessageType.HEARTBEAT) {
            logger.trace("Received heartbeat requestId: {}, no response needed", requestId);
        } else {
            logger.warn("Received unknown message type: {} for requestId: {}", msgType, requestId);
        }
    }

    /**
     * Sends a response message back to the client.
     *
     * @param ctx           the channel handler context
     * @param requestMsg    the original request message
     * @param response      the response object
     * @param serializeType the serialization type to use
     */
    private void sendResponse(ChannelHandlerContext ctx, Message requestMsg, Response response, byte serializeType) {
        try {
            Serializer serializer = DefaultSerializerFactory.getSerializer(serializeType);
            byte[] bodyBytes = serializer.serialize(response);
            Message message = new Message(
                    MessageType.RESPONSE.getValue(),
                    serializeType,
                    requestMsg.getCompressType(),
                    requestMsg.getRequestId(),
                    bodyBytes.length,
                    bodyBytes);
            ctx.writeAndFlush(message);
        } catch (Exception e) {
            logger.error("Failed to send response, requestId: {}", requestMsg.getRequestId(), e);
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception caught in ServerHandler on channel: {}", ctx.channel(), cause);
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.trace("Server channel active: {}", ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.trace("Server channel inactive: {}", ctx.channel());
    }
}