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
package org.apache.bigtop.manager.ai.qianfan;

import org.apache.bigtop.manager.ai.core.AbstractAIAssistant;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.internal.ValidationUtils;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.qianfan.QianfanChatModel;
import dev.langchain4j.model.qianfan.QianfanStreamingChatModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

public class QianFanAssistant extends AbstractAIAssistant {

    private final ChatLanguageModel chatLanguageModel;
    private final StreamingChatLanguageModel streamingChatLanguageModel;
    private SystemMessage systemMessage;

    public QianFanAssistant(
            ChatLanguageModel chatLanguageModel,
            StreamingChatLanguageModel streamingChatLanguageModel,
            ChatMemory chatMemory) {
        super(chatMemory);
        this.chatLanguageModel = chatLanguageModel;
        this.streamingChatLanguageModel = streamingChatLanguageModel;
        for (ChatMessage chatMessage : chatMemory.messages()) {
            if (chatMessage instanceof SystemMessage) {
                this.systemMessage = (SystemMessage) chatMessage;
            }
        }
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

    @Override
    public void setSystemPrompt(String systemPrompt) {
        // Multiple system messages are not supported
        if (this.systemMessage == null) {
            this.systemMessage = SystemMessage.systemMessage(systemPrompt);
            chatMemory.add(this.systemMessage);
        }
    }

    @Override
    public PlatformType getPlatform() {
        return PlatformType.QIANFAN;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractAIAssistant.Builder {

        public AIAssistant build() {
            MessageWindowChatMemory.Builder builder = MessageWindowChatMemory.builder()
                    .chatMemoryStore(chatMemoryStore)
                    .maxMessages(MEMORY_LEN);
            if (id != null) {
                builder.id(id);
            }
            MessageWindowChatMemory chatMemory = builder.build();
            return new QianFanAssistant(getChatLanguageModel(), getStreamingChatLanguageModel(), chatMemory);
        }

        @Override
        public ChatLanguageModel getChatLanguageModel() {
            String model = ValidationUtils.ensureNotNull(configProvider.getModel(), "model");
            String apiKey = ValidationUtils.ensureNotNull(
                    configProvider.getCredentials().get("apiKey"), "apiKey");
            String secretKey = ValidationUtils.ensureNotNull(
                    configProvider.getCredentials().get("secretKey"), "secretKey");
            return QianfanChatModel.builder()
                    .apiKey(apiKey)
                    .secretKey(secretKey)
                    .modelName(model)
                    .build();
        }

        @Override
        public StreamingChatLanguageModel getStreamingChatLanguageModel() {
            String model = ValidationUtils.ensureNotNull(configProvider.getModel(), "model");
            String apiKey = ValidationUtils.ensureNotNull(
                    configProvider.getCredentials().get("apiKey"), "apiKey");
            String secretKey = ValidationUtils.ensureNotNull(
                    configProvider.getCredentials().get("secretKey"), "secretKey");
            return QianfanStreamingChatModel.builder()
                    .apiKey(apiKey)
                    .secretKey(secretKey)
                    .modelName(model)
                    .build();
        }
    }
}
