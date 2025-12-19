package org.apache.bigtop.manager.ai.core.factory;

import org.apache.bigtop.manager.ai.core.config.AIAssistantConfig;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.StreamingChatModel;
import reactor.core.publisher.Flux;

public interface AIAssistant {

    /**
     * This ID is the unique identifier for the {@link AIAssistant}.
     * Its memory storage is independent of AIAssistant instances in other threads.
     * @return
     */
    Object getId();

    /**
     * This is a conversation based on streaming output.
     * @param userMessage
     * @return
     */
    Flux<String> streamAsk(String userMessage);

    /**
     * This is a conversation based on blocking output.
     * @param userMessage
     * @return
     */
    String ask(String userMessage);

    /**
     * This is used to get the AIAssistant's Platform
     * @return
     */
    PlatformType getPlatform();

    /**
     * This is used to test whether the configuration is correct
     * @return
     */
    boolean test();

    interface Service {
        String chat(String userMessage);

        Flux<String> streamChat(String userMessage);
    }

    interface Builder {
        Builder id(Object id);

        Builder memoryStore(ChatMemory memoryStore);

        Builder withConfig(AIAssistantConfig configProvider);

        Builder withSystemPrompt(String systemPrompt);

        AIAssistant build();

        ChatModel getChatModel();

        StreamingChatModel getStreamingChatModel();

        ChatMemory getChatMemory();
    }
}
