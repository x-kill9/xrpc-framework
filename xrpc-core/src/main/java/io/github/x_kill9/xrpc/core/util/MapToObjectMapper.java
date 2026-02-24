/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.core.util;

import io.github.x_kill9.xrpc.core.exception.XRpcException;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Utility for mapping a {@link Map} structure to a Java object.
 *
 * <p>This class provides reflection-based mapping of configuration data (usually parsed from YAML)
 * to POJO instances. It supports primitive types, wrappers, nested objects, and collections.
 *
 * @author x-kill9
 */
public final class MapToObjectMapper {

    // Cache of declared fields for each class (including superclasses)
    private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    // Registry for custom type converters
    private static final Map<Class<?>, TypeConverter<?>> CUSTOM_CONVERTERS = new ConcurrentHashMap<>();

    // Built-in converters for common types
    private static final Map<Class<?>, Function<String, ?>> BUILT_IN_CONVERTERS = new HashMap<>();

    static {
        BUILT_IN_CONVERTERS.put(String.class, String::valueOf);
        BUILT_IN_CONVERTERS.put(Integer.class, Integer::parseInt);
        BUILT_IN_CONVERTERS.put(Long.class, Long::parseLong);
        BUILT_IN_CONVERTERS.put(Double.class, Double::parseDouble);
        BUILT_IN_CONVERTERS.put(Boolean.class, Boolean::parseBoolean);
        BUILT_IN_CONVERTERS.put(int.class, Integer::parseInt);
        BUILT_IN_CONVERTERS.put(long.class, Long::parseLong);
        BUILT_IN_CONVERTERS.put(double.class, Double::parseDouble);
        BUILT_IN_CONVERTERS.put(boolean.class, Boolean::parseBoolean);
        BUILT_IN_CONVERTERS.put(float.class, Float::parseFloat);
        BUILT_IN_CONVERTERS.put(Float.class, Float::parseFloat);
        BUILT_IN_CONVERTERS.put(short.class, Short::parseShort);
        BUILT_IN_CONVERTERS.put(Short.class, Short::parseShort);
        BUILT_IN_CONVERTERS.put(byte.class, Byte::parseByte);
        BUILT_IN_CONVERTERS.put(Byte.class, Byte::parseByte);
        BUILT_IN_CONVERTERS.put(char.class, s -> s.charAt(0));
        BUILT_IN_CONVERTERS.put(Character.class, s -> s.charAt(0));
    }

    private MapToObjectMapper() {
        // Prevent instantiation
    }

    /**
     * Maps a map of field names to values into an instance of the target class.
     *
     * @param map   the source map (keys are field names, values are field values)
     * @param clazz the target class to instantiate and populate
     * @param <T>   the type of the target class
     * @return a populated instance of {@code clazz}
     * @throws XRpcException if instantiation or field assignment fails
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        try {
            T obj = clazz.getDeclaredConstructor().newInstance();
            List<Field> fields = getFieldCache(clazz);
            for (Field field : fields) {
                Object value = map.get(field.getName());
                if (value != null) {
                    setField(obj, field, value);
                }
            }
            return obj;
        } catch (ReflectiveOperationException e) {
            throw new XRpcException("Failed to instantiate or access class: " + clazz.getName(), e);
        } catch (Exception e) {
            throw new XRpcException("Unexpected error during config mapping", e);
        }
    }

    /**
     * Sets the value of a field on the target object, performing necessary type conversions.
     *
     * @param obj   the target object
     * @param field the field to set
     * @param value the raw value from the map
     * @throws IllegalAccessException if the field is inaccessible
     */
    private static void setField(Object obj, Field field, Object value) throws IllegalAccessException {
        field.setAccessible(true);
        Class<?> type = field.getType();

        if (type.isAssignableFrom(value.getClass())) {
            field.set(obj, value);
        } else if (BUILT_IN_CONVERTERS.containsKey(type)) {
            String stringValue = value.toString();
            Object converted = BUILT_IN_CONVERTERS.get(type).apply(stringValue);
            field.set(obj, converted);
        } else if (isComplexType(type)) {
            Object complexValue = handleComplexType(type, value);
            field.set(obj, complexValue);
        } else {
            throw new IllegalArgumentException(
                    "Unsupported field type: " + type + ". Value: " + value + " (" + value.getClass() + ")"
            );
        }
    }

