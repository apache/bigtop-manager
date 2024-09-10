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
package org.apache.bigtop.manager.ai.assistant.provider;

import org.apache.bigtop.manager.ai.assistant.store.PersistentChatMemoryStore;
import org.apache.bigtop.manager.ai.assistant.store.PersistentMessageRepository;
import org.apache.bigtop.manager.ai.core.repository.MessageRepository;
import org.apache.bigtop.manager.dao.repository.ChatMessageDao;
import org.apache.bigtop.manager.dao.repository.ChatThreadDao;

import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersistentStoreProvider {
    private final ChatThreadDao chatThreadDao;
    private final ChatMessageDao chatMessageDao;

    public PersistentStoreProvider(ChatThreadDao chatThreadDao, ChatMessageDao chatMessageDao) {
        this.chatThreadDao = chatThreadDao;
        this.chatMessageDao = chatMessageDao;
    }

    public PersistentStoreProvider() {
        chatMessageDao = null;
        chatThreadDao = null;
    }

    public MessageRepository getPersistentRepository() {
        return new PersistentMessageRepository(chatThreadDao, chatMessageDao);
    }

    public ChatMemoryStore getChatMemoryStore() {
        if (chatThreadDao == null) {
            return new InMemoryChatMemoryStore();
        }
        return new PersistentChatMemoryStore(chatThreadDao, chatMessageDao);
    }
}
