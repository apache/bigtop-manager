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
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import org.apache.bigtop.manager.ai.assistant.provider.LocSystemPromptProvider;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.assistant.provider.AIAssistantConfig;
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.List;


public class AIAssistantFactoryTests {

    private AIAssistantFactory aiAssistantFactory = new AIAssistantFactory();
    private PlatformType platformType = PlatformType.OPENAI;

    private AIAssistantConfigProvider configProvider = AIAssistantConfig.builder()
            .set("apiKey", "sk-")
            // The `baseUrl` has a default value that is automatically generated based on the `PlatformType`.
            .set("baseUrl", "https://api.openai.com/v1")
            // default 30
            .set("memoryLen", "10")
            .set("modelName", OpenAiChatModelName.GPT_3_5_TURBO.toString())
            .build();


    @Test
    public void simpleChatTest(){
        AIAssistant aiAssistant = aiAssistantFactory.create(platformType, configProvider);
        String message = aiAssistant.ask("Hi,GPT!");
        Assert.assertFalse(message, message.isEmpty());
        System.out.println(message);

    }

    @Test
    public void streamChatTest() throws InterruptedException {
        AIAssistant aiAssistant = aiAssistantFactory.create(platformType, configProvider);
        Flux<String> resp = aiAssistant.streamAsk("Hi,GOT!");
        resp.subscribe(System.out::println, error-> System.out.println("error:" + error), ()-> System.out.println("Completed"));
        Thread.sleep(5000);
    }

    @Test
    public void simpleMemoryTest(){
        AIAssistant aiAssistant = aiAssistantFactory.create(platformType, configProvider);

        String message1 = aiAssistant.ask("Hi, GPT!");
        Assert.assertFalse(message1.isEmpty());
        System.out.println(message1);

        String message2 = aiAssistant.ask("What's your name?");
        Assert.assertFalse(message2, message2.isEmpty());
        System.out.println(message2);

        String message3 = aiAssistant.ask("What's langchain4j?");
        Assert.assertFalse(message3, message3.isEmpty());
        System.out.println(message3);

        String message4 = aiAssistant.ask("What was my first question during our conversation?");
        Assert.assertFalse(message4, message4.isEmpty());
        System.out.println(message4);

        String message5 = aiAssistant.ask("What is my second question during our conversation?");
        Assert.assertFalse(message5, message5.isEmpty());
        System.out.println(message5);

        String message6 = aiAssistant.ask("What is your answer to the first question?");
        Assert.assertFalse(message6, message6.isEmpty());
        System.out.println(message6);

        String message7 = aiAssistant.ask("What is your answer to the second question?");
        Assert.assertFalse(message7, message7.isEmpty());
        System.out.println(message7);

    }

    @Test
    public void platformTest(){
        AIAssistant aiAssistant = aiAssistantFactory.create(platformType, configProvider);
        String platform = aiAssistant.getPlatform();
        Assert.assertFalse(platform,platform.isEmpty());
        System.out.println(platform);
    }


    @Test
    public void memoryByIdTest(){
        AIAssistant aiAssistant1 = aiAssistantFactory.create(PlatformType.OPENAI.getValue(), configProvider);
        AIAssistant aiAssistant2 = aiAssistantFactory.create(PlatformType.OPENAI.getValue(), configProvider);

        aiAssistant1.ask("Hi GPT!");
        aiAssistant2.ask("Hi GPT!");

        aiAssistant1.ask("What's langchain4j?");
        aiAssistant2.ask("What's langchain4j?");

        aiAssistant1.ask("What was my first question during our conversation?");
        aiAssistant2.ask("What was my first question during our conversation?");

        ChatMemory memory1 = aiAssistant1.getMemory();
        ChatMemory memory2 = aiAssistant2.getMemory();

        Assert.assertFalse(memory1.messages().isEmpty());
        Assert.assertFalse(memory2.messages().isEmpty());

        Object id = memory2.id();
        AIAssistant aiAssistant3 = aiAssistantFactory.create(PlatformType.OPENAI, configProvider, id);
        ChatMemory memory3 = aiAssistant3.getMemory();

        List<ChatMessage> messages2 = memory2.messages();
        List<ChatMessage> messages3 = memory3.messages();
        Assert.assertFalse(memory3.messages().isEmpty());
        Assert.assertFalse(messages3.size() != messages2.size());
        Assert.assertArrayEquals(memory3.messages().toArray(), memory2.messages().toArray());
    }


    @Test
    public void systemPromptTest(){
        AIAssistant aiAssistant1 = aiAssistantFactory.createWithPrompt(PlatformType.OPENAI, configProvider, LocSystemPromptProvider.DEFAULT);
        String resp = aiAssistant1.ask("What is hadoop? What is BigTop? How are they related?");
        System.out.println(resp);
        ChatMemory memory = aiAssistant1.getMemory();
        System.out.println(memory.messages().get(0));
    }


}
