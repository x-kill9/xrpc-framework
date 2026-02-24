/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.api;

public interface CalculatorService {
    int add(int a, int b);

    int subtract(int a, int b);

    int multiply(int a, int b);

    int divide(int a, int b);
}