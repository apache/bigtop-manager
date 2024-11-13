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
import org.apache.bigtop.manager.dao.repository.ChatMessageDao;
import org.apache.bigtop.manager.dao.repository.ChatThreadDao;

import dev.langchain4j.data.message.ChatMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AiServiceChatMemoryStore extends PersistentChatMemoryStore {
    private final Map<Object, List<ChatMessage>> messagesByMemoryId = new ConcurrentHashMap();

    public AiServiceChatMemoryStore(ChatThreadDao chatThreadDao, ChatMessageDao chatMessageDao) {
        super(chatThreadDao, chatMessageDao);
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        return this.messagesByMemoryId.computeIfAbsent(memoryId, (ignored) -> new ArrayList<>());
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        this.messagesByMemoryId.put(memoryId, messages);
        ChatMessage newMessage = messages.get(messages.size() - 1);
        ChatMessagePO chatMessagePO = convertToChatMessagePO(newMessage, (Long) memoryId);
        if (chatMessagePO == null) {
            return;
        }
        chatMessageDao.save(chatMessagePO);
    }
}
