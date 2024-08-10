package org.apache.bigtop.manager.ai.assistant;

import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.openai.OpenAIConfig;
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

    @Before
    public void init(){
        AIAssistantFactory aiAssistantFactory = new AIAssistantFactory();
        aiAssistant = aiAssistantFactory.create(PlatformType.OPENAI,
                OpenAIConfig.withDefault("sk-", "https://api.mnzdna.xyz/v1"));
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



}
