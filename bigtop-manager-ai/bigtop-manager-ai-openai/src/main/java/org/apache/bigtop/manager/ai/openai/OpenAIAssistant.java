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
package org.apache.bigtop.manager.ai.openai;

import org.apache.bigtop.manager.ai.core.AbstractAIAssistant;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.exception.AssistantConfigNotSetException;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.internal.ValidationUtils;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

public class OpenAIAssistant extends AbstractAIAssistant {

    private final ChatLanguageModel chatLanguageModel;
    private final StreamingChatLanguageModel streamingChatLanguageModel;

    private static final String BASE_URL = "https://api.openai.com/v1";

    public OpenAIAssistant(
            ChatLanguageModel chatLanguageModel,
            StreamingChatLanguageModel streamingChatLanguageModel,
            ChatMemory chatMemory) {
        super(chatMemory);
        this.chatLanguageModel = chatLanguageModel;
        this.streamingChatLanguageModel = streamingChatLanguageModel;
    }

    @Override
    public Flux<String> streamAsk(String chatMessage) {
        if (chatMemory == null || streamingChatLanguageModel == null) {
            throw new AssistantConfigNotSetException("threadId");
        }
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
        if (chatMemory == null || chatLanguageModel == null) {
            throw new AssistantConfigNotSetException("threadId");
        }
        chatMemory.add(UserMessage.from(chatMessage));
        Response<AiMessage> generate = chatLanguageModel.generate(chatMemory.messages());
        String aiMessage = generate.content().text();
        chatMemory.add(AiMessage.from(aiMessage));
        return aiMessage;
    }

    @Override
    public void setSystemPrompt(String systemPrompt) {
        if (chatMemory == null) {
            throw new AssistantConfigNotSetException("threadId");
        }
        chatMemory.add(SystemMessage.systemMessage(systemPrompt));
    }

    public void setSystemPrompt(SystemMessage systemPrompt) {
        if (chatMemory == null) {
            throw new AssistantConfigNotSetException("threadId");
        }
        chatMemory.add(systemPrompt);
    }

    @Override
    public Object getId() {
        return chatMemory.id();
    }

    public void resetMemory() {
        chatMemory.clear();
    }

    @Override
    public PlatformType getPlatform() {
        return PlatformType.OPENAI;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Object id;

        private ChatMemoryStore chatMemoryStore;
        private AIAssistantConfigProvider configProvider;

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

        public AIAssistant build() {
            String model = ValidationUtils.ensureNotNull(configProvider.getModel(), "model");
            String apiKey = ValidationUtils.ensureNotNull(
                    configProvider.getCredentials().get("apiKey"), "apiKey");
            ChatLanguageModel openAiChatModel = OpenAiChatModel.builder()
                    .apiKey(apiKey)
                    .baseUrl(BASE_URL)
                    .modelName(model)
                    .build();
            StreamingChatLanguageModel openaiStreamChatModel = OpenAiStreamingChatModel.builder()
                    .apiKey(apiKey)
                    .baseUrl(BASE_URL)
                    .modelName(model)
                    .build();
            MessageWindowChatMemory.Builder builder = MessageWindowChatMemory.builder()
                    .chatMemoryStore(chatMemoryStore)
                    .maxMessages(MEMORY_LEN);
            if (id != null) {
                builder.id(id);
            }
            MessageWindowChatMemory chatMemory = builder.build();
            return new OpenAIAssistant(openAiChatModel, openaiStreamChatModel, chatMemory);
        }
    }
}
