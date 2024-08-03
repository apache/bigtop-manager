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

import org.apache.bigtop.manager.server.model.vo.ServiceConfigVO;
import org.apache.bigtop.manager.server.service.ConfigService;
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
class ConfigControllerTest {

    @Mock
    private ConfigService configService;

    @InjectMocks
    private ConfigController configController;

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
    void listReturnsAllConfigurations() {
        Long clusterId = 1L;
        List<ServiceConfigVO> configs = Arrays.asList(new ServiceConfigVO(), new ServiceConfigVO());
        when(configService.list(clusterId)).thenReturn(configs);

        ResponseEntity<List<ServiceConfigVO>> response = configController.list(clusterId);

        assertTrue(response.isSuccess());
        assertEquals(configs, response.getData());
    }

    @Test
    void latestReturnsLatestConfigurations() {
        Long clusterId = 1L;
        List<ServiceConfigVO> latestConfigs = Arrays.asList(new ServiceConfigVO(), new ServiceConfigVO());
        when(configService.latest(clusterId)).thenReturn(latestConfigs);

        ResponseEntity<List<ServiceConfigVO>> response = configController.latest(clusterId);

        assertTrue(response.isSuccess());
        assertEquals(latestConfigs, response.getData());
    }

    @Test
    void listReturnsEmptyForInvalidClusterId() {
        Long clusterId = 999L;
        when(configService.list(clusterId)).thenReturn(List.of());

        ResponseEntity<List<ServiceConfigVO>> response = configController.list(clusterId);

        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    void latestReturnsEmptyForInvalidClusterId() {
        Long clusterId = 999L;
        when(configService.latest(clusterId)).thenReturn(List.of());

        ResponseEntity<List<ServiceConfigVO>> response = configController.latest(clusterId);

        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty());
    }
}
