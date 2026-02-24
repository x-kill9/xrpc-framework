/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.serialize;

import java.io.IOException;

/**
 * SPI interface for serialization and deserialization.
 *
 * <p>Implementations of this interface handle the conversion of Java objects to byte arrays
 * and vice versa. Each serializer is identified by a unique type ID.
 *
 * @author x-kill9
 */
public interface Serializer {

    /**
     * Serializes the given object into a byte array.
     *
     * @param obj the object to serialize
     * @return the serialized byte array
     * @throws IOException if serialization fails
     */
    byte[] serialize(Object obj) throws IOException;

    /**
     * Deserializes the given byte array into an object of the specified class.
     *
     * @param bytes the byte array to deserialize
     * @param clazz the target class
     * @param <T>   the type of the result
     * @return the deserialized object
     * @throws IOException if deserialization fails
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException;

    /**
     * Returns the unique type ID of this serializer.
     *
     * @return the type ID (e.g., 1 for Kryo, 2 for Protobuf)
     */
    byte getTypeId();
}