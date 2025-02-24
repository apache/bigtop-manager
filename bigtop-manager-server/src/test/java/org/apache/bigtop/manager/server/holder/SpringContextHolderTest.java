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
package org.apache.bigtop.manager.server.holder;

import org.apache.bigtop.manager.server.command.factory.JobFactory;
import org.apache.bigtop.manager.server.command.validator.CommandValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SpringContextHolderTest {

    private ApplicationContext applicationContext;
    private SpringContextHolder springContextHolder;

    @BeforeEach
    public void setUp() {
        applicationContext = mock(ApplicationContext.class);
        springContextHolder = new SpringContextHolder();
        springContextHolder.setApplicationContext(applicationContext);
    }

    @Test
    public void testGetBean() {
        CommandValidator commandValidatorMock = mock(CommandValidator.class);
        when(applicationContext.getBean(CommandValidator.class)).thenReturn(commandValidatorMock);

        CommandValidator result = springContextHolder.getBean(CommandValidator.class);
        assertNotNull(result);
        assertEquals(commandValidatorMock, result);
    }

    @Test
    public void testGetCommandValidators() {
        CommandValidator commandValidatorMock = mock(CommandValidator.class);
        Map<String, CommandValidator> commandValidatorMap = new HashMap<>();
        commandValidatorMap.put("commandValidator1", commandValidatorMock);
        when(applicationContext.getBeansOfType(CommandValidator.class)).thenReturn(commandValidatorMap);

        Map<String, CommandValidator> result = springContextHolder.getCommandValidators();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(commandValidatorMock, result.get("commandValidator1"));
    }

    @Test
    public void testGetJobFactories() {
        JobFactory jobFactoryMock = mock(JobFactory.class);
        Map<String, JobFactory> jobFactoryMap = new HashMap<>();
        jobFactoryMap.put("jobFactory1", jobFactoryMock);
        when(applicationContext.getBeansOfType(JobFactory.class)).thenReturn(jobFactoryMap);

        Map<String, JobFactory> result = springContextHolder.getJobFactories();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(jobFactoryMock, result.get("jobFactory1"));
    }
}
