/*
 * Copyright (c) 2026 x-kill9. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package io.github.x_kill9.xrpc.config;

import io.github.x_kill9.xrpc.config.exception.ConfigurationException;
import io.github.x_kill9.xrpc.core.config.loader.ConfigLoader;
import io.github.x_kill9.xrpc.core.config.model.ClientConfig;
import io.github.x_kill9.xrpc.core.config.model.RegistryConfig;
import io.github.x_kill9.xrpc.core.config.model.ServerConfig;
import io.github.x_kill9.xrpc.core.config.model.XrpcConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

/**
 * YAML implementation of {@link ConfigLoader}.
 * Loads XRPC configuration from {@code xrpc.yaml} in the classpath.
 *
 * @author x-kill9
 */
public class YamlConfigLoader implements ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(YamlConfigLoader.class);

    @Override
    public XrpcConfig load() {
        logger.debug("Loading configuration from xrpc.yaml");
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("xrpc.yaml")) {
            if (in == null) {
                logger.error("xrpc.yaml not found in classpath");
                throw new ConfigurationException("xrpc.yaml not found in classpath");
            }
            logger.debug("xrpc.yaml found in classpath");

            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(in);
            Object xrpcNode = root.get("xrpc");
            if (xrpcNode == null) {
                logger.error("Missing 'xrpc' root in configuration");
                throw new ConfigurationException("Missing 'xrpc' root in configuration");
            }
            logger.debug("Found 'xrpc' root element");

            // Convert the xrpc node back to YAML string and then to XrpcConfig object
            String xrpcYaml = yaml.dump(xrpcNode);
            XrpcConfig config = yaml.loadAs(xrpcYaml, XrpcConfig.class);
            logger.debug("Parsed XrpcConfig from YAML");
            logger.info("Configuration loaded successfully");

            return validateConfig(config);
        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to load xrpc.yaml", e);
            throw new ConfigurationException("Failed to load xrpc.yaml", e);
        }
    }

    private XrpcConfig validateConfig(XrpcConfig config) {
        logger.debug("Starting configuration validation");
        validateRegistryConfig(config.getRegistry());
        validateClientConfig(config.getClient());
        validateServerConfig(config.getServer());
        logger.debug("Configuration validation completed");
        return config;
    }

    private void validateRegistryConfig(RegistryConfig registry) {
        logger.debug("Validating registry config: {}", registry);

        if (registry.getAddress() == null || registry.getAddress().trim().isEmpty()) {
            throw new ConfigurationException("Registry address must not be empty");
        }
        if (!registry.getAddress().matches("^[^:]+:\\d+$")) {
            throw new ConfigurationException("Registry address format must be host:port, but got: " + registry.getAddress());
        }
        logger.debug("Registry address validated: {}", registry.getAddress());

        if (registry.getType() == null || registry.getType().trim().isEmpty()) {
            throw new ConfigurationException("Registry type must not be empty");
        }
        logger.debug("Registry type validated: {}", registry.getType());

        logger.info("Registry config validated - type: {}, address: {}", registry.getType(), registry.getAddress());
    }

    private void validateClientConfig(ClientConfig client) {
        logger.debug("Validating client config: {}", client);

        if (client.getConnectTimeout() <= 0) {
            throw new ConfigurationException("Client connectTimeout must be positive, but got: " + client.getConnectTimeout());
        }
        if (client.getCallTimeout() <= 0) {
            throw new ConfigurationException("Client callTimeout must be positive, but got: " + client.getCallTimeout());
        }
        if (client.getHeartbeatIntervalSeconds() <= 0) {
            throw new ConfigurationException("Client heartbeatIntervalSeconds must be positive, but got: " + client.getHeartbeatIntervalSeconds());
        }
        if (client.getSerializer() == null || client.getSerializer().trim().isEmpty()) {
            throw new ConfigurationException("Client serializer must not be empty");
        }
        if (client.getLoadBalancer() == null || client.getLoadBalancer().trim().isEmpty()) {
            throw new ConfigurationException("Client loadBalancer must not be empty");
        }

        logger.info("Client config validated - serializer: {}, loadBalancer: {}",
                client.getSerializer(), client.getLoadBalancer());
    }

    private void validateServerConfig(ServerConfig server) {
        logger.debug("Validating server config: {}", server);

        int port = server.getPort();
        if (port <= 0 || port > 65535) {
            throw new ConfigurationException("Server port must be between 1 and 65535, but got: " + port);
        }

        logger.info("Server config validated - port: {}", port);
    }
}