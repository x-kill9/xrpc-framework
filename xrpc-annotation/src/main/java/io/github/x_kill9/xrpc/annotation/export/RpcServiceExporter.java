/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.annotation.export;

import io.github.x_kill9.xrpc.annotation.RpcService;
import io.github.x_kill9.xrpc.core.container.RpcContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for exporting RPC services from the container.
 * <p>
 * This class scans all beans registered in the {@link RpcContainer} that are annotated
 * with {@link RpcService}, resolves the appropriate service interface, and builds
 * a mapping from interface name to service instance.
 *
 * @author x-kill9
 */
public class RpcServiceExporter {

    /**
     * Builds a map of interface names to service instances for all beans annotated
     * with {@code @RpcService} in the given container.
     *
     * @param container the RPC container from which to retrieve beans
     * @return a map where keys are fully qualified interface names and values are the corresponding service instances
     * @throws ServiceExportException if a service class does not satisfy the conditions for exporting
     */
    public static Map<String, Object> getExportedServices(RpcContainer container) {
        final Map<String, Object> serviceMap = new HashMap<>();

        for (Class<?> clazz : container.getAllBeanClasses()) {
            RpcService annotation = clazz.getAnnotation(RpcService.class);
            if (annotation == null) {
                continue;
            }

            final Object instance = container.getBean(clazz);
            final Class<?> targetInterface = resolveTargetInterface(clazz, annotation);
            serviceMap.put(targetInterface.getName(), instance);
        }

        return serviceMap;
    }

    /**
     * Resolves the target service interface for a given implementation class.
     * <p>
     * If the {@link RpcService#interfaceClass()} is explicitly provided, it validates
     * that the implementation actually implements that interface. If no interface is
     * specified, it automatically picks the sole implemented interface. If multiple
     * interfaces are implemented and none is specified, an exception is thrown.
     *
     * @param implClass   the service implementation class
     * @param annotation  the {@link RpcService} annotation on the class
     * @return the resolved service interface class
     * @throws ServiceExportException if the interface cannot be resolved unambiguously
     */
    private static Class<?> resolveTargetInterface(Class<?> implClass, RpcService annotation) {
        final Class<?> specified = annotation.interfaceClass();

        if (specified != RpcService.None.class) {
            if (!implClass.isAssignableFrom(specified) && !specified.isAssignableFrom(implClass)) {
                throw new ServiceExportException(String.format(
                        "Class %s is annotated with @RpcService specifying interface %s, but it does not implement that interface",
                        implClass.getName(), specified.getName()
                ));
            }
            return specified;
        }

        final Class<?>[] interfaces = implClass.getInterfaces();
        if (interfaces.length == 1) {
            return interfaces[0];
        }

        throw new ServiceExportException(String.format(
                "Class %s implements multiple interfaces; please specify which one to export via @RpcService.interfaceClass()",
                implClass.getName()
        ));
    }
}