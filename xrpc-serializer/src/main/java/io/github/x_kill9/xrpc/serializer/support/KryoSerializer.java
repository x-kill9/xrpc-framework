/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.serializer.support;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import io.github.x_kill9.xrpc.core.enums.SerializeType;
import io.github.x_kill9.xrpc.core.exception.SerializerException;
import io.github.x_kill9.xrpc.core.serialize.Serializer;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Kryo-based implementation of {@link Serializer}.
 *
 * <p>This serializer uses a {@link ThreadLocal} {@link Kryo} instance to ensure thread safety.
 * Each thread gets its own Kryo instance, which is configured to support references and
 * use the standard instantiator strategy with fallback.
 *
 * @author x-kill9
 */
public class KryoSerializer implements Serializer {

    private static final Logger logger = LoggerFactory.getLogger(KryoSerializer.class);

    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setReferences(true);
        kryo.setRegistrationRequired(false);
        ((DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy())
                .setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) throws SerializerException {
        logger.trace("Serializing object: {}", obj.getClass().getName());
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            kryo.writeObject(output, obj);
            kryoThreadLocal.remove(); // Remove to avoid accumulating Kryo instances (optional)
            byte[] result = output.toBytes();
            logger.trace("Serialized object to {} bytes", result.length);
            return result;
        } catch (IOException e) {
            logger.error("Serialization failed for object: {}", obj.getClass().getName(), e);
            throw new SerializerException("Serialization failed", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws SerializerException {
        logger.trace("Deserializing {} bytes to type: {}", bytes.length, clazz.getName());
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            Object obj = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            T result = clazz.cast(obj);
            logger.trace("Deserialized object: {}", result);
            return result;
        } catch (IOException e) {
            logger.error("Deserialization failed for type: {}, bytes length: {}", clazz.getName(), bytes.length, e);
            throw new SerializerException("Deserialization failed", e);
        }
    }

    @Override
    public byte getTypeId() {
        return SerializeType.KRYO.getValue();
    }
}