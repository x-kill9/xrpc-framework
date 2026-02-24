package io.github.x_kill9.xrpc.consumer;

import io.github.x_kill9.xrpc.annotation.RpcComponent;
import io.github.x_kill9.xrpc.annotation.RpcReference;
import io.github.x_kill9.xrpc.annotation.bootstrap.RpcClientBootstrap;
import io.github.x_kill9.xrpc.api.HelloService;
import io.github.x_kill9.xrpc.netty.client.NettyRpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example demonstrating automatic dependency injection using annotations.
 *
 * @author x-kill9
 */
@RpcComponent
public class ClientAutoInjectExample {

    private static final Logger logger = LoggerFactory.getLogger(ClientAutoInjectExample.class);

    @RpcReference
    private HelloService helloService;

    public void run() {
        String result = helloService.sayHello();
        logger.info("Auto-injected HelloService result: {}", result);
    }

    public static void main(String[] args) throws Exception {
        // RpcClientBootstrap will scan the package of AutoInjectExample,
        // instantiate it, and inject all @RpcReference fields.
        ClientAutoInjectExample example = RpcClientBootstrap.run(ClientAutoInjectExample.class);
        example.run();

        Runtime.getRuntime().addShutdownHook(new Thread(NettyRpcClient::shutdown));
    }
}