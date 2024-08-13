package org.apache.bigtop.manager.ai.assistant;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import org.apache.bigtop.manager.ai.assistant.provider.LocSystemPromptProvider;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.factory.ToolBox;
import org.apache.bigtop.manager.ai.assistant.provider.AIAssistantConfig;
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;

/**
 * @Project: org.apache.bigtop.manager.ai.assistant
 * @Author: pgthinker
 * @GitHub: https://github.com/ningning0111
 * @Date: 2024/8/10 20:10
 * @Description:
 */
public class AIAssistantFactoryTests {

    private AIAssistant aiAssistant = null;
    private AIAssistantFactory aiAssistantFactory = new AIAssistantFactory();

    // The Key and url share from : https://pgthinker.me/2023/10/03/196/
    private AIAssistantConfigProvider configProvider = AIAssistantConfig.builder()
            .set("apiKey", "sk-KgvugzpKzki15GFxB72e7782De844b23B3E4Fc6dDf40B29a")
            .set("baseUrl", "https://api.mnzdna.xyz/v1")
            .set("memoryLen", "10")
            .set("modelName", OpenAiChatModelName.GPT_3_5_TURBO.toString())
            .build();


    @Before
    public void init(){
        aiAssistant = aiAssistantFactory.create(
                PlatformType.OPENAI.getValue(),
                configProvider
        );

        ToolBox toolBox = aiAssistantFactory.createToolBox(PlatformType.OPENAI);

    }

    @Test
    public void simpleChatTest(){
    String message = aiAssistant.ask("Hi,GPT!");
        System.out.println(message);
        Assert.assertFalse(message, message.isEmpty());
    }

    @Test
    public void streamChatTest() throws InterruptedException {
        Flux<String> resp = aiAssistant.streamAsk("Hi,GOT!");
        resp.subscribe(System.out::println, error-> System.out.println("error:" + error), ()-> System.out.println("Completed"));
        Thread.sleep(5000);
    }

    @Test
    public void simpleMemoryTest(){
        String message1 = aiAssistant.ask("Hi, GPT!");
        System.out.println(message1);
        Assert.assertFalse(message1.isEmpty());

        String message2 = aiAssistant.ask("What's your name?");
        System.out.println(message2);
        Assert.assertFalse(message2, message2.isEmpty());

        String message3 = aiAssistant.ask("What's langchain4j?");
        System.out.println(message3);
        Assert.assertFalse(message3, message3.isEmpty());

        String message4 = aiAssistant.ask("What was my first question during our conversation?");
        System.out.println(message4);
        Assert.assertFalse(message4, message4.isEmpty());

        String message5 = aiAssistant.ask("What is my second question during our conversation?");
        System.out.println(message5);
        Assert.assertFalse(message5, message5.isEmpty());

        String message6 = aiAssistant.ask("What is your answer to the first question?");
        System.out.println(message6);
        Assert.assertFalse(message6, message6.isEmpty());

        String message7 = aiAssistant.ask("What is your answer to the second question?");
        System.out.println(message7);
        Assert.assertFalse(message7, message7.isEmpty());
    }

    @Test
    public void platformTest(){
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

        System.out.println(memory1.messages());
        System.out.println(memory2.messages());

        Object id = memory2.id();
        aiAssistantFactory.create(PlatformType.OPENAI,configProvider,id);
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
