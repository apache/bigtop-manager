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
package org.apache.bigtop.manager.ai.assistant;

import org.apache.bigtop.manager.ai.assistant.provider.LocSystemPromptProvider;
import org.apache.bigtop.manager.ai.core.AIAssistantAbstractFactory;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.factory.ToolBox;
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;
import org.apache.bigtop.manager.ai.core.provider.SystemPromptProvider;
import org.apache.bigtop.manager.ai.openai.OpenAIAssistant;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;

import java.util.Objects;

public class GeneralAssistantFactory extends AIAssistantAbstractFactory {

    private SystemPromptProvider systemPromptProvider = new LocSystemPromptProvider();
    private ChatMemoryStore chatMemoryStore = new InMemoryChatMemoryStore();

    public GeneralAssistantFactory() {}

    public GeneralAssistantFactory(SystemPromptProvider systemPromptProvider) {
        this.systemPromptProvider = systemPromptProvider;
    }

    public GeneralAssistantFactory(SystemPromptProvider systemPromptProvider, ChatMemoryStore chatMemoryStore) {
        this.systemPromptProvider = systemPromptProvider;
        this.chatMemoryStore = chatMemoryStore;
    }

    @Override
    public AIAssistant createWithPrompt(
            PlatformType platformType, AIAssistantConfigProvider assistantConfig, Object id, Object promptId) {
        AIAssistant aiAssistant = create(platformType, assistantConfig, id);
        SystemMessage systemPrompt = systemPromptProvider.getSystemPrompt(promptId);
        aiAssistant.setSystemPrompt(systemPrompt);
        return aiAssistant;
    }

    @Override
    public AIAssistant create(PlatformType platformType, AIAssistantConfigProvider assistantConfig, Object id) {
        if (Objects.requireNonNull(platformType) == PlatformType.OPENAI) {
            AIAssistant aiAssistant = OpenAIAssistant.builder()
                    .id(id)
                    .memoryStore(chatMemoryStore)
                    .withConfigProvider(assistantConfig)
                    .build();
            aiAssistant.setSystemPrompt(systemPromptProvider.getSystemPrompt());
            return aiAssistant;
        }
        return null;
    }

    @Override
    public ToolBox createToolBox(PlatformType platformType) {
        return null;
    }
}
