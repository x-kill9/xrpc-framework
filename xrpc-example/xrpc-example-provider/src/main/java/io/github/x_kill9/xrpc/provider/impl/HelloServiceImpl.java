/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.provider.impl;

import io.github.x_kill9.xrpc.annotation.RpcService;
import io.github.x_kill9.xrpc.api.HelloService;

@RpcService
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello() {
        return "Hello";
    }
}