/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.provider.impl;

import io.github.x_kill9.xrpc.annotation.RpcService;
import io.github.x_kill9.xrpc.api.CalculatorService;

/**
 * TODO: Add class description.
 *
 * @author x-kill9
 */
@RpcService
public class CalculatorImpl implements CalculatorService {
    @Override
    public int add(int a, int b) {
        return a + b;
    }

    @Override
    public int subtract(int a, int b) {
        return a - b;
    }

    @Override
    public int multiply(int a, int b) {
        return a * b;
    }

    @Override
    public int divide(int a, int b) {
        return a / b;
    }
}