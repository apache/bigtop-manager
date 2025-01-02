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

import org.apache.bigtop.manager.ai.assistant.config.GeneralAssistantConfig;
import org.apache.bigtop.manager.ai.assistant.provider.ChatMemoryStoreProvider;
import org.apache.bigtop.manager.ai.assistant.provider.LocSystemPromptProvider;
import org.apache.bigtop.manager.ai.core.AbstractAIAssistantFactory;
import org.apache.bigtop.manager.ai.core.config.AIAssistantConfig;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.enums.SystemPrompt;
import org.apache.bigtop.manager.ai.core.exception.AssistantConfigNotSetException;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.provider.SystemPromptProvider;
import org.apache.bigtop.manager.ai.dashscope.DashScopeAssistant;
import org.apache.bigtop.manager.ai.openai.OpenAIAssistant;
import org.apache.bigtop.manager.ai.qianfan.QianFanAssistant;

import org.springframework.stereotype.Component;

import dev.langchain4j.service.tool.ToolProvider;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class GeneralAssistantFactory extends AbstractAIAssistantFactory {

    @Resource
    private SystemPromptProvider systemPromptProvider;

    @Resource
    private ChatMemoryStoreProvider chatMemoryStoreProvider;

    private void configureSystemPrompt(AIAssistant.Builder builder, SystemPrompt systemPrompt, String locale) {
        LocSystemPromptProvider locSystemPromptProvider = (LocSystemPromptProvider) systemPromptProvider;
        List<String> systemPrompts = new ArrayList<>();
        if (systemPrompt != null) {
            systemPrompts.add(locSystemPromptProvider.getSystemMessage(systemPrompt));
        }
        if (locale != null) {
            systemPrompts.add(locSystemPromptProvider.getLanguagePrompt(locale));
        }
        builder.withSystemPrompt(locSystemPromptProvider.getSystemMessages(systemPrompts));
    }

    private AIAssistant.Builder initializeBuilder(PlatformType platformType) {
        return switch (platformType) {
            case OPENAI -> OpenAIAssistant.builder();
            case DASH_SCOPE -> DashScopeAssistant.builder();
            case QIANFAN -> QianFanAssistant.builder();
        };
    }

    @Override
    public AIAssistant createWithPrompt(
            AIAssistantConfig config, ToolProvider toolProvider, SystemPrompt systemPrompt) {
        GeneralAssistantConfig generalAssistantConfig = (GeneralAssistantConfig) config;
        PlatformType platformType = generalAssistantConfig.getPlatformType();
        Object id = generalAssistantConfig.getId();
        if (id == null) {
            throw new AssistantConfigNotSetException("ID");
        }

        AIAssistant.Builder builder = initializeBuilder(platformType);
        builder.id(id)
                .memoryStore(chatMemoryStoreProvider.createPersistentChatMemoryStore())
                .withConfig(generalAssistantConfig)
                .withToolProvider(toolProvider);

        configureSystemPrompt(builder, systemPrompt, generalAssistantConfig.getLanguage());

        return builder.build();
    }

    @Override
    public AIAssistant createForTest(AIAssistantConfig config, ToolProvider toolProvider) {
        GeneralAssistantConfig generalAssistantConfig = (GeneralAssistantConfig) config;
        PlatformType platformType = generalAssistantConfig.getPlatformType();
        AIAssistant.Builder builder = initializeBuilder(platformType);

        builder.id(null)
                .memoryStore(chatMemoryStoreProvider.createInMemoryChatMemoryStore())
                .withConfig(generalAssistantConfig)
                .withToolProvider(toolProvider);

        return builder.build();
    }
}
