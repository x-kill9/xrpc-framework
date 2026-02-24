/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.constants;

/**
 * Protocol-level constants for XRPC.
 *
 * <p>This class defines fixed values used in the XRPC protocol, such as the magic number,
 * protocol version, and header size. These constants are essential for encoding and decoding
 * messages over the wire.
 *
 * @author x-kill9
 */
public final class Constants {

    /**
     * Magic number identifying XRPC protocol (0x7872 = "xr" in ASCII).
     */
    public static final short MAGIC = (short) 0x7872;

    /**
     * Current protocol version.
     */
    public static final byte PROTOCOL_VERSION = 0x01;

    /**
     * Fixed header size in bytes for all XRPC messages.
     */
    public static final int HEADER_SIZE = 14;

    private Constants() {
        // Prevent instantiation
    }
}