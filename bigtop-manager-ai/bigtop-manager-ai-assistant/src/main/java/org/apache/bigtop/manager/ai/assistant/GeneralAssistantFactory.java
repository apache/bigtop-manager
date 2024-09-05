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
import org.apache.bigtop.manager.ai.assistant.provider.PersistentProvider;
import org.apache.bigtop.manager.ai.core.AbstractAIAssistantFactory;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.enums.SystemPrompt;
import org.apache.bigtop.manager.ai.core.exception.PlatformNotFoundException;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.factory.ToolBox;
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;
import org.apache.bigtop.manager.ai.core.provider.SystemPromptProvider;
import org.apache.bigtop.manager.ai.dashscope.DashScopeAssistant;
import org.apache.bigtop.manager.ai.openai.OpenAIAssistant;

import org.apache.commons.lang3.NotImplementedException;

import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;

import java.util.Objects;

public class GeneralAssistantFactory extends AbstractAIAssistantFactory {

    private final SystemPromptProvider systemPromptProvider;
    private final PersistentProvider persistentProvider;

    public GeneralAssistantFactory() {
        this(new LocSystemPromptProvider(), new PersistentProvider());
    }

    public GeneralAssistantFactory(SystemPromptProvider systemPromptProvider) {
        this(systemPromptProvider, new PersistentProvider());
    }

    public GeneralAssistantFactory(PersistentProvider persistentProvider) {
        this(new LocSystemPromptProvider(), persistentProvider);
    }

    public GeneralAssistantFactory(SystemPromptProvider systemPromptProvider, PersistentProvider persistentProvider) {
        this.systemPromptProvider = systemPromptProvider;
        this.persistentProvider = persistentProvider;
    }

    @Override
    public AIAssistant createWithPrompt(
            PlatformType platformType,
            AIAssistantConfigProvider assistantConfig,
            Object id,
            SystemPrompt systemPrompts,
            boolean isPersistent) {
        AIAssistant aiAssistant;
        if (Objects.requireNonNull(platformType) == PlatformType.OPENAI) {
            aiAssistant = OpenAIAssistant.builder()
                    .id(id)
                    .memoryStore(isPersistent ? persistentProvider.getChatMemoryStore() : new InMemoryChatMemoryStore())
                    .withConfigProvider(assistantConfig)
                    .build();
        } else if (Objects.requireNonNull(platformType) == PlatformType.DASH_SCOPE) {
            aiAssistant = DashScopeAssistant.builder()
                    .id(id)
                    .withConfigProvider(assistantConfig)
                    .messageRepository(persistentProvider.getPersistentRepository())
                    .build();
        } else {
            throw new PlatformNotFoundException(platformType.getValue());
        }
        if (isPersistent) {
            String systemPrompt = systemPromptProvider.getSystemMessage(systemPrompts);
            aiAssistant.setSystemPrompt(systemPrompt);
            String locale = assistantConfig.getLanguage();
            if (locale != null) {
                aiAssistant.setSystemPrompt(systemPromptProvider.getLanguagePrompt(locale));
            }
        }
        return aiAssistant;
    }

    @Override
    public AIAssistant create(
            PlatformType platformType, AIAssistantConfigProvider assistantConfig, Object id, boolean isPersistent) {
        return createWithPrompt(platformType, assistantConfig, id, SystemPrompt.DEFAULT_PROMPT, isPersistent);
    }

    @Override
    public ToolBox createToolBox(PlatformType platformType) {
        throw new NotImplementedException("ToolBox is not implemented for GeneralAssistantFactory");
    }
}
