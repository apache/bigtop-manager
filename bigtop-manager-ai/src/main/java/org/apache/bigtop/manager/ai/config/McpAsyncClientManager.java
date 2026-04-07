/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.bigtop.manager.ai.config;

import org.springframework.ai.mcp.client.common.autoconfigure.properties.McpSseClientProperties;
import org.springframework.ai.mcp.client.common.autoconfigure.properties.McpStdioClientProperties;
import org.springframework.ai.mcp.client.common.autoconfigure.properties.McpStreamableHttpClientProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.client.transport.WebClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class McpAsyncClientManager {

    @Value("${spring.ai.mcp.client.enabled:false}")
    private boolean enabled;

    @Value("${spring.ai.mcp.client.request-timeout-seconds:120}")
    private long requestTimeoutSeconds;

    @Value("${spring.ai.mcp.client.init-timeout-seconds:10}")
    private long initTimeoutSeconds;

    @Value("${spring.ai.mcp.client.connections:}")
    private String connections;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final McpJsonMapper mcpJsonMapper = new JacksonMcpJsonMapper(objectMapper);

    private final ObjectProvider<McpSseClientProperties> sseClientPropertiesProvider;
    private final ObjectProvider<McpStreamableHttpClientProperties> streamableHttpClientPropertiesProvider;
    private final ObjectProvider<McpStdioClientProperties> stdioClientPropertiesProvider;

    private List<McpAsyncClient> clients = Collections.emptyList();
    private boolean initialized = false;
    private boolean disabledLogged = false;

    public McpAsyncClientManager(
            ObjectProvider<McpSseClientProperties> sseClientPropertiesProvider,
            ObjectProvider<McpStreamableHttpClientProperties> streamableHttpClientPropertiesProvider,
            ObjectProvider<McpStdioClientProperties> stdioClientPropertiesProvider) {
        this.sseClientPropertiesProvider = sseClientPropertiesProvider;
        this.streamableHttpClientPropertiesProvider = streamableHttpClientPropertiesProvider;
        this.stdioClientPropertiesProvider = stdioClientPropertiesProvider;
    }

    public synchronized McpAsyncClient getClient() {
        List<McpAsyncClient> allClients = getClients();
        if (allClients.isEmpty()) {
            return null;
        }
        return allClients.get(0);
    }

    public synchronized List<McpAsyncClient> getClients() {
        if (!enabled) {
            if (!disabledLogged) {
                log.info("MCP Async Client is disabled by config (spring.ai.mcp.client.enabled=false)");
                disabledLogged = true;
            }
            return Collections.emptyList();
        }

        if (!initialized) {
            initialized = true;
            clients = initializeClients();
            log.info("MCP Async Clients initialized: {}", clients.size());
        }

        return clients;
    }

    private List<McpAsyncClient> initializeClients() {
        List<McpConnection> parsedConnections = parseConnections();

        if (parsedConnections.isEmpty()) {
            parsedConnections.addAll(fromSpringMcpProperties());
        }

        if (parsedConnections.isEmpty()) {
            log.warn(
                    "No MCP connections configured. Please use spring.ai.mcp.client.connections or spring.ai.mcp.client.<type>.connections.");
            return Collections.emptyList();
        }

        List<McpAsyncClient> initializedClients = new ArrayList<>();
        for (McpConnection connection : parsedConnections) {
            McpAsyncClient client = initializeClient(connection);
            if (client != null) {
                initializedClients.add(client);
            }
        }
        return initializedClients;
    }

    private List<McpConnection> fromSpringMcpProperties() {
        List<McpConnection> conns = new ArrayList<>();

        McpSseClientProperties sseProperties = sseClientPropertiesProvider.getIfAvailable();
        if (sseProperties != null && sseProperties.getConnections() != null) {
            sseProperties.getConnections().forEach((name, params) -> {
                if (params != null) {
                    conns.add(McpConnection.sse(name, params.url(), params.sseEndpoint()));
                }
            });
        }

        McpStreamableHttpClientProperties streamableHttpProperties =
                streamableHttpClientPropertiesProvider.getIfAvailable();
        if (streamableHttpProperties != null && streamableHttpProperties.getConnections() != null) {
            streamableHttpProperties.getConnections().forEach((name, params) -> {
                if (params != null) {
                    conns.add(McpConnection.streamableHttp(name, params.url(), params.endpoint()));
                }
            });
        }

        McpStdioClientProperties stdioProperties = stdioClientPropertiesProvider.getIfAvailable();
        if (stdioProperties != null && stdioProperties.getConnections() != null) {
            stdioProperties.getConnections().forEach((name, params) -> {
                if (params != null) {
                    conns.add(McpConnection.local(name, params.command(), params.args(), params.env()));
                }
            });
        }

        return conns;
    }

    private McpAsyncClient initializeClient(McpConnection connection) {
        try {
            McpClientTransport transport = buildTransport(connection);
            if (transport == null) {
                log.warn("Skip MCP connection {} due to unsupported type {}", connection.name, connection.type);
                return null;
            }

            Duration requestTimeout = Duration.ofSeconds(Math.max(
                    connection.requestTimeoutSeconds > 0 ? connection.requestTimeoutSeconds : requestTimeoutSeconds,
                    1));
            Duration initTimeout = Duration.ofSeconds(Math.max(
                    connection.initTimeoutSeconds > 0 ? connection.initTimeoutSeconds : initTimeoutSeconds, 1));

            McpAsyncClient client =
                    McpClient.async(transport).requestTimeout(requestTimeout).build();

            client.initialize().block(initTimeout);
            client.ping().block(initTimeout);

            log.info(
                    "MCP Async Client [{}] initialized, type={}, baseUrl={}, endpoint={}, requestTimeout={}, initTimeout={}",
                    connection.name,
                    connection.type,
                    connection.baseUrl,
                    connection.endpoint,
                    requestTimeout,
                    initTimeout);
            logRegisteredTools(connection.name, client, initTimeout);
            return client;
        } catch (Exception e) {
            log.warn("Failed to initialize MCP client [{}]: {}", connection.name, e.getMessage());
            return null;
        }
    }

    private McpClientTransport buildTransport(McpConnection connection) {
        String type = connection.type.toLowerCase();
        return switch (type) {
            case "sse" -> buildWebFluxSseTransport(connection);
            case "streamable-http", "http", "http-streamable" -> buildWebFluxStreamableHttpTransport(connection);
            case "local", "stdio" -> buildStdioTransport(connection);
            default -> null;
        };
    }

    private McpClientTransport buildWebFluxSseTransport(McpConnection connection) {
        String baseUrl = connection.baseUrl;
        if (!StringUtils.hasText(baseUrl)) {
            log.warn("MCP SSE connection {} missing baseUrl", connection.name);
            return null;
        }
        String endpoint = StringUtils.hasText(connection.endpoint) ? connection.endpoint : "/mcp/sse";
        WebClient.Builder webClientBuilder = WebClient.builder().baseUrl(baseUrl);
        return WebFluxSseClientTransport.builder(webClientBuilder)
                .jsonMapper(mcpJsonMapper)
                .sseEndpoint(endpoint)
                .build();
    }

    private McpClientTransport buildWebFluxStreamableHttpTransport(McpConnection connection) {
        String baseUrl = connection.baseUrl;
        if (!StringUtils.hasText(baseUrl)) {
            log.warn("MCP streamable-http connection {} missing baseUrl", connection.name);
            return null;
        }
        String endpoint = StringUtils.hasText(connection.endpoint) ? connection.endpoint : "/mcp";
        WebClient.Builder webClientBuilder = WebClient.builder().baseUrl(baseUrl);
        return WebClientStreamableHttpTransport.builder(webClientBuilder)
                .jsonMapper(mcpJsonMapper)
                .endpoint(endpoint)
                .openConnectionOnStartup(true)
                .build();
    }

    private McpClientTransport buildStdioTransport(McpConnection connection) {
        if (!StringUtils.hasText(connection.command)) {
            log.warn("MCP stdio/local connection {} missing command", connection.name);
            return null;
        }

        ServerParameters.Builder serverBuilder = ServerParameters.builder(connection.command);
        if (!connection.args.isEmpty()) {
            serverBuilder.args(connection.args);
        }
        if (!connection.env.isEmpty()) {
            serverBuilder.env(connection.env);
        }

        StdioClientTransport stdioTransport = new StdioClientTransport(serverBuilder.build(), mcpJsonMapper);
        stdioTransport.setStdErrorHandler(line -> log.warn("MCP stdio [{}] stderr: {}", connection.name, line));
        return stdioTransport;
    }

    private void logRegisteredTools(String connectionName, McpAsyncClient client, Duration initTimeout) {
        try {
            List<String> toolNames = new ArrayList<>();
            String cursor = null;
            while (true) {
                McpSchema.ListToolsResult listToolsResult = cursor == null || cursor.isBlank()
                        ? client.listTools().block(initTimeout)
                        : client.listTools(cursor).block(initTimeout);
                if (listToolsResult == null
                        || listToolsResult.tools() == null
                        || listToolsResult.tools().isEmpty()) {
                    break;
                }

                for (McpSchema.Tool tool : listToolsResult.tools()) {
                    if (tool != null && tool.name() != null) {
                        toolNames.add(tool.name());
                    }
                }

                String nextCursor = listToolsResult.nextCursor();
                if (!StringUtils.hasText(nextCursor) || nextCursor.equals(cursor)) {
                    break;
                }
                cursor = nextCursor;
            }

            log.info("MCP tools discovered for [{}]: count={}, names={}", connectionName, toolNames.size(), toolNames);
        } catch (Exception e) {
            log.warn("Failed to list MCP tools for [{}]: {}", connectionName, e.getMessage());
        }
    }

    private List<McpConnection> parseConnections() {
        if (!StringUtils.hasText(connections)) {
            return new ArrayList<>();
        }

        List<McpConnection> parsed = new ArrayList<>();
        String[] segments = connections.split(";");
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i].trim();
            if (!StringUtils.hasText(segment)) {
                continue;
            }

            Map<String, String> kv = new HashMap<>();
            String[] pairs = segment.split(",");
            for (String pair : pairs) {
                String[] items = pair.split("=", 2);
                if (items.length != 2) {
                    continue;
                }
                kv.put(items[0].trim().toLowerCase(), items[1].trim());
            }

            String name = valueOrDefault(kv.get("name"), "conn-" + (i + 1));
            String type = valueOrDefault(kv.get("type"), "sse");
            String baseUrl = kv.get("baseurl");
            String endpoint = kv.get("endpoint");
            String command = kv.get("command");
            List<String> args = parseList(kv.get("args"));
            Map<String, String> env = parseEnv(kv.get("env"));
            long connectionRequestTimeout = parseLongOrDefault(kv.get("requesttimeoutseconds"), -1);
            long connectionInitTimeout = parseLongOrDefault(kv.get("inittimeoutseconds"), -1);

            parsed.add(new McpConnection(
                    name,
                    type,
                    baseUrl,
                    endpoint,
                    command,
                    args,
                    env,
                    connectionRequestTimeout,
                    connectionInitTimeout));
        }

        return parsed;
    }

    private static long parseLongOrDefault(String raw, long defaultValue) {
        if (!StringUtils.hasText(raw)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static List<String> parseList(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>();
        for (String item : raw.split("\\|")) {
            String trimmed = item.trim();
            if (StringUtils.hasText(trimmed)) {
                list.add(trimmed);
            }
        }
        return list;
    }

    private static Map<String, String> parseEnv(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Collections.emptyMap();
        }
        Map<String, String> env = new HashMap<>();
        for (String item : raw.split("\\|")) {
            String[] pair = item.split(":", 2);
            if (pair.length == 2 && StringUtils.hasText(pair[0])) {
                env.put(pair[0].trim(), pair[1].trim());
            }
        }
        return env;
    }

    private static String valueOrDefault(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private static class McpConnection {
        private final String name;
        private final String type;
        private final String baseUrl;
        private final String endpoint;
        private final String command;
        private final List<String> args;
        private final Map<String, String> env;
        private final long requestTimeoutSeconds;
        private final long initTimeoutSeconds;

        private McpConnection(
                String name,
                String type,
                String baseUrl,
                String endpoint,
                String command,
                List<String> args,
                Map<String, String> env,
                long requestTimeoutSeconds,
                long initTimeoutSeconds) {
            this.name = name;
            this.type = type;
            this.baseUrl = baseUrl;
            this.endpoint = endpoint;
            this.command = command;
            this.args = args == null ? Collections.emptyList() : args;
            this.env = env == null ? Collections.emptyMap() : env;
            this.requestTimeoutSeconds = requestTimeoutSeconds;
            this.initTimeoutSeconds = initTimeoutSeconds;
        }

        private static McpConnection sse(String name, String baseUrl, String endpoint) {
            return new McpConnection(
                    name, "sse", baseUrl, endpoint, null, Collections.emptyList(), Collections.emptyMap(), -1, -1);
        }

        private static McpConnection streamableHttp(String name, String baseUrl, String endpoint) {
            return new McpConnection(
                    name,
                    "streamable-http",
                    baseUrl,
                    endpoint,
                    null,
                    Collections.emptyList(),
                    Collections.emptyMap(),
                    -1,
                    -1);
        }

        private static McpConnection local(String name, String command, List<String> args, Map<String, String> env) {
            return new McpConnection(name, "local", null, null, command, args, env, -1, -1);
        }
    }
}
