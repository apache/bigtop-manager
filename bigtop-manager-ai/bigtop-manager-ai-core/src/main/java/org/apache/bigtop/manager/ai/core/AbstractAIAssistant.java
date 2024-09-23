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
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

public abstract class AbstractAIAssistant implements AIAssistant {

    protected static final Integer MEMORY_LEN = 10;
    protected static final Integer THREAD_NAME_LEN = 100;
    protected final ChatMemory chatMemory;
    private String threadNameGenerator;

    protected AbstractAIAssistant(ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
    }

    @Override
    public String ask(String chatMessage) {
        chatMemory.add(UserMessage.from(chatMessage));
        String aiMessage = runAsk(chatMessage);
        chatMemory.add(AiMessage.from(aiMessage));
        return aiMessage;
    }

    @Override
    public boolean test() {
        return runAsk("1+1=") != null;
    }

    @Override
    public String getThreadName() {
        if (threadNameGenerator == null) {
            return null;
        }
        boolean hasUserMessage = false;
        for (ChatMessage message : chatMemory.messages()) {
            if (message instanceof UserMessage) {
                hasUserMessage = true;
                break;
            }
        }
        if (!hasUserMessage) {
            return null;
        }
        String threadName = runAsk(threadNameGenerator);
        return threadName.length() > THREAD_NAME_LEN ? threadName.substring(0, THREAD_NAME_LEN) : threadName;
    }

    @Override
    public Object getId() {
        return chatMemory.id();
    }

    @Override
    public void setThreadNameGenerator(String threadNameGenerator) {
        this.threadNameGenerator = threadNameGenerator;
    }

    public abstract static class Builder implements AIAssistant.Builder {
        protected Object id;

        protected ChatMemoryStore chatMemoryStore;
        protected AIAssistantConfigProvider configProvider;

        public Builder() {}

        public Builder withConfigProvider(AIAssistantConfigProvider configProvider) {
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
    }
}
