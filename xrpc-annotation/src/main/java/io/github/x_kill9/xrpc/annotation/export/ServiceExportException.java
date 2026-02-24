/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.annotation.export;


import io.github.x_kill9.xrpc.core.exception.XRpcException;

public class ServiceExportException extends XRpcException {
    public ServiceExportException(String message) {
        super(message);
    }
}
