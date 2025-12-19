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
import org.apache.bigtop.manager.ai.core.AbstractAIAssistantFactory;
import org.apache.bigtop.manager.ai.core.config.AIAssistantConfig;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.enums.SystemPrompt;
import org.apache.bigtop.manager.ai.core.exception.AssistantConfigNotSetException;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.provider.SystemPromptProvider;
import org.apache.bigtop.manager.ai.platform.DashScopeAssistant;
import org.apache.bigtop.manager.ai.platform.DeepSeekAssistant;
import org.apache.bigtop.manager.ai.platform.OpenAIAssistant;
import org.apache.bigtop.manager.ai.platform.QianFanAssistant;

import org.springframework.stereotype.Component;

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
        List<String> systemPrompts = new ArrayList<>();
        if (systemPrompt != null) {
            systemPrompts.add(systemPromptProvider.getSystemMessage(systemPrompt));
        }
        if (locale != null) {
            systemPrompts.add(systemPromptProvider.getLanguagePrompt(locale));
        }
        builder.withSystemPrompt(systemPromptProvider.getSystemMessages(systemPrompts));
    }

    private AIAssistant.Builder initializeBuilder(PlatformType platformType) {
        return switch (platformType) {
            case OPENAI -> OpenAIAssistant.builder();
            case DASH_SCOPE -> DashScopeAssistant.builder();
            case QIANFAN -> QianFanAssistant.builder();
            case DEEPSEEK -> DeepSeekAssistant.builder();
        };
    }

    @Override
    public AIAssistant createWithPrompt(
            AIAssistantConfig config, Object toolProvider, SystemPrompt systemPrompt) {
        GeneralAssistantConfig generalAssistantConfig = (GeneralAssistantConfig) config;
        PlatformType platformType = generalAssistantConfig.getPlatformType();
        Object id = generalAssistantConfig.getId();
        if (id == null) {
            throw new AssistantConfigNotSetException("ID");
        }

        AIAssistant.Builder builder = initializeBuilder(platformType);
        builder.id(id)
                .memoryStore(chatMemoryStoreProvider.createPersistentChatMemoryStore(id))
                .withConfig(generalAssistantConfig);

        configureSystemPrompt(builder, systemPrompt, generalAssistantConfig.getLanguage());

        return builder.build();
    }

    @Override
    public AIAssistant createForTest(AIAssistantConfig config, Object toolProvider) {
        GeneralAssistantConfig generalAssistantConfig = (GeneralAssistantConfig) config;
        PlatformType platformType = generalAssistantConfig.getPlatformType();
        AIAssistant.Builder builder = initializeBuilder(platformType);

        builder.id(null)
                .memoryStore(chatMemoryStoreProvider.createInMemoryChatMemoryStore())
                .withConfig(generalAssistantConfig);

        return builder.build();
    }
}
