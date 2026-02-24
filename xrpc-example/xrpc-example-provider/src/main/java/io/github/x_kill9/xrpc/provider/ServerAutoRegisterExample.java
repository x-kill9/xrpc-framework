package io.github.x_kill9.xrpc.provider;

import io.github.x_kill9.xrpc.annotation.bootstrap.RpcServiceBootstrap;
import io.github.x_kill9.xrpc.annotation.export.RpcServiceExporter;
import io.github.x_kill9.xrpc.core.container.RpcContainer;
import io.github.x_kill9.xrpc.netty.server.NettyServer;

import java.util.Map;

/**
 * Server example using annotation-driven automatic service registration.
 *
 * @author x-kill9
 */
public class ServerAutoRegisterExample {
    public static void main(String[] args) throws Exception {
        // 1. Scan and register all @RpcService beans automatically
        RpcServiceBootstrap.start(ServerAutoRegisterExample.class.getPackage().getName());

        // 2. Export service map from container (interface name -> service instance)
        RpcContainer container = RpcContainer.getInstance();
        Map<String, Object> serviceMap = RpcServiceExporter.getExportedServices(container);

        // 3. Start Netty server with the service map
        NettyServer server = new NettyServer("127.0.0.1", 8080, serviceMap);
        server.start();
    }
}