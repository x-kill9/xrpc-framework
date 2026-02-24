/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as an RPC service to be registered in the container.
 * Services are automatically scanned and managed as singleton beans.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcService {

    /**
     * Bean name. If empty, use class name with first letter lowercase.
     */
    String name() default "";

    /**
     * Whether this is the primary implementation when multiple exist.
     */
    boolean primary() default false;

    /**
     * The interface class that this service implements.
     */
    Class<?> interfaceClass() default None.class;

    /**
     * The default value for the flag "interface not specified".
     */
    final class None {
    }
}