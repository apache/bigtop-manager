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

import org.apache.bigtop.manager.ai.core.enums.MessageSender;
import org.apache.bigtop.manager.dao.po.ChatMessagePO;
import org.apache.bigtop.manager.dao.po.ChatThreadPO;
import org.apache.bigtop.manager.dao.repository.ChatMessageDao;
import org.apache.bigtop.manager.dao.repository.ChatThreadDao;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PersistentChatMemoryStore implements ChatMemoryStore {

    private final ChatThreadDao chatThreadDao;
    private final ChatMessageDao chatMessageDao;

    public PersistentChatMemoryStore(ChatThreadDao chatThreadDao, ChatMessageDao chatMessageDao) {
        this.chatThreadDao = chatThreadDao;
        this.chatMessageDao = chatMessageDao;
    }

    private ChatMessage convertToChatMessage(ChatMessagePO chatMessagePO) {
        String sender = chatMessagePO.getSender().toLowerCase();
        if (sender.equals(MessageSender.AI.getValue())) {
            return new AiMessage(chatMessagePO.getMessage());
        } else if (sender.equals(MessageSender.USER.getValue())) {
            return new UserMessage(chatMessagePO.getMessage());
        } else if (sender.equals(MessageSender.SYSTEM.getValue())) {
            return new SystemMessage(chatMessagePO.getMessage());
        } else {
            return null;
        }
    }

    private ChatMessagePO convertToChatMessagePO(ChatMessage chatMessage, Long chatThreadId) {
        ChatMessagePO chatMessagePO = new ChatMessagePO();
        if (chatMessage.type().equals(ChatMessageType.AI)) {
            chatMessagePO.setSender(MessageSender.AI.getValue());
            AiMessage aiMessage = (AiMessage) chatMessage;
            chatMessagePO.setMessage(aiMessage.text());
        } else if (chatMessage.type().equals(ChatMessageType.USER)) {
            chatMessagePO.setSender(MessageSender.USER.getValue());
            UserMessage userMessage = (UserMessage) chatMessage;
            chatMessagePO.setMessage(userMessage.singleText());
        } else if (chatMessage.type().equals(ChatMessageType.SYSTEM)) {
            chatMessagePO.setSender(MessageSender.SYSTEM.getValue());
            SystemMessage systemMessage = (SystemMessage) chatMessage;
            chatMessagePO.setMessage(systemMessage.text());
        } else {
            chatMessagePO.setSender(chatMessage.type().toString());
        }
        ChatThreadPO chatThreadPO = chatThreadDao.findById(chatThreadId);
        chatMessagePO.setUserId(chatThreadPO.getUserId());
        chatMessagePO.setThreadId(chatThreadId);
        return chatMessagePO;
    }

    @Override
    public List<ChatMessage> getMessages(Object threadId) {
        List<ChatMessagePO> chatMessages = chatMessageDao.findAllByThreadId((Long) threadId);
        if (chatMessages.isEmpty()) {
            return new ArrayList<>();
        } else {
            return chatMessages.stream().map(this::convertToChatMessage).collect(Collectors.toList());
        }
    }

    @Override
    public void updateMessages(Object threadId, List<ChatMessage> messages) {
        if (threadId == null) {
            return;
        }
        ChatMessagePO chatMessagePO = convertToChatMessagePO(messages.get(messages.size() - 1), (Long) threadId);
        chatMessageDao.save(chatMessagePO);
    }

    @Override
    public void deleteMessages(Object threadId) {
        chatMessageDao.deleteByThreadId((Long) threadId);
    }
}
