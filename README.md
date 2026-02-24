# X-RPC Framework

[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://www.oracle.com/java/)
[![Netty](https://img.shields.io/badge/Netty-4.1.117-blue)](https://netty.io/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

[中文](README.zh-CN.md) | English

X-RPC is a high-performance, lightweight RPC (Remote Procedure Call) framework built on Netty, designed for microservices architecture. It provides service governance features including service registration and discovery, load balancing, rate limiting, circuit breaking, and more.

## 📋 Table of Contents

- [Core Features](#core-features)
- [Architecture Design](#architecture-design)
- [Quick Start](#quick-start)
- [Detailed Usage Guide](#detailed-usage-guide)
- [Configuration Manual](#configuration-manual)
- [Module Structure](#module-structure)
- [Example Code](#example-code)
- [Tech Stack](#tech-stack)
- [Roadmap](#roadmap)
- [Contributing Guide](#contributing-guide)
- [License](#license)

## ✨ Core Features

### 🎯 Service Governance

- **Service Registration and Discovery**: Built-in ZooKeeper support for service registration and discovery
- **Load Balancing**: Support for random and round-robin load balancing strategies
- **Rate Limiting**: Token bucket algorithm for traffic control
- **Circuit Breaking**: Sliding window algorithm for fault tolerance
- **Logging**: Request/response logging and tracing

### 🔧 Technical Features

- **High Performance**: Asynchronous non-blocking I/O based on Netty
- **Pluggable Architecture**: SPI extension mechanism for easy customization
- **Multiple Invocation Methods**: Support for manual API calls and annotation-driven injection
- **Flexible Configuration**: YAML format configuration for easy management
- **Kryo Serialization**: High-performance serialization for more efficient data transfer

### 🏗️ Architecture Design

```
┌─────────────────────────────────────────────────────────────┐
│                        Application Layer                     │
│  ┌──────────────┐          ┌──────────────┐                │
│  │   Service    │          │   Service    │                │
│  │   Consumer   │          │   Provider   │                │
│  │ @RpcReference│          │  @RpcService │                │
│  └──────┬───────┘          └──────┬───────┘                │
└─────────┼───────────────────────────┼────────────────────────┘
          │                           │
┌─────────▼───────────────────────────▼────────────────────────┐
│                      Framework Core Layer                   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │   Proxy  │  │   Chain  │  │  Config  │  │   SPI    │   │
│  │  Factory │─▶│ (Chain)  │─▶│  Loader  │─▶│  Loader  │   │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘   │
└───────┼───────────────┼───────────────┼───────────────┼──────┘
        │               │               │               │
┌───────▼───────────────▼───────────────▼───────────────▼──────┐
│                    Infrastructure Layer                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ Network  │  │ Registry │  │   Ser-   │  │  Load    │   │
│  │ Transport│  │  Center  │  │ ializer  │  │ Balancer │   │
│  │ (Netty)  │  │(ZooKeeper)│  │  (Kryo)  │  │(Random/  │   │
│  └──────────┘  └──────────┘  └──────────┘  │ Round-   │   │
│                                             │ robin)   │   │
│ Note: xrpc-all and xrpc-bom are management  └──────────┘   │
│       modules for simplified dependency management         │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 Quick Start

### Requirements

- JDK 17 or higher
- Maven 3.6+
- ZooKeeper 3.5+ (required for service registration and discovery)

**About ZooKeeper**: Currently X-RPC requires an external ZooKeeper instance for service registration. We plan to introduce a built-in local registry center in future versions to simplify deployment.

### Installation Steps

1. Clone the repository:

```bash
git clone https://github.com/x-kill9/xrpc-framework.git
cd xrpc-framework
```

2. Build the project:

```bash
mvn clean install
```

3. Add dependencies to your project (choose one of the following methods):

**Method A: Using BOM for dependency management (recommended)**

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.x-kill9</groupId>
            <artifactId>xrpc-bom</artifactId>
            <version>1.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<!-- Then add specific modules without specifying version -->
<dependencies>
    <dependency>
        <groupId>io.github.x-kill9</groupId>
        <artifactId>xrpc-core</artifactId>
    </dependency>
</dependencies>
```

**Method B: Using aggregate module**

```xml
<dependency>
    <groupId>io.github.x-kill9</groupId>
    <artifactId>xrpc-all</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Method C: Add individual modules**

```xml
<dependency>
    <groupId>io.github.x-kill9</groupId>
    <artifactId>xrpc-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage

#### 1. Define Service Interface

```java
public interface HelloService {
    String sayHello(String name);
}
```

#### 2. Implement Service Provider

```java
@RpcService
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello, " + name + "!";
    }
}
```

#### 3. Configure Provider (xrpc.yaml)

```yaml
xrpc:
  server:
    port: 8080
  registry:
    type: zookeeper
    address: 127.0.0.1:2181
```

#### 4. Start Provider

```java
public class ProviderApplication {
    public static void main(String[] args) {
        // Auto-register services via @RpcService annotation - xrpc-annotation module required
        RpcContainer container = RpcContainer.getInstance();
        Map<String, Object> serviceMap = RpcServiceExporter.getExportedServices(container);
        
        // Or manually register services
        container.registerBean(CalculatorImpl.class, calculator, "calculator");
        container.registerBean(HelloServiceImpl.class, helloService, "helloService");
        Map<String, Object> serviceMap = ServiceMapBuilder.buildFromContainer(container);
        
        NettyServer server = new NettyServer("127.0.0.1", 8080, serviceMap);
        server.start();
    }
}
```

#### 5. Configure Consumer (xrpc.yaml)

```yaml
xrpc:
  client:
    serializer: kryo
    loadBalancer: round
    connectTimeout: 3000
    callTimeout: 3000
    
    interceptors:
      - name: trace
        properties:
          level: INFO
          logArgs: true
          logResult: true
      
      - name: rateLimiter
        properties:
          type: tokenBucket
          params:
            capacity: 200
            refillRate: 20
      
      - name: circuitBreaker
        properties:
          type: slidingWindow
          params:
            failureThreshold: 5
            timeoutMs: 10000
            windowSize: 10000
            
  registry:
    type: zookeeper
    address: 127.0.0.1:2181
```

#### 6. Use Service in Consumer

```java
public class ConsumerApplication {
    public static void main(String[] args) {
        final String transport = ConfigFactory.getConfig().getClient().getTransport();
        RpcClient client = ExtensionLoader.getExtensionLoader(RpcClient.class)
                .getExtension(transport);
        
        JdkProxyFactory proxyFactory = new JdkProxyFactory(client);
        HelloService helloService = proxyFactory.getProxy(HelloService.class);
        
        String result = helloService.sayHello("World");
        System.out.println(result); // Output: Hello, World!
    }
}
```

## 📖 Detailed Usage Guide

### Service Registration and Discovery

#### Using Annotations (Recommended)

**Service Provider:**

```java
@RpcService
public class UserServiceImpl implements UserService {
    // Implementation methods
}
```

**Service Consumer:**

```java
public class OrderService {
    @RpcReference
    private UserService userService;
    
    public void createOrder() {
        User user = userService.getUserById(123L);
        // Business logic
    }
}
```

#### Manual Registration

```java
// Register service
RpcContainer container = RpcContainer.getInstance();
container.registerBean(CalculatorImpl.class, new CalculatorImpl(), "calculator");

// Build service map
Map<String, Object> serviceMap = new HashMap<>();
serviceMap.put(CalculatorService.class.getName(), new CalculatorImpl());

// Start server
NettyServer server = new NettyServer("127.0.0.1", 8081, serviceMap);
server.start();
```

### Load Balancing

Configure load balancing strategy in `xrpc.yaml`:

```yaml
xrpc:
  client:
    loadBalancer: round  # Options: random (random), round (round-robin)
```

Or get from configuration via code:

```java
// Get load balancer type name from configuration
String loadBalancerType = ConfigFactory.getConfig().getClient().getLoadBalancer();

// Get load balancer instance via SPI
LoadBalancer loadBalancer = ExtensionLoader.getExtensionLoader(LoadBalancer.class)
        .getExtension(loadBalancerType);

// Select service instance
ServiceMeta selectedService = loadBalancer.select(serviceMetas);
```

**Custom Load Balancer**: You can develop your own load balancing algorithm by implementing the `LoadBalancer` interface and registering it with SPI. Refer to the [SPI Extension Interfaces](#spi-extension-interfaces) section for more extension points.

### Rate Limiting

Configure rate limiter in `xrpc.yaml`:

```yaml
xrpc:
  client:
    interceptors:
      - name: rateLimiter
        properties:
          type: tokenBucket
          params:
            capacity: 200      # Maximum token bucket capacity
            refillRate: 20     # Refill rate per second
```

### Circuit Breaking

Configure circuit breaker in `xrpc.yaml`:

```yaml
xrpc:
  client:
    interceptors:
      - name: circuitBreaker
        properties:
          type: slidingWindow
          params:
            failureThreshold: 5    # Open circuit after 5 failures
            timeoutMs: 10000       # Try half-open state after 10 seconds
            windowSize: 10000      # Sliding window size (milliseconds)
```

### Custom Interceptor

Implement the `Interceptor` interface:

```java
public class CustomInterceptor implements Interceptor<InterceptorConfig> {
    private InterceptorConfig config;
    
    @Override
    public String getName() {
        return "custom";
    }
    
    @Override
    public Class<InterceptorConfig> getConfigClass() {
        return InterceptorConfig.class;
    }
    
    @Override
    public void setConfig(InterceptorConfig config) {
        this.config = config;
    }
    
    @Override
    public InterceptorConfig getConfig() {
        return config;
    }
    
    @Override
    public Object intercept(InvocationContext context, InterceptorChain chain) throws Throwable {
        // Pre-processing logic
        logger.info("Before request: {}", context.getRequest().getMethodName());
        
        try {
            // Continue interceptor chain execution
            Object result = chain.proceed(context);
            
            // Post-processing logic
            logger.info("After request: {}", result);
            return result;
        } catch (Exception e) {
            // Exception handling logic
            logger.error("Request exception: {}", e.getMessage());
            throw e;
        }
    }
}
```

Register in `META-INF/xrpc/io.github.x_kill9.xrpc.core.invocation.interceptor.Interceptor`:

```properties
custom=com.example.CustomInterceptor
```

Configure in `xrpc.yaml`:

```yaml
xrpc:
  client:
    interceptors:
      - name: custom
```

### SPI Extension Interfaces

The X-RPC framework provides powerful extension capabilities based on the SPI (Service Provider Interface) mechanism. You can extend framework functionality by implementing the following interfaces and registering them in the `META-INF/xrpc/` directory:

#### Extensible Interfaces List

| Interface | Description | Configuration File Path |
|-----------|-------------|------------------------|
| `io.github.x_kill9.xrpc.core.loadbalance.LoadBalancer` | Load balancing strategy | `META-INF/xrpc/io.github.x_kill9.xrpc.core.loadbalance.LoadBalancer` |
| `io.github.x_kill9.xrpc.core.serialize.Serializer` | Serialization method | `META-INF/xrpc/io.github.x_kill9.xrpc.core.serialize.Serializer` |
| `io.github.x_kill9.xrpc.core.registry.RegistryService` | Registry center | `META-INF/xrpc/io.github.x_kill9.xrpc.core.registry.RegistryService` |
| `io.github.x_kill9.xrpc.core.transport.RpcClient` | RPC client | `META-INF/xrpc/io.github.x_kill9.xrpc.core.transport.RpcClient` |
| `io.github.x_kill9.xrpc.core.circuitbreaker.CircuitBreaker` | Circuit breaker | `META-INF/xrpc/io.github.x_kill9.xrpc.core.circuitbreaker.CircuitBreaker` |
| `io.github.x_kill9.xrpc.core.flowcontrol.RateLimiter` | Rate limiter | `META-INF/xrpc/io.github.x_kill9.xrpc.core.flowcontrol.RateLimiter` |
| `io.github.x_kill9.xrpc.core.invocation.interceptor.Interceptor` | Interceptor | `META-INF/xrpc/io.github.x_kill9.xrpc.core.invocation.interceptor.Interceptor` |
| `io.github.x_kill9.xrpc.core.config.loader.ConfigLoader` | Configuration loader | `META-INF/xrpc/io.github.x_kill9.xrpc.core.config.loader.ConfigLoader` |

#### SPI Registration Example

Create corresponding interface files in `src/main/resources/META-INF/xrpc/` directory with format:

```properties
# name=fully-qualified implementation class name
myCustomImpl=com.example.MyCustomImplementation
```

Then reference by name in configuration file:

```yaml
xrpc:
  client:
    loadBalancer: myCustomImpl  # Use custom load balancer
```

#### Extension Example: Custom Serialization

```java
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) throws Exception {
        // JSON serialization implementation
        return jsonString.getBytes();
    }
    
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception {
        // JSON deserialization implementation
        return jsonObject;
    }
}
```

Register in `META-INF/xrpc/io.github.x_kill9.xrpc.core.serialize.Serializer`:

```properties
json=com.example.JsonSerializer
```

Use in configuration:

```yaml
xrpc:
  client:
    serializer: json
```

## ⚙️ Configuration Manual

### Client Configuration

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `serializer` | String | kryo | Serialization method (currently supports kryo) |
| `loadBalancer` | String | random | Load balancing strategy (random/round) |
| `connectTimeout` | Integer | 3000 | Connection timeout (milliseconds) |
| `callTimeout` | Integer | 3000 | Call timeout (milliseconds) |
| `heartbeatIntervalSeconds` | Integer | 60 | Heartbeat interval (seconds) |
| `interceptors` | List | [] | Interceptor chain configuration |

### Server Configuration

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `port` | Integer | 8080 | Service port |
| `bossThreads` | Integer | 1 | Netty boss thread count |
| `workerThreads` | Integer | 4 | Netty worker thread count |

### Registry Configuration

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `type` | String | zookeeper | Registry type (zookeeper/local) |
| `address` | String | - | Registry address (host:port) |
| `timeout` | Integer | 3000 | Connection timeout (milliseconds) |

### Complete Configuration Example

```yaml
xrpc:
  client:
    serializer: kryo
    loadBalancer: round
    connectTimeout: 3000
    callTimeout: 3000
    heartbeatIntervalSeconds: 60
    
    interceptors:
      - name: trace
        properties:
          level: INFO
          logArgs: true
          logResult: true
      
      - name: rateLimiter
        properties:
          type: tokenBucket
          params:
            capacity: 200
            refillRate: 20
      
      - name: circuitBreaker
        properties:
          type: slidingWindow
          params:
            failureThreshold: 5
            timeoutMs: 10000
            windowSize: 10000
            
  server:
    port: 8080
    bossThreads: 1
    workerThreads: 4
    
  registry:
    type: zookeeper
    address: 127.0.0.1:2181
    timeout: 3000
```

## 📦 Module Structure

### Management Modules

#### xrpc-all

Aggregate module containing all X-RPC modules. Use this module when you need the complete framework.

#### xrpc-bom

BOM (Bill of Materials) module for dependency management. Import this module in your project's dependency management to ensure consistent versions across all X-RPC modules.

### Core Modules

#### xrpc-core

Core module containing all basic interfaces and abstractions.

- **Key Classes**:
  - `RpcClient` - RPC client interface
  - `RpcServer` - RPC server interface
  - `ExtensionLoader` - SPI extension loader
  - `JdkProxyFactory` - JDK dynamic proxy factory
  - `Request` / `Response` - RPC message model
  - `XrpcConfig` - Configuration model

#### xrpc-netty

Network transport module based on Netty.

- **Key Classes**:
  - `NettyClient` - Netty-based RPC client implementation
  - `NettyServer` - Netty-based RPC server implementation
  - `RpcClientHandler` - Client message handler
  - `RpcServerHandler` - Server message handler

#### xrpc-registry

Service registration and discovery module.

**xrpc-registry-zookeeper**:

- `ZookeeperRegistry` - ZooKeeper-based service registry
- Supports ephemeral nodes with automatic service deregistration on shutdown
- Listens for service changes
- **Note**: Currently requires external ZooKeeper deployment. Future versions will include a built-in local registry to eliminate this dependency.

**xrpc-registry-local**:

- `LocalRegistry` - In-memory service registry for testing (not yet implemented)

#### xrpc-serializer

Serialization module using Kryo.

- `KryoSerializer` - High-performance serialization based on Kryo
- Thread-safe Kryo instance pool

#### xrpc-loadbalance

Load balancing module.

- `RandomLoadBalancer` - Random load balancing
- `RoundRobinLoadBalancer` - Round-robin load balancing

#### xrpc-ratelimiter

Rate limiting module.

- `TokenBucketRateLimiter` - Token bucket algorithm implementation

#### xrpc-circuitbreaker

Circuit breaker module.

- `SlidingWindowCircuitBreaker` - Sliding window algorithm implementation

#### xrpc-config

Configuration module.

**xrpc-config-yaml**:

- `YamlConfigLoader` - YAML configuration file loader
- Supports complex nested configurations

#### xrpc-logging

Logging interceptor module.

- `TraceInterceptor` - Request/response logging and tracing

#### xrpc-annotation

Annotation definition module.

- `@RpcService` - Marks RPC service provider
- `@RpcReference` - Injects RPC service proxy

### Example Modules

#### xrpc-example-api

Shared API interfaces.

- `HelloService` - Simple greeting service
- `CalculatorService` - Calculator service with arithmetic operations

#### xrpc-example-provider

Service provider implementation.

- `HelloServiceImpl` - Hello service implementation
- `CalculatorImpl` - Calculator service implementation
- `ManualServerExample` - Manual server startup example

#### xrpc-example-consumer

Service consumer implementation.

- `ManualClientExample` - Manual client usage example

## 💻 Example Code

### Running Examples

**Important**: X-RPC currently requires ZooKeeper for service registration and discovery. Please ensure ZooKeeper is deployed and running before starting the examples.

1. Start ZooKeeper:

```bash
# Default port 2181
zkServer.sh start
```

2. Start provider: io.github.x_kill9.xrpc.provider.ServerManualExample

3. Run consumer: io.github.x_kill9.xrpc.consumer.ClientManualExample

### Manual API Example

```java
// Server side
public class ServerDemo {
    public static void main(String[] args) throws Exception {
        // 1. Create service instances
        CalculatorService calculator = new CalculatorImpl();
        HelloService helloService = new HelloServiceImpl();
        
        // 2. Register to container
        RpcContainer container = RpcContainer.getInstance();
        container.registerBean(CalculatorImpl.class, calculator, "calculator");
        container.registerBean(HelloServiceImpl.class, helloService, "helloService");
        
        // 3. Build service map
        Map<String, Object> serviceMap = new HashMap<>();
        serviceMap.put(CalculatorService.class.getName(), calculator);
        serviceMap.put(HelloService.class.getName(), helloService);
        
        // 4. Start Netty server
        NettyServer server = new NettyServer("127.0.0.1", 8081, serviceMap);
        server.start();
    }
}

// Client side
public class ClientDemo {
    public static void main(String[] args) throws Exception {
        // 1. Get transport type from configuration
        final String transport = ConfigFactory.getConfig().getClient().getTransport();
        
        // 2. Create RPC client via SPI
        RpcClient client = ExtensionLoader.getExtensionLoader(RpcClient.class)
                .getExtension(transport);
        
        // 3. Create JDK proxy factory
        final JdkProxyFactory proxyFactory = new JdkProxyFactory(client);
        
        // 4. Get service proxy
        final CalculatorService calculator = proxyFactory.getProxy(CalculatorService.class);
        
        // 5. Make RPC call
        int result = calculator.multiply(11, 20);
        logger.info("Result of 11 * 20 = {}", result);
    }
}
```

### Advanced Configuration Example

**Custom Load Balancer**: Implement the `LoadBalancer` interface to develop custom algorithms (e.g., consistent hashing), then register in `META-INF/xrpc/io.github.x_kill9.xrpc.core.loadbalance.LoadBalancer`:

```text
consistentHash=com.example.ConsistentHashLoadBalancer
```

Configuration usage:
```yaml
xrpc:
  client:
    loadBalancer: consistentHash
```

## 🛠️ Tech Stack

### Core Technologies

- **Java 17** - Modern Java features and performance
- **Netty 4.1.117.Final** - High-performance network application framework
- **Kryo 5.6.2** - Fast and efficient serialization library
- **ZooKeeper + Curator 5.9.0** - Distributed coordination and service discovery
- **SnakeYAML 1.33** - YAML configuration parsing
- **SLF4J + Logback** - Logging framework
- **Reflections 0.10.2** - Runtime classpath scanning

### Build Tools

- **Maven 3.6+** - Project build and dependency management
- **JUnit 5** - Testing framework

### Design Patterns

- **SPI (Service Provider Interface)** - Pluggable extension mechanism
- **Proxy Pattern** - JDK dynamic proxy for RPC calls
- **Interceptor Chain** - Chain of responsibility for service governance
- **Factory Pattern** - Component creation and configuration
- **Singleton Pattern** - RPC container and extension loader

## 🔮 Roadmap

### Version 1.1 (Planned)

- [ ] Support for multiple serialization formats (Protobuf, JSON)
- [ ] Additional load balancing algorithms (consistent hashing, least connections)
- [ ] Support for multiple registry centers (Nacos, Consul, Etcd)
- [ ] Connection pool support
- [ ] Async call support (CompletableFuture)

### Version 1.2 (Planned)

- [ ] HTTP/2 support
- [ ] gRPC compatibility layer
- [ ] Metrics and monitoring integration (Prometheus)
- [ ] Distributed tracing integration (OpenTelemetry)
- [ ] Security features (TLS/SSL, authentication)

### Future Enhancements

- [ ] IDL (Interface Definition Language) support
- [ ] Code generation tools
- [ ] Spring Boot Starter
- [ ] Docker and Kubernetes deployment examples
- [ ] Performance optimization (zero-copy, memory pool)

## 🤝 Contributing Guide

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Environment Setup

```bash
# Clone your fork
git clone https://github.com/x-kill9/xrpc-framework.git
cd xrpc-framework

# Install dependencies
mvn clean install

# Run tests
mvn test

# Build project
mvn clean package
```

### Code Standards

- Follow standard Java conventions
- Use meaningful variable and method names
- Add JavaDoc for public APIs
- Write unit tests for new features
- Maintain code coverage above 80%

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Inspired by [Dubbo](https://github.com/apache/dubbo) and [gRPC](https://github.com/grpc/grpc-java)
- Built with excellent open-source libraries from the Java ecosystem
- Thanks to all contributors and users

## 📞 Contact

- Project Link: [https://github.com/x-kill9/xrpc-framework](https://github.com/x-kill9/xrpc-framework)
- Issue Tracking: [GitHub Issues](https://github.com/x-kill9/xrpc-framework/issues)

---

<div align="center">

**⭐ If you find this project helpful, please give it a Star! ⭐**

</div>