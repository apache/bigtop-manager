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

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
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
    Flux<String> streamAsk(ChatMessage userMessage);

    /**
     * This is a conversation based on blocking output.
     * @param userMessage
     * @return
     */
    String ask(ChatMessage userMessage);

    /**
     * This is primarily used to retrieve the AI assistant's history of chat conversations.
     * @return
     */
    ChatMemory getMemory();

    /**
     * This is used to get the AIAssistant's Platform
     * @return
     */
    String getPlatform();

    void setSystemPrompt(SystemMessage systemPrompt);

    void resetMemory();

    default Flux<String> streamAsk(String message) {
        return streamAsk(UserMessage.from(message));
    }

    default String ask(String message) {
        return ask(UserMessage.from(message));
    }

    default void setSystemPrompt(String systemPrompt) {
        setSystemPrompt(SystemMessage.systemMessage(systemPrompt));
    }
}
