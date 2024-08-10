package org.apache.bigtop.manager.ai.core;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

/**
 * @Project: org.apache.bigtop.manager.ai.core
 * @Author: pgthinker
 * @GitHub: https://github.com/ningning0111
 * @Date: 2024/8/10 17:45
 * @Description:
 */
public abstract class AbstractAIAssistant implements AIAssistant {
    private final ChatLanguageModel chatLanguageModel;
    private final StreamingChatLanguageModel streamingChatLanguageModel;
    private final ChatMemory chatMemory;

    public AbstractAIAssistant(ChatLanguageModel chatLanguageModel, StreamingChatLanguageModel streamingChatLanguageModel, ChatMemory chatMemory) {
        this.chatLanguageModel = chatLanguageModel;
        this.streamingChatLanguageModel = streamingChatLanguageModel;
        this.chatMemory = chatMemory;
    }

    @Override
    public Flux<String> streamAsk(ChatMessage chatMessage) {
        chatMemory.add(chatMessage);
        Flux<String> streamAiMessage = Flux.create(emitter -> {
            streamingChatLanguageModel.generate(chatMemory.messages(), new StreamingResponseHandler<>() {
                @Override
                public void onNext(String token) {
                    emitter.next(token);
                }

                @Override
                public void onError(Throwable error) {
                    emitter.error(error);
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    StreamingResponseHandler.super.onComplete(response);
                    chatMemory.add(response.content());
                }
            });
        }, FluxSink.OverflowStrategy.BUFFER);

        return streamAiMessage;
    }

    @Override
    public String ask(ChatMessage chatMessage) {
        chatMemory.add(chatMessage);
        Response<AiMessage> generate = chatLanguageModel.generate(chatMemory.messages());
        String aiMessage = generate.content().text();
        chatMemory.add(AiMessage.from(aiMessage));
        return aiMessage;
    }


    @Override
    public ChatMemory getMemory() {
        return this.chatMemory;
    }
}
