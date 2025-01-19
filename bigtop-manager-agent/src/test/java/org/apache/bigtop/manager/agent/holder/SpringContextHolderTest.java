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
package org.apache.bigtop.manager.agent.holder;

import org.apache.bigtop.manager.agent.executor.CommandExecutor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SpringContextHolderTest {

    @Mock
    private ApplicationContext mockApplicationContext;

    @Mock
    private CommandExecutor mockCommandExecutor;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Use reflection to set the static variable applicationContext
        Field field = SpringContextHolder.class.getDeclaredField("applicationContext");
        field.setAccessible(true);
        field.set(null, mockApplicationContext);
    }

    @Test
    public void testGetCommandExecutors() {
        // Prepare test data
        Map<String, CommandExecutor> commandExecutorsMap = new HashMap<>();
        commandExecutorsMap.put("commandExecutor1", mockCommandExecutor);
        when(mockApplicationContext.getBeansOfType(CommandExecutor.class)).thenReturn(commandExecutorsMap);

        // Execute the method under test
        Map<String, CommandExecutor> result = SpringContextHolder.getCommandExecutors();

        // Validate the result
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("commandExecutor1"));
        assertSame(mockCommandExecutor, result.get("commandExecutor1"));

        // Verify method calls
        verify(mockApplicationContext, times(1)).getBeansOfType(CommandExecutor.class);
    }
}
