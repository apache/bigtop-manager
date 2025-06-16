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
package org.apache.bigtop.manager.ai.platform;

import org.apache.bigtop.manager.ai.core.AbstractAIAssistant;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;

import dev.langchain4j.community.model.qianfan.QianfanChatModel;
import dev.langchain4j.community.model.qianfan.QianfanStreamingChatModel;
import dev.langchain4j.internal.ValidationUtils;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;

public class QianFanAssistant extends AbstractAIAssistant {

    public QianFanAssistant(ChatMemory chatMemory, AIAssistant.Service aiServices) {
        super(chatMemory, aiServices);
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
            AIAssistant.Service aiService = AiServices.builder(AIAssistant.Service.class)
                    .chatModel(getChatModel())
                    .streamingChatModel(getStreamingChatModel())
                    .chatMemory(getChatMemory())
                    .toolProvider(toolProvider)
                    .systemMessageProvider(threadId -> {
                        if (threadId != null) {
                            return systemPrompt;
                        }
                        return null;
                    })
                    .build();
            return new QianFanAssistant(getChatMemory(), aiService);
        }

        @Override
        public ChatModel getChatModel() {
            String model = ValidationUtils.ensureNotNull(config.getModel(), "model");
            String apiKey =
                    ValidationUtils.ensureNotNull(config.getCredentials().get("apiKey"), "apiKey");
            String secretKey =
                    ValidationUtils.ensureNotNull(config.getCredentials().get("secretKey"), "secretKey");
            return QianfanChatModel.builder()
                    .apiKey(apiKey)
                    .secretKey(secretKey)
                    .modelName(model)
                    .build();
        }

        @Override
        public StreamingChatModel getStreamingChatModel() {
            String model = ValidationUtils.ensureNotNull(config.getModel(), "model");
            String apiKey =
                    ValidationUtils.ensureNotNull(config.getCredentials().get("apiKey"), "apiKey");
            String secretKey =
                    ValidationUtils.ensureNotNull(config.getCredentials().get("secretKey"), "secretKey");
            return QianfanStreamingChatModel.builder()
                    .apiKey(apiKey)
                    .secretKey(secretKey)
                    .modelName(model)
                    .build();
        }
    }
}
