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
package org.apache.bigtop.manager.ai.assistant.store;

import org.apache.bigtop.manager.dao.po.ChatMessagePO;
import org.apache.bigtop.manager.dao.po.ChatThreadPO;
import org.apache.bigtop.manager.dao.repository.ChatMessageRepository;
import org.apache.bigtop.manager.dao.repository.ChatThreadRepository;

import org.springframework.stereotype.Component;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PersistentChatMemoryStore implements ChatMemoryStore {

    private final ChatThreadRepository chatThreadRepository;
    private final ChatMessageRepository chatMessageRepository;

    public PersistentChatMemoryStore(
            ChatThreadRepository chatThreadRepository, ChatMessageRepository chatMessageRepository) {
        this.chatThreadRepository = chatThreadRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    private ChatMessage convertToChatMessage(ChatMessagePO chatMessagePO) {
        if (chatMessagePO.getSender().equals("AI")) {
            return new AiMessage(chatMessagePO.getMessage());
        } else if (chatMessagePO.getSender().equals("User")) {
            return new UserMessage(chatMessagePO.getMessage());
        } else if (chatMessagePO.getSender().equals("System")) {
            return new SystemMessage(chatMessagePO.getMessage());
        } else {
            return null;
        }
    }

    private ChatMessagePO convertToChatMessagePO(ChatMessage chatMessage, Long chatThreadId) {
        ChatMessagePO chatMessagePO = new ChatMessagePO();
        if (chatMessage.type().equals(ChatMessageType.AI)) {
            chatMessagePO.setSender("AI");
            AiMessage aiMessage = (AiMessage) chatMessage;
            chatMessagePO.setMessage(aiMessage.text());
        } else if (chatMessage.type().equals(ChatMessageType.USER)) {
            chatMessagePO.setSender("User");
            UserMessage userMessage = (UserMessage) chatMessage;
            chatMessagePO.setMessage(userMessage.singleText());
        } else if (chatMessage.type().equals(ChatMessageType.SYSTEM)) {
            chatMessagePO.setSender("System");
            SystemMessage systemMessage = (SystemMessage) chatMessage;
            chatMessagePO.setMessage(systemMessage.text());
        } else {
            chatMessagePO.setSender(chatMessage.type().toString());
        }
        ChatThreadPO chatThreadPO = chatThreadRepository.findById(chatThreadId).orElse(null);
        if (chatThreadPO != null) {
            chatMessagePO.setUserPO(chatThreadPO.getUserPO());
        } else {
            chatMessagePO.setUserPO(null);
        }
        chatMessagePO.setChatThreadPO(
                chatThreadRepository.findById(chatThreadId).orElse(null));
        return chatMessagePO;
    }

    @Override
    public List<ChatMessage> getMessages(Object threadId) {
        log.info("getMessages called");
        log.info("threadId: {}", threadId.toString());
        ChatThreadPO chatThreadPO = null;
        if (chatThreadRepository != null) {
            chatThreadPO = chatThreadRepository.findById((Long) threadId).orElse(null);
        }
        if (chatThreadPO == null) {
            return new ArrayList<>();
        }
        List<ChatMessagePO> chatMessages = chatMessageRepository.findAllByChatThreadPO(chatThreadPO);
        if (chatMessages.isEmpty()) {
            return new ArrayList<>();
        } else {
            return chatMessages.stream().map(this::convertToChatMessage).collect(Collectors.toList());
        }
    }

    @Override
    public void updateMessages(Object threadId, List<ChatMessage> messages) {
        log.info("updateMessages called");
        ChatMessagePO chatMessagePO = convertToChatMessagePO(messages.get(messages.size() - 1), (Long) threadId);
        chatMessageRepository.save(chatMessagePO);
    }

    @Override
    public void deleteMessages(Object threadId) {
        log.info("deleteMessages called");
        ChatThreadPO chatThreadPO =
                chatThreadRepository.findById((Long) threadId).orElse(null);
        chatMessageRepository.deleteByChatThreadPO(chatThreadPO);
    }
}
