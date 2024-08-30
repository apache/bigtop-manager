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
package org.apache.bigtop.manager.ai.assistant;

import org.apache.bigtop.manager.ai.assistant.provider.LocSystemPromptProvider;
import org.apache.bigtop.manager.ai.core.enums.SystemPrompt;
import org.apache.bigtop.manager.ai.core.provider.SystemPromptProvider;

import org.junit.jupiter.api.Test;

import dev.langchain4j.data.message.SystemMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SystemPromptProviderTests {

    private final SystemPromptProvider systemPromptProvider = new LocSystemPromptProvider();

    @Test
    public void loadSystemPromptByIdTest() {
        SystemMessage systemPrompt1 = systemPromptProvider.getSystemPrompt(SystemPrompt.BIGDATA_PROFESSOR);
        assertFalse(systemPrompt1.text().isEmpty());

        SystemMessage systemPrompt2 = systemPromptProvider.getSystemPrompt();
        assertFalse(systemPrompt2.text().isEmpty());

        assertEquals(systemPrompt1.text(), systemPrompt2.text());
    }
}
