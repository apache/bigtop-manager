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
package org.apache.bigtop.manager.server.controller;

import org.apache.bigtop.manager.server.model.vo.ServiceComponentVO;
import org.apache.bigtop.manager.server.model.vo.ServiceConfigVO;
import org.apache.bigtop.manager.server.model.vo.StackVO;
import org.apache.bigtop.manager.server.service.StackService;
import org.apache.bigtop.manager.server.utils.MessageSourceUtils;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StackControllerTest {

    @Mock
    private StackService stackService;

    @InjectMocks
    private StackController stackController;

    private MockedStatic<MessageSourceUtils> mockedMessageSourceUtils;

    @BeforeEach
    void setUp() {
        mockedMessageSourceUtils = Mockito.mockStatic(MessageSourceUtils.class);
        when(MessageSourceUtils.getMessage(any())).thenReturn("Mocked message");
    }

    @AfterEach
    void tearDown() {
        mockedMessageSourceUtils.close();
    }

    @Test
    void listReturnsAllStacks() {
        List<StackVO> stacks = Arrays.asList(new StackVO(), new StackVO());
        when(stackService.list()).thenReturn(stacks);

        ResponseEntity<List<StackVO>> response = stackController.list();

        assertTrue(response.isSuccess());
        assertEquals(stacks, response.getData());
    }

    @Test
    void componentsReturnsAllComponentsForValidStack() {
        String stackName = "bigtop";
        String stackVersion = "1.0.0";
        List<ServiceComponentVO> components = Arrays.asList(new ServiceComponentVO(), new ServiceComponentVO());
        when(stackService.components(stackName, stackVersion)).thenReturn(components);

        ResponseEntity<List<ServiceComponentVO>> response = stackController.components(stackName, stackVersion);

        assertTrue(response.isSuccess());
        assertEquals(components, response.getData());
    }

    @Test
    void configurationsReturnsAllConfigurationsForValidStack() {
        String stackName = "bigtop";
        String stackVersion = "1.0.0";
        List<ServiceConfigVO> configurations = Arrays.asList(new ServiceConfigVO(), new ServiceConfigVO());
        when(stackService.configurations(stackName, stackVersion)).thenReturn(configurations);

        ResponseEntity<List<ServiceConfigVO>> response = stackController.configurations(stackName, stackVersion);

        assertTrue(response.isSuccess());
        assertEquals(configurations, response.getData());
    }
}
