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
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.util.Assert;

import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

public class DeepSeekAssistant extends AbstractAIAssistant {

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
        public ChatModel getChatModel() {
            String model = config.getModel();
            Assert.notNull(model, "model must not be null");
            String apiKey = config.getCredentials().get("apiKey");
            Assert.notNull(apiKey, "apiKey must not be null");

            OpenAiApi openAiApi =
                    OpenAiApi.builder().baseUrl(BASE_URL).apiKey(apiKey).build();
            OpenAiChatOptions options = OpenAiChatOptions.builder().model(model).build();
            return OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .defaultOptions(options)
                    .build();
        }

        @Override
        public StreamingChatModel getStreamingChatModel() {
            // In Spring AI, OpenAiChatModel handles both sync and streaming
            return getChatModel();
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
                            .map(chatResponse -> {
                                String content =
                                        chatResponse.getResult().getOutput().getText();
                                if (content != null) {
                                    responseBuilder.append(content);
                                }
                                return content;
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
