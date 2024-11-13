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
import org.apache.bigtop.manager.ai.assistant.store.ChatMemoryStoreProvider;
import org.apache.bigtop.manager.ai.core.AbstractAIAssistantFactory;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.enums.SystemPrompt;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;
import org.apache.bigtop.manager.ai.core.provider.SystemPromptProvider;
import org.apache.bigtop.manager.ai.dashscope.DashScopeAssistant;
import org.apache.bigtop.manager.ai.openai.OpenAIAssistant;
import org.apache.bigtop.manager.ai.qianfan.QianFanAssistant;

import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;

public class GeneralAssistantFactory extends AbstractAIAssistantFactory {

    private final SystemPromptProvider systemPromptProvider;
    private final ChatMemoryStoreProvider chatMemoryStoreProvider;

    public GeneralAssistantFactory(ChatMemoryStoreProvider chatMemoryStoreProvider) {
        this(new LocSystemPromptProvider(), chatMemoryStoreProvider);
    }

    public GeneralAssistantFactory() {
        this(new LocSystemPromptProvider(), new ChatMemoryStoreProvider(null, null));
    }

    public GeneralAssistantFactory(
            SystemPromptProvider systemPromptProvider, ChatMemoryStoreProvider chatMemoryStoreProvider) {
        this.systemPromptProvider = systemPromptProvider;
        this.chatMemoryStoreProvider = chatMemoryStoreProvider;
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
                    case QIANFAN -> QianFanAssistant.builder();
                };
        AIAssistant aiAssistant = builder.id(id)
                .memoryStore(
                        (id == null)
                                ? new InMemoryChatMemoryStore()
                                : chatMemoryStoreProvider.createPersistentChatMemoryStore())
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
    public AIAssistant createWithTools(
            PlatformType platformType, AIAssistantConfigProvider assistantConfig, Long id, ToolProvider toolProvider) {
        AIAssistant.Builder builder =
                switch (platformType) {
                    case OPENAI -> OpenAIAssistant.builder();
                    case DASH_SCOPE -> DashScopeAssistant.builder();
                    case QIANFAN -> QianFanAssistant.builder();
                };
        builder = builder.id(id)
                .memoryStore(
                        (id == null)
                                ? new InMemoryChatMemoryStore()
                                : chatMemoryStoreProvider.createAiServiceChatMemoryStore())
                .withConfigProvider(assistantConfig);
        /// TODO: Only a portion of the models of DashScope support the API of OpenAI

        return AiServices.builder(AIAssistant.class)
                .chatLanguageModel(builder.getChatLanguageModel())
                .streamingChatLanguageModel(builder.getStreamingChatLanguageModel())
                .chatMemory(builder.getChatMemory())
                .toolProvider(toolProvider)
                .build();
    }
}
