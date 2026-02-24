/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.enums;

/**
 * Types of messages exchanged in XRPC protocol.
 *
 * @author x-kill9
 */
public enum MessageType {

    /**
     * RPC request message.
     */
    REQUEST((byte) 0x01),

    /**
     * RPC response message.
     */
    RESPONSE((byte) 0x02),

    /**
     * Heartbeat message for keep-alive.
     */
    HEARTBEAT((byte) 0x03);

    private final byte value;

    MessageType(byte value) {
        this.value = value;
    }

    /**
     * Returns the byte value representing this message type.
     *
     * @return the byte value
     */
    public byte getValue() {
        return value;
    }

    /**
     * Returns the {@code MessageType} corresponding to the given byte value.
     *
     * @param value the byte value
     * @return the matching message type
     * @throws IllegalArgumentException if no matching type exists
     */
    public static MessageType of(byte value) {
        for (MessageType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown message type: " + value);
    }
}