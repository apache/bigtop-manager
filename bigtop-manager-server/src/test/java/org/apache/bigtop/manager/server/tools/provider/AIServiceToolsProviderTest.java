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
package org.apache.bigtop.manager.server.tools.provider;

import org.apache.bigtop.manager.server.enums.ChatbotCommand;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.langchain4j.service.tool.ToolProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class AIServiceToolsProviderTest {

    @Mock
    private InfoToolsProvider infoToolsProvider;

    @InjectMocks
    private AIServiceToolsProvider aiServiceToolsProvider;

    @Test
    public void testGetToolsProvideWithInfoCommand() {
        ToolProvider toolProvider = aiServiceToolsProvider.getToolsProvide(ChatbotCommand.INFO);
        assertNotNull(toolProvider);
        assertEquals(infoToolsProvider, toolProvider);
    }

    @Test
    public void testGetToolsProvideWithOtherCommand() {
        ToolProvider toolProvider = aiServiceToolsProvider.getToolsProvide(ChatbotCommand.HELP);
        assertNull(toolProvider);
    }

    @Test
    public void testGetToolsProvideWithNullCommand() {
        ToolProvider toolProvider = aiServiceToolsProvider.getToolsProvide(null);
        assertNull(toolProvider);
    }
}
