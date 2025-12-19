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

import org.apache.bigtop.manager.ai.core.enums.MessageType;
import org.apache.bigtop.manager.dao.po.ChatMessagePO;
import org.apache.bigtop.manager.dao.po.ChatThreadPO;
import org.apache.bigtop.manager.dao.repository.ChatMessageDao;
import org.apache.bigtop.manager.dao.repository.ChatThreadDao;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class PersistentChatMemoryStore implements ChatMemoryRepository {

    private final List<Message> messagesInMemory = new ArrayList<>();
    private final ChatThreadDao chatThreadDao;
    private final ChatMessageDao chatMessageDao;
    private final Long conversationId;

    public PersistentChatMemoryStore(Long conversationId, ChatThreadDao chatThreadDao, ChatMessageDao chatMessageDao) {
        this.conversationId = conversationId;
        this.chatThreadDao = chatThreadDao;
        this.chatMessageDao = chatMessageDao;
    }

    private Message convertToChatMessage(ChatMessagePO chatMessagePO) {
        String sender = chatMessagePO.getSender().toLowerCase();
        if (sender.equals(MessageType.AI.getValue())) {
            return new AssistantMessage(chatMessagePO.getMessage());
        } else if (sender.equals(MessageType.USER.getValue())) {
            return new UserMessage(chatMessagePO.getMessage());
        } else if (sender.equals(MessageType.SYSTEM.getValue())) {
            return new SystemMessage(chatMessagePO.getMessage());
        } else {
            return null;
        }
    }

    private ChatMessagePO convertToChatMessagePO(Message message, Long chatThreadId) {
        ChatMessagePO chatMessagePO = new ChatMessagePO();
        if (message.getMessageType() == org.springframework.ai.chat.messages.MessageType.ASSISTANT) {
            chatMessagePO.setSender(MessageType.AI.getValue());
            AssistantMessage assistantMessage = (AssistantMessage) message;
            if (assistantMessage.getText() == null) {
                return null;
            }
            chatMessagePO.setMessage(assistantMessage.getText());
        } else if (message.getMessageType() == org.springframework.ai.chat.messages.MessageType.USER) {
            chatMessagePO.setSender(MessageType.USER.getValue());
            UserMessage userMessage = (UserMessage) message;
            chatMessagePO.setMessage(userMessage.getText());
        } else if (message.getMessageType() == org.springframework.ai.chat.messages.MessageType.SYSTEM) {
            chatMessagePO.setSender(MessageType.SYSTEM.getValue());
            SystemMessage systemMessage = (SystemMessage) message;
            chatMessagePO.setMessage(systemMessage.getText());
        } else {
            return null;
        }
        ChatThreadPO chatThreadPO = chatThreadDao.findById(chatThreadId);
        chatMessagePO.setUserId(chatThreadPO.getUserId());
        chatMessagePO.setThreadId(chatThreadId);
        return chatMessagePO;
    }

    private List<Message> sortMessages(List<Message> messages) {
        List<Message> systemMessages = messages.stream()
                .filter(message -> message instanceof SystemMessage)
                .collect(Collectors.toList());
        List<Message> otherMessages = messages.stream()
                .filter(message -> !(message instanceof SystemMessage))
                .toList();

        systemMessages.addAll(otherMessages);
        return systemMessages;
    }

    @Override
    public List<String> findConversationIds() {
        // Return the current conversation ID as a list
        return List.of(String.valueOf(conversationId));
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        List<ChatMessagePO> chatMessages = chatMessageDao.findAllByThreadId(this.conversationId);
        List<Message> allChatMessages = new ArrayList<>();
        if (!chatMessages.isEmpty()) {
            allChatMessages.addAll(chatMessages.stream()
                    .map(this::convertToChatMessage)
                    .filter(Objects::nonNull)
                    .toList());
        }

        allChatMessages.addAll(messagesInMemory);

        return sortMessages(allChatMessages);
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        for (Message message : messages) {
            ChatMessagePO chatMessagePO = convertToChatMessagePO(message, this.conversationId);
            if (chatMessagePO == null) {
                messagesInMemory.add(message);
                continue;
            }
            chatMessageDao.save(chatMessagePO);
        }
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        List<ChatMessagePO> chatMessagePOS = chatMessageDao.findAllByThreadId(this.conversationId);
        chatMessagePOS.forEach(chatMessage -> chatMessage.setIsDeleted(true));
        chatMessageDao.partialUpdateByIds(chatMessagePOS);
        messagesInMemory.clear();
    }
}
