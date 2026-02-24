/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.netty.codec;

import io.github.x_kill9.xrpc.core.constants.Constants;
import io.github.x_kill9.xrpc.core.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Encodes {@link Message} objects into bytes according to the XRPC protocol.
 *
 * <p>The encoding order follows the fixed header:
 * magic (2 bytes), version, message type, serialization type, compression type,
 * request ID (8 bytes), body length (4 bytes), and then the body bytes.
 *
 * @author x-kill9
 */
public class Encoder extends MessageToByteEncoder<Message> {

    private static final short MAGIC = Constants.MAGIC;

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) {
        out.writeShort(MAGIC);
        out.writeByte(msg.getVersion());
        out.writeByte(msg.getMessageType());
        out.writeByte(msg.getSerializeType());
        out.writeByte(msg.getCompressType());
        out.writeLong(msg.getRequestId());
        out.writeInt(msg.getBodyLength());
        if (msg.getBodyBytes() != null) {
            out.writeBytes(msg.getBodyBytes());
        }
    }
}