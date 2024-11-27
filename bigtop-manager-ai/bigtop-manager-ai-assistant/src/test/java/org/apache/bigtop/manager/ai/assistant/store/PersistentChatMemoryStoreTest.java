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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersistentChatMemoryStoreTest {

    @Mock
    private ChatThreadDao chatThreadDao;

    @Mock
    private ChatMessageDao chatMessageDao;

    @InjectMocks
    private PersistentChatMemoryStore persistentChatMemoryStore;

    @BeforeEach
    void setUp() {
        persistentChatMemoryStore = new PersistentChatMemoryStore(chatThreadDao, chatMessageDao);
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

        List<ChatMessage> result = persistentChatMemoryStore.getMessages(threadId);

        assertEquals(2, result.size());
        assertTrue(result.get(0) instanceof AiMessage);
        assertEquals("Hello from AI", ((AiMessage) result.get(0)).text());
    }

    @Test
    void testUpdateMessages() {
        Long threadId = 1L;
        ChatThreadPO chatThreadPO = new ChatThreadPO();
        chatThreadPO.setUserId(123L);

        when(chatThreadDao.findById(threadId)).thenReturn(chatThreadPO);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new SystemMessage("Hello System"));
        persistentChatMemoryStore.updateMessages(threadId, messages);
        messages.add(new UserMessage("Hello User"));
        persistentChatMemoryStore.updateMessages(threadId, messages);
        messages.add(new AiMessage("Hello AI"));
        persistentChatMemoryStore.updateMessages(threadId, messages);

        ChatMessage mockMessage = mock(ChatMessage.class);
        when(mockMessage.type()).thenReturn(ChatMessageType.TOOL_EXECUTION_RESULT);
        messages.add(mockMessage);
        persistentChatMemoryStore.updateMessages(threadId, messages);
    }

    @Test
    void testDeleteMessages() {
        Long threadId = 1L;
        List<ChatMessagePO> chatMessagePOS = new ArrayList<>();

        ChatMessagePO messagePO = new ChatMessagePO();
        messagePO.setIsDeleted(false);
        chatMessagePOS.add(messagePO);

        when(chatMessageDao.findAllByThreadId(threadId)).thenReturn(chatMessagePOS);

        persistentChatMemoryStore.deleteMessages(threadId);

        assertTrue(chatMessagePOS.get(0).getIsDeleted());
    }

    @Test
    void testSystemMessage() {
        Long threadId = 1L;

        when(chatMessageDao.findAllByThreadId(threadId)).thenReturn(new ArrayList<>());
        persistentChatMemoryStore.updateMessages(threadId, List.of(new SystemMessage("Hello from System")));
        List<ChatMessage> result = persistentChatMemoryStore.getMessages(threadId);

        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof SystemMessage);
        assertEquals("Hello from System", ((SystemMessage) result.get(0)).text());
    }
}
