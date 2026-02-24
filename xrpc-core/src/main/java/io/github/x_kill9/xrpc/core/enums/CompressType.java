/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.enums;

/**
 * Compression algorithms supported by XRPC.
 *
 * @author x-kill9
 */
public enum CompressType {

    /**
     * No compression.
     */
    NONE((byte) 0x00),

    /**
     * GZIP compression.
     */
    GZIP((byte) 0x01),

    /**
     * Snappy compression.
     */
    SNAPPY((byte) 0x02);

    private final byte value;

    CompressType(byte value) {
        this.value = value;
    }

    /**
     * Returns the byte value representing this compression type.
     *
     * @return the byte value
     */
    public byte getValue() {
        return value;
    }

    /**
     * Returns the {@code CompressType} corresponding to the given byte value.
     *
     * @param value the byte value
     * @return the matching compression type
     * @throws IllegalArgumentException if no matching type exists
     */
    public static CompressType of(byte value) {
        for (CompressType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown compress type: " + value);
    }
}