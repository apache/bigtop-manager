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
import org.apache.bigtop.manager.dao.repository.ChatMessageDao;
import org.apache.bigtop.manager.dao.repository.ChatThreadDao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersistentChatMemoryStoreTest {

    @Mock
    private ChatThreadDao chatThreadDao;

    @Mock
    private ChatMessageDao chatMessageDao;

    private PersistentChatMemoryStore persistentChatMemoryStore;

    @BeforeEach
    void setUp() {
        Long threadId = 1L;
        persistentChatMemoryStore = new PersistentChatMemoryStore(threadId, chatThreadDao, chatMessageDao);
    }

    @Test
    void testGetMessages() {
        Long threadId = 1L;
        List<ChatMessagePO> chatMessagePOS = new ArrayList<>();

        ChatMessagePO messagePO = new ChatMessagePO();
        messagePO.setSender("ai");
        messagePO.setMessage("Hello from AI");
        chatMessagePOS.add(messagePO);
        ChatMessagePO messagePO2 = new ChatMessagePO();
        messagePO2.setSender("user");
        messagePO2.setMessage("Hello from User");
        chatMessagePOS.add(messagePO2);
        ChatMessagePO messagePO3 = new ChatMessagePO();
        messagePO3.setSender("other");
        messagePO3.setMessage("Hello from System");
        chatMessagePOS.add(messagePO3);
        when(chatMessageDao.findAllByThreadId(threadId)).thenReturn(chatMessagePOS);

        List<Message> result = persistentChatMemoryStore.findByConversationId(String.valueOf(threadId));

        assertEquals(2, result.size());
        assertTrue(result.get(0) instanceof AssistantMessage);
        assertEquals("Hello from AI", ((AssistantMessage) result.get(0)).getText());
    }

    @Test
    void testAddMessages() {
        Long threadId = 1L;
        ChatThreadPO chatThreadPO = new ChatThreadPO();
        chatThreadPO.setUserId(123L);

        when(chatThreadDao.findById(threadId)).thenReturn(chatThreadPO);

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("Hello System"));
        persistentChatMemoryStore.saveAll(String.valueOf(threadId), messages);
        messages.clear();
        messages.add(new UserMessage("Hello User"));
        persistentChatMemoryStore.saveAll(String.valueOf(threadId), messages);
        messages.clear();
        messages.add(new AssistantMessage("Hello AI"));
        persistentChatMemoryStore.saveAll(String.valueOf(threadId), messages);
    }

    @Test
    void testClearMessages() {
        Long threadId = 1L;
        List<ChatMessagePO> chatMessagePOS = new ArrayList<>();

        ChatMessagePO messagePO = new ChatMessagePO();
        messagePO.setIsDeleted(false);
        chatMessagePOS.add(messagePO);

        when(chatMessageDao.findAllByThreadId(threadId)).thenReturn(chatMessagePOS);

        persistentChatMemoryStore.deleteByConversationId(String.valueOf(threadId));

        Assertions.assertTrue(chatMessagePOS.get(0).getIsDeleted());
    }

    @Test
    void testSystemMessage() {
        Long threadId = 1L;

        ChatMessagePO systemMessagePO = new ChatMessagePO();
        systemMessagePO.setSender("system");
        systemMessagePO.setMessage("Hello from System");

        when(chatMessageDao.findAllByThreadId(threadId)).thenReturn(List.of(systemMessagePO));

        List<Message> result = persistentChatMemoryStore.findByConversationId(String.valueOf(threadId));

        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof SystemMessage);
        assertEquals("Hello from System", ((SystemMessage) result.get(0)).getText());
    }
}
