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
package org.apache.bigtop.manager.ai.core.factory;

import org.apache.bigtop.manager.ai.core.config.AIAssistantConfig;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import reactor.core.publisher.Flux;

public interface AIAssistant {

    /**
     * This ID is the unique identifier for the {@link AIAssistant}.
     * Its memory storage is independent of AIAssistant instances in other threads.
     * @return
     */
    Object getId();

    /**
     * This is a conversation based on streaming output.
     * @param userMessage
     * @return
     */
    Flux<String> streamAsk(String userMessage);

    /**
     * This is a conversation based on blocking output.
     * @param userMessage
     * @return
     */
    String ask(String userMessage);

    /**
     * This is used to get the AIAssistant's Platform
     * @return
     */
    PlatformType getPlatform();

    /**
     * This is used to test whether the configuration is correct
     * @return
     */
    boolean test();

    interface Service {
        String chat(String userMessage);

        Flux<String> streamChat(String userMessage);
    }

    interface Builder {
        Builder id(Object id);

        Builder memoryStore(ChatMemoryStore memoryStore);

        Builder withConfig(AIAssistantConfig configProvider);

        Builder withToolProvider(ToolProvider toolProvider);

        Builder withSystemPrompt(String systemPrompt);

        AIAssistant build();

        ChatModel getChatModel();

        StreamingChatModel getStreamingChatModel();

        ChatMemory getChatMemory();
    }
}
