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

import org.apache.bigtop.manager.ai.assistant.store.PersistentChatMemoryStore;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;
import org.apache.bigtop.manager.ai.core.provider.SystemPromptProvider;
import org.apache.bigtop.manager.ai.openai.OpenAIAssistant;

import org.apache.commons.lang3.NotImplementedException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class GeneralAssistantFactoryTest {

    @Mock
    private SystemPromptProvider systemPromptProvider;

    @Mock
    private ChatMemoryStore chatMemoryStore;

    @Mock
    private AIAssistantConfigProvider assistantConfigProvider;

    @InjectMocks
    private GeneralAssistantFactory generalAssistantFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatMemoryStore = new PersistentChatMemoryStore(null, null);
        generalAssistantFactory = new GeneralAssistantFactory(systemPromptProvider, chatMemoryStore);
        Map<String, String> credentials = Map.of("apiKey", "123456");
        when(assistantConfigProvider.getModel()).thenReturn("model");
        when(assistantConfigProvider.getCredentials()).thenReturn(credentials);
        when(assistantConfigProvider.getConfigs()).thenReturn(null);
        when(assistantConfigProvider.getLanguage()).thenReturn("en");
    }

    @Test
    void testCreateAIAssistant() {
        AIAssistant.Builder mockBuilder = mock(OpenAIAssistant.Builder.class);
        when(mockBuilder.id(any())).thenReturn(mockBuilder);
        when(mockBuilder.memoryStore(any())).thenReturn(mockBuilder);
        when(mockBuilder.withConfigProvider(any())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mock(AIAssistant.class));

        try (MockedStatic<OpenAIAssistant> openAIAssistantMockedStatic = mockStatic(OpenAIAssistant.class)) {
            openAIAssistantMockedStatic.when(OpenAIAssistant::builder).thenReturn(mockBuilder);

            PlatformType platformType = PlatformType.OPENAI;
            generalAssistantFactory.create(platformType, assistantConfigProvider);
            generalAssistantFactory = new GeneralAssistantFactory(chatMemoryStore);
            generalAssistantFactory.create(platformType, assistantConfigProvider, UUID.randomUUID());
            assertThrows(NotImplementedException.class, () -> {
                generalAssistantFactory.createToolBox(platformType);
            });
        }
    }
}
