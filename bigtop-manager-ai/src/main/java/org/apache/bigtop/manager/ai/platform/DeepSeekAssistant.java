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

package org.apache.bigtop.manager.ai.platform;

import org.apache.bigtop.manager.ai.core.AbstractAIAssistant;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.util.Assert;

import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeepSeekAssistant extends AbstractAIAssistant {

    private static final String BASE_URL_ENV_KEY = "BIGTOP_MANAGER_AI_DEEPSEEK_BASE_URL";
    private static final String BASE_URL = "https://api.deepseek.com";

    public DeepSeekAssistant(Object memoryId, ChatMemory chatMemory, AIAssistant.Service aiServices) {
        super(memoryId, chatMemory, aiServices);
    }

    @Override
    public PlatformType getPlatform() {
        return PlatformType.DEEPSEEK;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractAIAssistant.Builder {

        @Override
        protected String resolveModelsBaseUrl() {
            return resolveDefaultBaseUrl();
        }

        private String resolveDefaultBaseUrl() {
            String envBaseUrl = System.getenv(BASE_URL_ENV_KEY);
            if (envBaseUrl != null && !envBaseUrl.isBlank()) {
                return envBaseUrl;
            }
            return BASE_URL;
        }

        @Override
        public ChatModel getChatModel() {
            String model = config.getModel();
            Assert.notNull(model, "model must not be null");
            String apiKey = config.getCredentials().get("apiKey");
            Assert.notNull(apiKey, "apiKey must not be null");

            DeepSeekApi deepSeekApi = DeepSeekApi.builder()
                    .baseUrl(resolveDefaultBaseUrl())
                    .apiKey(apiKey)
                    .build();
            DeepSeekChatOptions.Builder optionsBuilder =
                    DeepSeekChatOptions.builder().model(model);
            List<io.modelcontextprotocol.client.McpAsyncClient> mcpClients = getMcpAsyncClients();
            if (!mcpClients.isEmpty()) {
                optionsBuilder.toolCallbacks(buildObservedToolCallbacks(mcpClients));
            }
            DeepSeekChatOptions options = optionsBuilder.build();
            return DeepSeekChatModel.builder()
                    .deepSeekApi(deepSeekApi)
                    .defaultOptions(options)
                    .build();
        }

        @Override
        public StreamingChatModel getStreamingChatModel() {
            // DeepSeekChatModel handles both sync and streaming
            return getChatModel();
        }

        private ToolCallback[] buildObservedToolCallbacks(
                List<io.modelcontextprotocol.client.McpAsyncClient> mcpClients) {
            ToolCallback[] callbacks = new AsyncMcpToolCallbackProvider(mcpClients).getToolCallbacks();
            ToolCallback[] observedCallbacks = new ToolCallback[callbacks.length];
            for (int i = 0; i < callbacks.length; i++) {
                observedCallbacks[i] = wrapToolCallback(callbacks[i]);
            }
            return observedCallbacks;
        }

        private ToolCallback wrapToolCallback(ToolCallback delegate) {
            return new ToolCallback() {
                @Override
                public ToolDefinition getToolDefinition() {
                    return delegate.getToolDefinition();
                }

                @Override
                public String call(String toolInput) {
                    return call(toolInput, null);
                }

                @Override
                public String call(String toolInput, org.springframework.ai.chat.model.ToolContext toolContext) {
                    String toolName = getToolDefinition().name();
                    String executionId = UUID.randomUUID().toString();
                    emitToolExecutionEvent(executionId, toolName, "started", toolInput);
                    try {
                        String result = delegate.call(toolInput, toolContext);
                        emitToolExecutionEvent(executionId, toolName, "completed", result);
                        return result;
                    } catch (Exception e) {
                        emitToolExecutionEvent(executionId, toolName, "failed", e.getMessage());
                        throw e;
                    }
                }
            };
        }

        public AIAssistant build() {
            ChatModel chatModel = getChatModel();
            StreamingChatModel streamingChatModel = getStreamingChatModel();
            ChatMemory memory = getChatMemory();

            AIAssistant.Service aiService = new AIAssistant.Service() {
                @Override
                public String chat(String userMessage) {
                    List<Message> messages = new ArrayList<>();
                    if (systemPrompt != null) {
                        messages.add(new SystemMessage(systemPrompt));
                    }
                    // Add conversation history
                    String convId = String.valueOf(id);
                    List<Message> history = memory.get(convId);
                    messages.addAll(history);
                    // Add new user message
                    UserMessage newUserMessage = new UserMessage(userMessage);
                    messages.add(newUserMessage);

                    Prompt prompt = new Prompt(messages);
                    String response =
                            chatModel.call(prompt).getResult().getOutput().getText();

                    // Save to memory
                    memory.add(
                            convId,
                            List.of(
                                    newUserMessage,
                                    new org.springframework.ai.chat.messages.AssistantMessage(response)));

                    return response;
                }

                @Override
                public Flux<String> streamChat(String userMessage) {
                    List<Message> messages = new ArrayList<>();
                    if (systemPrompt != null) {
                        messages.add(new SystemMessage(systemPrompt));
                    }
                    // Add conversation history
                    String convId = String.valueOf(id);
                    List<Message> history = memory.get(convId);
                    messages.addAll(history);
                    // Add new user message
                    UserMessage newUserMessage = new UserMessage(userMessage);
                    messages.add(newUserMessage);

                    Prompt prompt = new Prompt(messages);

                    StringBuilder responseBuilder = new StringBuilder();
                    return streamingChatModel.stream(prompt)
                            .concatMap(chatResponse -> {
                                String content = null;
                                if (chatResponse.getResult() != null
                                        && chatResponse.getResult().getOutput() != null) {
                                    content =
                                            chatResponse.getResult().getOutput().getText();
                                }
                                if (content != null && !content.isEmpty()) {
                                    responseBuilder.append(content);
                                    return Flux.just(content);
                                }
                                return Flux.empty();
                            })
                            .doOnComplete(() -> {
                                // Save to memory when streaming completes
                                memory.add(
                                        convId,
                                        List.of(
                                                newUserMessage,
                                                new org.springframework.ai.chat.messages.AssistantMessage(
                                                        responseBuilder.toString())));
                            });
                }
            };

            return new DeepSeekAssistant(id, memory, aiService);
        }
    }
}
