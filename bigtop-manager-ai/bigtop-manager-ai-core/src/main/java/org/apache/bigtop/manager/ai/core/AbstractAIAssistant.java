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

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

public abstract class AbstractAIAssistant implements AIAssistant {

    protected static final Integer MEMORY_LEN = 10;
    protected final ChatMemory chatMemory;

    protected AbstractAIAssistant(ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
    }

    @Override
    public boolean test() {
        return ask("1+1=") != null;
    }

    @Override
    public Object getId() {
        return chatMemory.id();
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
