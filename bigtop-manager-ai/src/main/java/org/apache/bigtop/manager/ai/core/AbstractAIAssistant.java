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
package org.apache.bigtop.manager.ai.core;

import org.apache.bigtop.manager.ai.core.config.AIAssistantConfig;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractAIAssistant implements AIAssistant {
    protected final AIAssistant.Service aiServices;
    protected static final Integer MEMORY_LEN = 10;
    protected final ChatMemory chatMemory;
    protected final Object memoryId;

    protected AbstractAIAssistant(Object memoryId, ChatMemory chatMemory, AIAssistant.Service aiServices) {
        this.memoryId = memoryId;
        this.chatMemory = chatMemory;
        this.aiServices = aiServices;
    }

    @Override
    public boolean test() {
        return ask("1+1=") != null;
    }

    @Override
    public Object getId() {
        return memoryId;
    }

    @Override
    public Flux<String> streamAsk(String chatMessage) {
        return aiServices.streamChat(chatMessage);
    }

    @Override
    public String ask(String chatMessage) {
        return aiServices.chat(chatMessage);
    }

    public abstract static class Builder implements AIAssistant.Builder {
        protected Object id;

        protected ChatMemory chatMemory;
        protected AIAssistantConfig config;

        protected String systemPrompt;

        protected io.modelcontextprotocol.client.McpAsyncClient mcpAsyncClient;
        protected List<io.modelcontextprotocol.client.McpAsyncClient> mcpAsyncClients = new ArrayList<>();
        protected Consumer<AIAssistant.ToolExecutionEvent> toolExecutionListener;

        private static final String AUTHORIZATION_HEADER = "Authorization";

        public Builder() {}

        public Builder withSystemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        @Override
        public Builder withToolExecutionListener(Consumer<AIAssistant.ToolExecutionEvent> toolExecutionListener) {
            this.toolExecutionListener = toolExecutionListener;
            return this;
        }

        public Builder withConfig(AIAssistantConfig config) {
            this.config = config;
            return this;
        }

        public Builder id(Object id) {
            this.id = id;
            return this;
        }

        public Builder memoryStore(ChatMemory chatMemory) {
            this.chatMemory = chatMemory;
            return this;
        }

        public Builder withMcpClient(io.modelcontextprotocol.client.McpAsyncClient mcpAsyncClient) {
            this.mcpAsyncClient = mcpAsyncClient;
            if (mcpAsyncClient != null) {
                this.mcpAsyncClients = List.of(mcpAsyncClient);
            }
            return this;
        }

        @Override
        public Builder withMcpClients(List<io.modelcontextprotocol.client.McpAsyncClient> mcpAsyncClients) {
            if (mcpAsyncClients == null || mcpAsyncClients.isEmpty()) {
                this.mcpAsyncClients = Collections.emptyList();
                this.mcpAsyncClient = null;
                return this;
            }

            this.mcpAsyncClients = List.copyOf(mcpAsyncClients);
            this.mcpAsyncClient = this.mcpAsyncClients.get(0);
            return this;
        }

        protected List<io.modelcontextprotocol.client.McpAsyncClient> getMcpAsyncClients() {
            if (mcpAsyncClients != null && !mcpAsyncClients.isEmpty()) {
                return mcpAsyncClients;
            }
            if (mcpAsyncClient != null) {
                return List.of(mcpAsyncClient);
            }
            return Collections.emptyList();
        }

        protected void emitToolExecutionEvent(String executionId, String toolName, String status, String payload) {
            if (toolExecutionListener != null) {
                toolExecutionListener.accept(
                        new AIAssistant.ToolExecutionEvent(executionId, toolName, status, payload));
            }
        }

        public ChatMemory getChatMemory() {
            if (chatMemory == null) {
                chatMemory = MessageWindowChatMemory.builder()
                        .chatMemoryRepository(new InMemoryChatMemoryRepository())
                        .build();
            }
            return chatMemory;
        }

        protected String resolveModelsBaseUrl() {
            return null;
        }

        protected String resolveModelsPath() {
            return "/v1/models";
        }

        protected String resolveApiKey(Map<String, String> credentials) {
            if (credentials == null) {
                return null;
            }
            String apiKey = credentials.get("apiKey");
            if (apiKey == null) {
                return null;
            }
            apiKey = apiKey.trim();
            if (apiKey.startsWith("Bearer ")) {
                apiKey = apiKey.substring("Bearer ".length()).trim();
            }
            return apiKey;
        }

        protected void applyModelRequestAuth(WebClient.RequestHeadersSpec<?> requestSpec, String apiKey) {
            if (apiKey != null && !apiKey.isBlank()) {
                requestSpec.header(AUTHORIZATION_HEADER, "Bearer " + apiKey);
            }
        }

        protected List<String> parseModelsResponse(JsonNode response) {
            if (response == null || !response.has("data")) {
                return Collections.emptyList();
            }
            List<String> models = new ArrayList<>();
            for (JsonNode node : response.get("data")) {
                JsonNode idNode = node.get("id");
                if (idNode != null && !idNode.isNull()) {
                    models.add(idNode.asText());
                }
            }
            return models;
        }

        @Override
        public List<String> getModels() {
            String baseUrl = resolveModelsBaseUrl();
            if (baseUrl == null || baseUrl.isBlank()) {
                return Collections.emptyList();
            }

            String path = resolveModelsPath();
            if (path == null || path.isBlank()) {
                path = "/v1/models";
            }

            Map<String, String> credentials = config == null ? Collections.emptyMap() : config.getCredentials();
            String apiKey = resolveApiKey(credentials);

            try {
                WebClient webClient =
                        WebClient.builder().baseUrl(baseUrl.trim()).build();
                WebClient.RequestHeadersSpec<?> requestSpec = webClient.get().uri(path);
                applyModelRequestAuth(requestSpec, apiKey);

                JsonNode response = requestSpec
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .timeout(Duration.ofSeconds(10))
                        .block();

                return parseModelsResponse(response);
            } catch (Exception ignored) {
                return Collections.emptyList();
            }
        }
    }
}
