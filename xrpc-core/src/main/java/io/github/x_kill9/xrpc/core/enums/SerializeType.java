/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.enums;

/**
 * Serialization formats supported by XRPC.
 *
 * @author x-kill9
 */
public enum SerializeType {

    /**
     * Kryo serialization.
     */
    KRYO((byte) 0x01),

    /**
     * Protocol Buffers serialization.
     */
    PROTOBUF((byte) 0x02),

    /**
     * JSON serialization.
     */
    JSON((byte) 0x03);

    private final byte value;

    SerializeType(byte value) {
        this.value = value;
    }

    /**
     * Returns the byte value representing this serialization type.
     *
     * @return the byte value
     */
    public byte getValue() {
        return value;
    }

    /**
     * Returns the {@code SerializeType} corresponding to the given byte value.
     *
     * @param value the byte value
     * @return the matching serialization type
     * @throws IllegalArgumentException if no matching type exists
     */
    public static SerializeType of(byte value) {
        for (SerializeType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown serialize type: " + value);
    }
}