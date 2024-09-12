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
import org.apache.bigtop.manager.ai.core.AbstractAIAssistantFactory;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.enums.SystemPrompt;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.factory.ToolBox;
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;
import org.apache.bigtop.manager.ai.core.provider.SystemPromptProvider;
import org.apache.bigtop.manager.ai.dashscope.DashScopeAssistant;
import org.apache.bigtop.manager.ai.openai.OpenAIAssistant;

import org.apache.commons.lang3.NotImplementedException;

import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;

public class GeneralAssistantFactory extends AbstractAIAssistantFactory {

    private final SystemPromptProvider systemPromptProvider;
    private final ChatMemoryStore chatMemoryStore;

    public GeneralAssistantFactory() {
        this(new LocSystemPromptProvider(), new InMemoryChatMemoryStore());
    }

    public GeneralAssistantFactory(SystemPromptProvider systemPromptProvider) {
        this(systemPromptProvider, new InMemoryChatMemoryStore());
    }

    public GeneralAssistantFactory(ChatMemoryStore chatMemoryStore) {
        this(new LocSystemPromptProvider(), chatMemoryStore);
    }

    public GeneralAssistantFactory(SystemPromptProvider systemPromptProvider, ChatMemoryStore chatMemoryStore) {
        this.systemPromptProvider = systemPromptProvider;
        this.chatMemoryStore = chatMemoryStore;
    }

    @Override
    public AIAssistant createWithPrompt(
            PlatformType platformType,
            AIAssistantConfigProvider assistantConfig,
            Object id,
            SystemPrompt systemPrompts) {
        AIAssistant.Builder builder =
                switch (platformType) {
                    case OPENAI -> OpenAIAssistant.builder();
                    case DASH_SCOPE -> DashScopeAssistant.builder();
                };

        AIAssistant aiAssistant = builder.id(id)
                .memoryStore((id == null) ? new InMemoryChatMemoryStore() : chatMemoryStore)
                .withConfigProvider(assistantConfig)
                .build();

        String systemPrompt = systemPromptProvider.getSystemMessage(systemPrompts);
        aiAssistant.setSystemPrompt(systemPrompt);
        String locale = assistantConfig.getLanguage();
        if (locale != null) {
            aiAssistant.setSystemPrompt(systemPromptProvider.getLanguagePrompt(locale));
        }
        return aiAssistant;
    }

    @Override
    public AIAssistant create(PlatformType platformType, AIAssistantConfigProvider assistantConfig, Object id) {
        return createWithPrompt(platformType, assistantConfig, id, SystemPrompt.DEFAULT_PROMPT);
    }

    @Override
    public ToolBox createToolBox(PlatformType platformType) {
        throw new NotImplementedException("ToolBox is not implemented for GeneralAssistantFactory");
    }
}
