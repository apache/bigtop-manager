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
import org.apache.bigtop.manager.ai.assistant.store.PersistentRepository;
import org.apache.bigtop.manager.dao.repository.ChatMessageDao;
import org.apache.bigtop.manager.dao.repository.ChatThreadDao;

import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersistentProvider {
    private final ChatThreadDao chatThreadDao;
    private final ChatMessageDao chatMessageDao;

    public PersistentProvider(ChatThreadDao chatThreadDao, ChatMessageDao chatMessageDao) {
        this.chatThreadDao = chatThreadDao;
        this.chatMessageDao = chatMessageDao;
    }

    public PersistentProvider() {
        chatMessageDao = null;
        chatThreadDao = null;
    }

    public PersistentRepository getPersistentRepository() {
        if (chatThreadDao == null) {
            return new PersistentRepository();
        }
        return new PersistentRepository(chatThreadDao, chatMessageDao);
    }

    public ChatMemoryStore getChatMemoryStore() {
        return new PersistentChatMemoryStore(chatThreadDao, chatMessageDao);
    }
}
