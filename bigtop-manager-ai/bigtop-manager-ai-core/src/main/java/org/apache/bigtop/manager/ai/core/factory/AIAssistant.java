package org.apache.bigtop.manager.ai.core.factory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import reactor.core.publisher.Flux;

/**
 * @Project: org.apache.bigtop.manager.ai.core.factory
 * @Author: pgthinker
 * @GitHub: https://github.com/ningning0111
 * @Date: 2024/8/10 15:55
 * @Description:
 */
public interface AIAssistant {

    Flux<String> streamAsk(ChatMessage userMessage);

    String ask(ChatMessage userMessage);

    ChatMemory getMemory();

    String getPlatform();



    default Flux<String> streamAsk(String message){
        return streamAsk(UserMessage.from(message));
    }
    default String ask(String message){
        return ask(UserMessage.from(message));
    }



}
