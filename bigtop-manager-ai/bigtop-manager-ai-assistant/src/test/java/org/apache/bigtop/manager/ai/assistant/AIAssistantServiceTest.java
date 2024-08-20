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

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import org.apache.bigtop.manager.ai.assistant.provider.AIAssistantConfig;
import org.apache.bigtop.manager.ai.assistant.provider.LocSystemPromptProvider;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;
import org.apache.bigtop.manager.ai.core.provider.SystemPromptProvider;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Flux;


public class AIAssistantServiceTest {

    private AIAssistantConfigProvider configProvider = AIAssistantConfig.builder()
            .set("apiKey", "sk-")
            // The `baseUrl` has a default value that is automatically generated based on the `PlatformType`.
            .set("baseUrl", "https://api.openai.com/v1")
            // default 30
            .set("memoryLen", "10")
            .set("modelName", OpenAiChatModelName.GPT_3_5_TURBO.toString())
            .build();
    private AIAssistantFactory aiAssistantFactory = new AIAssistantFactory();


    @Test
    public void createNew2SimpleChat(){
        AIAssistant aiAssistant = aiAssistantFactory.create(PlatformType.OPENAI, configProvider);
        String ask = aiAssistant.ask("Hi GPT!");
        Assert.assertFalse(ask.isEmpty());
        System.out.println(ask);
    }

    @Test
    public void createNew2StreamChat() throws InterruptedException {
        AIAssistant aiAssistant = aiAssistantFactory.create(PlatformType.OPENAI, configProvider);
        Flux<String> stringFlux = aiAssistant.streamAsk("Hi GPT!");
        stringFlux.subscribe(System.out::println, error-> System.out.println("error:" + error), ()-> System.out.println("Completed"));
        Thread.sleep(5000);
    }

    @Test
    public void memoryTest(){
        AIAssistant aiAssistant = aiAssistantFactory.create(PlatformType.OPENAI, configProvider);
        String message1 = aiAssistant.ask("Hi, GPT!");
        Assert.assertFalse(message1.isEmpty());
        System.out.println(message1);

        String message2 = aiAssistant.ask("What's your name?");
        Assert.assertFalse( message2.isEmpty());
        System.out.println(message2);

        String message3 = aiAssistant.ask("What's langchain4j?");
        Assert.assertFalse( message3.isEmpty());
        System.out.println(message3);

        String message4 = aiAssistant.ask("What was my first question during our conversation?");
        Assert.assertFalse(message4.isEmpty());
        System.out.println(message4);

        String message5 = aiAssistant.ask("What is my second question during our conversation?");
        Assert.assertFalse(message5.isEmpty());
        System.out.println(message5);

        String message6 = aiAssistant.ask("What is your answer to the first question?");
        Assert.assertFalse( message6.isEmpty());
        System.out.println(message6);

        String message7 = aiAssistant.ask("What is your answer to the second question?");
        Assert.assertFalse( message7.isEmpty());
        System.out.println(message7);
    }

    @Test
    public void systemPromptTest(){
        SystemPromptProvider locSystemPromptProvider = new LocSystemPromptProvider();
        AIAssistant aiAssistant = aiAssistantFactory.create(PlatformType.OPENAI, configProvider);
        SystemMessage systemPrompt = locSystemPromptProvider.getSystemPrompt("default");
//        SystemMessage systemPrompt = locSystemPromptProvider.getSystemPrompt(); // default return "default" system prompt
        aiAssistant.setSystemPrompt(systemPrompt);

        String ask = aiAssistant.ask("What's is Apache's Bigtop?");
        Assert.assertFalse(ask.isEmpty());

        ChatMemory memory = aiAssistant.getMemory();
        ChatMessage chatMessage = memory.messages().get(0);
        Assert.assertTrue(chatMessage instanceof SystemMessage);
        System.out.println(chatMessage);

    }

    @Test
    public void createAIByIdTest(){
        AIAssistant aiAssistant = aiAssistantFactory.create(PlatformType.OPENAI, configProvider);
        String resp = aiAssistant.ask("Hi GPT! What's your name?");
        Assert.assertFalse(resp.isEmpty());
        System.out.println(resp);
        Object id = aiAssistant.getId();


        // create AI by id
        AIAssistant newAIAssistant = aiAssistantFactory.create(PlatformType.OPENAI, configProvider, id);
        String ask = newAIAssistant.ask("What was my first question during our conversation?");
        Assert.assertFalse(ask.isEmpty());
        System.out.println(ask);
        ChatMemory memory = newAIAssistant.getMemory();
        System.out.println(memory.messages());
    }
}
