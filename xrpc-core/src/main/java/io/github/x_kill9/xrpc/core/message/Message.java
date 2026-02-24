/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.message;

import io.github.x_kill9.xrpc.core.constants.Constants;

/**
 * Represents the protocol-level message exchanged between client and server.
 *
 * <p>A message consists of a fixed header and a variable-length body.
 * The header includes version, message type, serialization type, compression type,
 * request ID, and body length. The body contains the serialized request or response.
 *
 * @author x-kill9
 */
public class Message {

    /**
     * Protocol version (see {@link Constants#PROTOCOL_VERSION}).
     */
    private byte version = Constants.PROTOCOL_VERSION;

    /**
     * Type of the message: request, response, or heartbeat (see {@link io.github.x_kill9.xrpc.core.enums.MessageType}).
     */
    private byte messageType;

    /**
     * Serialization format used for the body (see {@link io.github.x_kill9.xrpc.core.enums.SerializeType}).
     */
    private byte serializeType;

    /**
     * Compression algorithm applied to the body (see {@link io.github.x_kill9.xrpc.core.enums.CompressType}).
     */
    private byte compressType;

    /**
     * Unique identifier for matching requests with responses.
     */
    private long requestId;

    /**
     * Length of the body in bytes.
     */
    private int bodyLength;

    /**
     * Serialized body data (request or response).
     */
    private byte[] bodyBytes;

    public Message() {
    }

    public Message(byte messageType, byte serializeType, byte compressType, long requestId, int bodyLength, byte[] bodyBytes) {
        this.messageType = messageType;
        this.serializeType = serializeType;
        this.compressType = compressType;
        this.requestId = requestId;
        this.bodyLength = bodyLength;
        this.bodyBytes = bodyBytes;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getMessageType() {
        return messageType;
    }

    public void setMessageType(byte messageType) {
        this.messageType = messageType;
    }

    public byte getSerializeType() {
        return serializeType;
    }

    public void setSerializeType(byte serializeType) {
        this.serializeType = serializeType;
    }

    public byte getCompressType() {
        return compressType;
    }

    public void setCompressType(byte compressType) {
        this.compressType = compressType;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public int getBodyLength() {
        return bodyLength;
    }

    public void setBodyLength(int bodyLength) {
        this.bodyLength = bodyLength;
    }

    public byte[] getBodyBytes() {
        return bodyBytes;
    }

    public void setBodyBytes(byte[] bodyBytes) {
        this.bodyBytes = bodyBytes;
    }
}