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
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;

import org.springframework.util.NumberUtils;

import dev.langchain4j.internal.ValidationUtils;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.qianfan.QianfanChatModel;
import dev.langchain4j.model.qianfan.QianfanStreamingChatModel;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import java.util.HashMap;
import java.util.Map;

public class QianFanAssistant extends AbstractAIAssistant {

    private static final String PLATFORM_NAME = "qianfan";
    private static final String MODEL_NAME = "glm-4-0520";

    private QianFanAssistant(
            ChatLanguageModel chatLanguageModel,
            StreamingChatLanguageModel streamingChatLanguageModel,
            ChatMemory chatMemory) {
        super(chatLanguageModel, streamingChatLanguageModel, chatMemory);
    }

    @Override
    public String getPlatform() {
        return PLATFORM_NAME;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Object id;

        private Map<String, String> configs = new HashMap<>();
        private ChatMemoryStore chatMemoryStore;

        public Builder() {
            configs.put("modelName", MODEL_NAME);
        }

        public Builder withConfigProvider(AIAssistantConfigProvider configProvider) {
            this.configs = configProvider.configs();
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
            ValidationUtils.ensureNotNull(id, "id");
            String modelName = configs.get("modelName");
            String apiKey = ValidationUtils.ensureNotNull(configs.get("apiKey"), "apiKey");
            String secretKey = ValidationUtils.ensureNotNull(configs.get("secretKey"), "secretKey");
            Integer memoryLen = ValidationUtils.ensureNotNull(
                    NumberUtils.parseNumber(configs.get("memoryLen"), Integer.class), "memoryLen not a number.");
            ChatLanguageModel qianFanChatModel = QianfanChatModel.builder()
                    .apiKey(apiKey)
                    .secretKey(secretKey)
                    .modelName(modelName)
                    .build();
            StreamingChatLanguageModel qianFanStreamChatModel = QianfanStreamingChatModel.builder()
                    .apiKey(apiKey)
                    .secretKey(secretKey)
                    .modelName(modelName)
                    .build();
            MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                    .id(id)
                    .chatMemoryStore(chatMemoryStore)
                    .maxMessages(memoryLen)
                    .build();
            return new QianFanAssistant(qianFanChatModel, qianFanStreamChatModel, chatMemory);
        }
    }
}
