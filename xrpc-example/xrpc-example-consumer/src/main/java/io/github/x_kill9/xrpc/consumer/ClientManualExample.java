package io.github.x_kill9.xrpc.consumer;

import io.github.x_kill9.xrpc.api.CalculatorService;
import io.github.x_kill9.xrpc.core.config.factory.ConfigFactory;
import io.github.x_kill9.xrpc.core.proxy.jdk.JdkProxyFactory;
import io.github.x_kill9.xrpc.core.spi.ExtensionLoader;
import io.github.x_kill9.xrpc.core.transport.RpcClient;
import io.github.x_kill9.xrpc.netty.client.NettyRpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientManualExample {
    private static final Logger logger = LoggerFactory.getLogger(ClientManualExample.class);

    public static void main(String[] args) throws Exception {
        // 1. Get transport type from config (default is "netty")
        final String transport = ConfigFactory.getConfig().getClient().getTransport();
        // 2. Create RPC client via SPI
        RpcClient client = ExtensionLoader.getExtensionLoader(RpcClient.class)
                .getExtension(transport);
        // 3. Create JDK proxy factory
        final JdkProxyFactory proxyFactory = new JdkProxyFactory(client);
        // 4. Get proxy for the Calculator service interface
        final CalculatorService calculator = proxyFactory.getProxy(CalculatorService.class);

        // 5. Make a simple RPC call
        int result = calculator.multiply(11, 20);
        logger.info("Result of 11 * 20 = {}", result);

        // 6. Shutdown hook to release resources
        Runtime.getRuntime().addShutdownHook(new Thread(NettyRpcClient::shutdown));
    }
}