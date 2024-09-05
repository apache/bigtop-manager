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

import org.apache.bigtop.manager.ai.assistant.provider.AIAssistantConfig;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.factory.AIAssistantFactory;
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import dev.langchain4j.model.openai.OpenAiChatModelName;
import reactor.core.publisher.Flux;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

public class AIAssistantServiceTest {

    private AIAssistantConfigProvider configProvider = AIAssistantConfig.builder()
            .addConfig("apiKey", "sk-")
            // The `baseUrl` has a default value that is automatically generated based on the `PlatformType`.
            .addConfig("baseUrl", "https://api.openai.com/v1")
            .addConfig("modelName", OpenAiChatModelName.GPT_3_5_TURBO.toString())
            .build();

    @Mock
    private AIAssistant aiAssistant;

    @Mock
    private AIAssistantFactory aiAssistantFactory;

    private final String threadId = UUID.randomUUID().toString();

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        when(aiAssistant.ask("1?")).thenReturn("1");
        when(aiAssistant.streamAsk("stream 1?")).thenReturn(Flux.create(emmit -> {
            final String text = "stream text";
            for (int i = 0; i < text.length(); i++) {
                emmit.next(text.charAt(i) + "");
            }
        }));
        when(aiAssistantFactory.create(PlatformType.OPENAI, configProvider, threadId, false))
                .thenReturn(this.aiAssistant);
        when(aiAssistant.getPlatform()).thenReturn(PlatformType.OPENAI);
    }

    @Test
    public void createNew2SimpleChat() {
        AIAssistant aiAssistant = aiAssistantFactory.create(PlatformType.OPENAI, configProvider, threadId, false);
        String ask = aiAssistant.ask("1?");
        assertFalse(ask.isEmpty());
        System.out.println(ask);
    }

    @Test
    public void createNew2StreamChat() throws InterruptedException {
        AIAssistant aiAssistant = aiAssistantFactory.create(PlatformType.OPENAI, configProvider, threadId, false);
        Flux<String> stringFlux = aiAssistant.streamAsk("stream 1?");
        stringFlux.subscribe(
                System.out::println,
                error -> System.out.println("error:" + error),
                () -> System.out.println("Completed"));
        Thread.sleep(1000);
    }
}
