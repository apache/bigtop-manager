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

import reactor.core.publisher.Flux;

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

        public Builder() {}

        public Builder withSystemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
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

        public ChatMemory getChatMemory() {
            if (chatMemory == null) {
                chatMemory = MessageWindowChatMemory.builder()
                        .chatMemoryRepository(new InMemoryChatMemoryRepository())
                        .build();
            }
            return chatMemory;
        }
    }
}