    /**
     * Determines if a type is a custom complex type (not a primitive or from java.lang).
     *
     * @param type the type to check
     * @return true if the type is complex
     */
    private static boolean isComplexType(Class<?> type) {
        return !type.isPrimitive() && !type.getName().startsWith("java.lang");
    }

    /**
     * Handles conversion for complex types: lists, maps, and custom objects.
     *
     * @param type  the target type
     * @param value the raw value
     * @return the converted value
     */
    private static Object handleComplexType(Class<?> type, Object value) {
        if (CUSTOM_CONVERTERS.containsKey(type)) {
            return CUSTOM_CONVERTERS.get(type).convert(value);
        } else if (List.class.isAssignableFrom(type)) {
            return parseList(value);
        } else if (Map.class.isAssignableFrom(type)) {
            return parseMap(value);
        } else {
            return parseCustomObject(type, value);
        }
    }

    /**
     * Converts a raw value into a List.
     *
     * @param value the raw value (expected to be iterable)
     * @return a new ArrayList containing the elements
     */
    private static List<?> parseList(Object value) {
        List<Object> list = new ArrayList<>();
        if (value instanceof Iterable<?>) {
            for (Object item : (Iterable<?>) value) {
                list.add(item);
            }
        }
        return list;
    }

    /**
     * Converts a raw value into a Map.
     *
     * @param value the raw value (must be a Map)
     * @return the map itself
     * @throws IllegalArgumentException if value is not a Map
     */
    private static Map<?, ?> parseMap(Object value) {
        if (value instanceof Map<?, ?>) {
            return (Map<?, ?>) value;
        }
        throw new IllegalArgumentException("Unsupported map value: " + value);
    }

    /**
     * Recursively builds a custom object from a map.
     *
     * @param type  the target object type
     * @param value the raw value (must be a Map)
     * @return an instance of the custom object
     */
    private static Object parseCustomObject(Class<?> type, Object value) {
        try {
            Object instance = type.getDeclaredConstructor().newInstance();
            if (value instanceof Map<?, ?>) {
                Map<?, ?> map = (Map<?, ?>) value;
                for (Field field : getFieldCache(type)) {
                    Object fieldValue = map.get(field.getName());
                    if (fieldValue != null) {
                        setField(instance, field, fieldValue);
                    }
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + type, e);
        }
    }

    /**
     * Retrieves cached field list for a class, including fields from superclasses.
     *
     * @param clazz the class
     * @return list of fields
     */
    private static List<Field> getFieldCache(Class<?> clazz) {
        return FIELD_CACHE.computeIfAbsent(clazz, cls -> {
            List<Field> fields = new ArrayList<>();
            while (cls != null) {
                Collections.addAll(fields, cls.getDeclaredFields());
                cls = cls.getSuperclass();
            }
            return fields;
        });
    }

    /**
     * Registers a custom type converter.
     *
     * @param type      the target type
     * @param converter the converter implementation
     * @param <T>       the type
     */
    public static <T> void registerConverter(Class<T> type, TypeConverter<T> converter) {
        CUSTOM_CONVERTERS.put(type, converter);
    }

    /**
     * Functional interface for custom type converters.
     *
     * @param <T> the target type
     */
    @FunctionalInterface
    public interface TypeConverter<T> {
        /**
         * Converts a raw value to the target type.
         *
         * @param value the raw value
         * @return the converted value
         */
        T convert(Object value);
    }
}