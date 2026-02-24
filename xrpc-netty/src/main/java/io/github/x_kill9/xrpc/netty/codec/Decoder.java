/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.netty.codec;

import io.github.x_kill9.xrpc.core.constants.Constants;
import io.github.x_kill9.xrpc.core.exception.DecoderException;
import io.github.x_kill9.xrpc.core.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Decodes incoming bytes into {@link Message} objects according to the XRPC protocol.
 *
 * <p>The protocol header consists of:
 * <ul>
 *   <li>magic number (2 bytes)</li>
 *   <li>version (1 byte)</li>
 *   <li>message type (1 byte)</li>
 *   <li>serialization type (1 byte)</li>
 *   <li>compression type (1 byte)</li>
 *   <li>request ID (8 bytes)</li>
 *   <li>body length (4 bytes)</li>
 * </ul>
 * followed by the body of the specified length.
 *
 * @author x-kill9
 */
public class Decoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(Decoder.class);
    private static final short HEADER_SIZE = Constants.HEADER_SIZE;
    private static final short MAGIC = Constants.MAGIC;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            in.markReaderIndex();

            if (in.readableBytes() < HEADER_SIZE) {
                return;
            }

            short magic = in.readShort();
            if (magic != MAGIC) {
                logger.error("Invalid magic number: 0x{} from channel: {}, closing connection",
                        Integer.toHexString(magic), ctx.channel());
                in.clear();
                ctx.close();
                throw new DecoderException("Invalid magic: 0x" + Integer.toHexString(magic));
            }

            byte version = in.readByte();
            byte msgType = in.readByte();
            byte serializeType = in.readByte();
            byte compressType = in.readByte();
            long requestId = in.readLong();
            int bodyLength = in.readInt();

            logger.trace("Decoded header - version: {}, msgType: {}, serializeType: {}, compressType: {}, requestId: {}, bodyLength: {}",
                    version, msgType, serializeType, compressType, requestId, bodyLength);

            if (in.readableBytes() < bodyLength) {
                logger.trace("Insufficient data for body, expected: {}, available: {}, resetting reader index",
                        bodyLength, in.readableBytes());
                in.resetReaderIndex();
                return;
            }

            byte[] bodyBytes = new byte[bodyLength];
            in.readBytes(bodyBytes);
            logger.trace("Read body bytes: {} bytes", bodyLength);

            Message message = new Message();
            message.setVersion(version);
            message.setMessageType(msgType);
            message.setSerializeType(serializeType);
            message.setCompressType(compressType);
            message.setRequestId(requestId);
            message.setBodyBytes(bodyBytes);

            out.add(message);
            logger.debug("Decoded XMessage, requestId: {}, msgType: {}, bodySize: {}",
                    requestId, msgType, bodyLength);
        } catch (DecoderException e) {
            logger.error("Decoder exception: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during decoding", e);
        }
    }
}