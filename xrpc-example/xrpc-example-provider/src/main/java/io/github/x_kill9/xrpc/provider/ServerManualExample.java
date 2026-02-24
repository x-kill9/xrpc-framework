package io.github.x_kill9.xrpc.provider;

import io.github.x_kill9.xrpc.api.CalculatorService;
import io.github.x_kill9.xrpc.api.HelloService;
import io.github.x_kill9.xrpc.core.container.RpcContainer;
import io.github.x_kill9.xrpc.core.util.ServiceMapBuilder;
import io.github.x_kill9.xrpc.netty.server.NettyServer;
import io.github.x_kill9.xrpc.provider.impl.CalculatorImpl;
import io.github.x_kill9.xrpc.provider.impl.HelloServiceImpl;

import java.util.Map;

/**
 * Server example using manual service registration.
 *
 * @author x-kill9
 */
public class ServerManualExample {
    public static void main(String[] args) throws Exception {
        // 1. Create service instances manually
        CalculatorService calculator = new CalculatorImpl();
        HelloService helloService = new HelloServiceImpl();

        // 2. Register them into container with explicit bean names
        RpcContainer container = RpcContainer.getInstance();
        container.registerBean(CalculatorImpl.class, calculator, "calculator");
        container.registerBean(HelloServiceImpl.class, helloService, "helloService");

        // 3. Build service map manually (interface name -> instance)
        Map<String, Object> serviceMap = ServiceMapBuilder.buildFromContainer(container);

        // 4. Start Netty server
        NettyServer server = new NettyServer("127.0.0.1", 8081, serviceMap);
        server.start();
    }
}