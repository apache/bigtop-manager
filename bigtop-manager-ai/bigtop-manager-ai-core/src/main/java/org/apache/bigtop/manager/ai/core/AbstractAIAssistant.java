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

import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfig;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import reactor.core.publisher.Flux;

public abstract class AbstractAIAssistant implements AIAssistant {
    protected final AIAssistant.Service aiServices;
    protected static final Integer MEMORY_LEN = 10;
    protected final ChatMemory chatMemory;

    protected AbstractAIAssistant(ChatMemory chatMemory, AIAssistant.Service aiServices) {
        this.chatMemory = chatMemory;
        this.aiServices = aiServices;
    }

    @Override
    public boolean test() {
        return ask("1+1=") != null;
    }

    @Override
    public Object getId() {
        return chatMemory.id();
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

        protected ChatMemoryStore chatMemoryStore;
        protected AIAssistantConfig configProvider;

        protected ToolProvider toolProvider;
        protected String systemPrompt;

        public Builder() {}

        public Builder withToolProvider(ToolProvider toolProvider) {
            this.toolProvider = toolProvider;
            return this;
        }

        public Builder withSystemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        public Builder withConfigProvider(AIAssistantConfig configProvider) {
            this.configProvider = configProvider;
            return this;
        }

        public Builder id(Object id) {
            this.id = id;
            return this;
        }

        public Builder memoryStore(ChatMemoryStore chatMemoryStore) {
            this.chatMemoryStore = chatMemoryStore;
            return this;
        }

        public MessageWindowChatMemory getChatMemory() {
            MessageWindowChatMemory.Builder builder = MessageWindowChatMemory.builder()
                    .chatMemoryStore(chatMemoryStore)
                    .maxMessages(MEMORY_LEN);
            if (id != null) {
                builder.id(id);
            }
            return builder.build();
        }
    }
}
