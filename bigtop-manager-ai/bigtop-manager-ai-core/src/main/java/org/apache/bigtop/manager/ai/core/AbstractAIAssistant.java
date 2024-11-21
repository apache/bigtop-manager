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

import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

public abstract class AbstractAIAssistant implements AIAssistant {

    protected final ChatLanguageModel chatLanguageModel;
    protected final StreamingChatLanguageModel streamingChatLanguageModel;
    protected static final Integer MEMORY_LEN = 10;
    protected final ChatMemory chatMemory;

    protected AbstractAIAssistant(
            ChatLanguageModel chatLanguageModel,
            StreamingChatLanguageModel streamingChatLanguageModel,
            ChatMemory chatMemory) {
        this.chatLanguageModel = chatLanguageModel;
        this.streamingChatLanguageModel = streamingChatLanguageModel;
        this.chatMemory = chatMemory;
    }

    @Override
    public boolean test() {
        return ask("1+1=") != null;
    }

    @Override
    public Object getId() {
        return chatMemory.id();
    }

    @Override
    public Flux<String> streamAsk(String chatMessage) {
        chatMemory.add(UserMessage.from(chatMessage));
        return Flux.create(
                emitter -> streamingChatLanguageModel.generate(chatMemory.messages(), new StreamingResponseHandler<>() {
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
                }),
                FluxSink.OverflowStrategy.BUFFER);
    }

    @Override
    public String ask(String chatMessage) {
        chatMemory.add(UserMessage.from(chatMessage));
        Response<AiMessage> generate = chatLanguageModel.generate(chatMemory.messages());
        String aiMessage = generate.content().text();
        chatMemory.add(AiMessage.from(aiMessage));
        return aiMessage;
    }

    public abstract static class Builder implements AIAssistant.Builder {
        protected Object id;

        protected ChatMemoryStore chatMemoryStore;
        protected AIAssistantConfigProvider configProvider;

        public Builder() {}

        public Builder withConfigProvider(AIAssistantConfigProvider configProvider) {
            this.configProvider = configProvider;
            return this;
        }

        public Builder id(Object id) {
            this.id = id;
            return this;
        }

        public Builder memoryStore(ChatMemoryStore chatMemoryStore) {
            this.chatMemoryStore = chatMemoryStore;
            return this;
        }

        public MessageWindowChatMemory getChatMemory() {
            MessageWindowChatMemory.Builder builder = MessageWindowChatMemory.builder()
                    .chatMemoryStore(chatMemoryStore)
                    .maxMessages(MEMORY_LEN);
            if (id != null) {
                builder.id(id);
            }
            return builder.build();
        }
    }
}
