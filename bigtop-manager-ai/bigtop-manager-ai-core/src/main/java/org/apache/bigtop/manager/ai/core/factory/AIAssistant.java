package org.apache.bigtop.manager.ai.core.factory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import reactor.core.publisher.Flux;

/**
 * @Project: org.apache.bigtop.manager.ai.core.factory
 * @Author: pgthinker
 * @GitHub: https://github.com/ningning0111
 * @Date: 2024/8/10 15:55
 * @Description: The {@link AIAssistant} class is an interface class for an AI assistant, abstractly defining some functionalities of the assistant.
 */
public interface AIAssistant {

    Flux<String> streamAsk(ChatMessage userMessage);

    String ask(ChatMessage userMessage);

    ChatMemory getMemory();

    String getPlatform();

    void setSystemPrompt(SystemMessage systemPrompt);


    void resetMemory();

    /**
     * This ID is the unique identifier for the {@link AIAssistant}. Its memory storage is independent of AIAssistant instances in other threads....
     * @return
     */
    Object getId();

    default Flux<String> streamAsk(String message){
        return streamAsk(UserMessage.from(message));
    }
    default String ask(String message){
        return ask(UserMessage.from(message));
    }
    default void setSystemPrompt(String systemPrompt){
        setSystemPrompt(systemPrompt);
    }




}
