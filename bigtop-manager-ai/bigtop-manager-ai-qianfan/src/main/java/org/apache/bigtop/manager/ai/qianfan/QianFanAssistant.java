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
import org.apache.bigtop.manager.ai.core.enums.MessageSender;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import reactor.core.publisher.Flux;

import java.util.Map;

public class QianFanAssistant extends AbstractAIAssistant {

    public QianFanAssistant(ChatMemory chatMemory) {
        super(chatMemory);
    }

    private void saveMessage(String message, MessageSender sender) {
        ChatMessage chatMessage;
        if (sender.equals(MessageSender.AI)) {
            chatMessage = new AiMessage(message);
        } else if (sender.equals(MessageSender.USER)) {
            chatMessage = new UserMessage(message);
        } else if (sender.equals(MessageSender.SYSTEM)) {
            chatMessage = new SystemMessage(message);
        } else {
            return;
        }
        chatMemory.add(chatMessage);
    }

    @Override
    public PlatformType getPlatform() {
        return PlatformType.DASH_SCOPE;
    }

    @Override
    public void setSystemPrompt(String systemPrompt) {}

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Object getId() {
        return null;
    }

    @Override
    public Flux<String> streamAsk(String userMessage) {
        return null;
    }

    @Override
    public String ask(String userMessage) {
        return null;
    }

    @Override
    public Map<String, String> createThread() {
        return null;
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
            return new QianFanAssistant(chatMemory);
        }
    }
}
