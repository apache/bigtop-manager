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
package org.apache.bigtop.manager.ai.core;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

public abstract class AbstractAIAssistant implements AIAssistant {
    private final ChatLanguageModel chatLanguageModel;
    private final StreamingChatLanguageModel streamingChatLanguageModel;
    private final Object assistantId;
    private final ChatMemory chatMemory;

    public AbstractAIAssistant(ChatLanguageModel chatLanguageModel, StreamingChatLanguageModel streamingChatLanguageModel, ChatMemory chatMemory) {
        this.chatLanguageModel = chatLanguageModel;
        this.streamingChatLanguageModel = streamingChatLanguageModel;
        this.chatMemory = chatMemory;
        this.assistantId = this.chatMemory.id();
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
    public void setSystemPrompt(SystemMessage systemPrompt) {
        chatMemory.add(systemPrompt);
    }

    @Override
    public Object getId() {
        return chatMemory.id();
    }

    @Override
    public void resetMemory() {
        chatMemory.clear();
    }

    @Override
    public ChatMemory getMemory() {
        return this.chatMemory;
    }
}
