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

import org.apache.bigtop.manager.server.model.vo.HostComponentVO;
import org.apache.bigtop.manager.server.service.HostComponentService;
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
class HostComponentControllerTest {

    @Mock
    private HostComponentService hostComponentService;

    @InjectMocks
    private HostComponentController hostComponentController;

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
    void listReturnsAllHostComponents() {
        Long clusterId = 1L;
        List<HostComponentVO> hostComponents = Arrays.asList(new HostComponentVO(), new HostComponentVO());
        when(hostComponentService.list(clusterId)).thenReturn(hostComponents);

        ResponseEntity<List<HostComponentVO>> response = hostComponentController.list(clusterId);

        assertTrue(response.isSuccess());
        assertEquals(hostComponents, response.getData());
    }

    @Test
    void listByHostReturnsHostComponentsForHost() {
        Long clusterId = 1L;
        Long hostId = 1L;
        List<HostComponentVO> hostComponents = Arrays.asList(new HostComponentVO(), new HostComponentVO());
        when(hostComponentService.listByHost(clusterId, hostId)).thenReturn(hostComponents);

        ResponseEntity<List<HostComponentVO>> response = hostComponentController.listByHost(clusterId, hostId);

        assertTrue(response.isSuccess());
        assertEquals(hostComponents, response.getData());
    }

    @Test
    void listByServiceReturnsHostComponentsForService() {
        Long clusterId = 1L;
        Long serviceId = 1L;
        List<HostComponentVO> hostComponents = Arrays.asList(new HostComponentVO(), new HostComponentVO());
        when(hostComponentService.listByService(clusterId, serviceId)).thenReturn(hostComponents);

        ResponseEntity<List<HostComponentVO>> response = hostComponentController.listByService(clusterId, serviceId);

        assertTrue(response.isSuccess());
        assertEquals(hostComponents, response.getData());
    }

    @Test
    void listReturnsEmptyForInvalidClusterId() {
        Long clusterId = 999L;
        when(hostComponentService.list(clusterId)).thenReturn(List.of());

        ResponseEntity<List<HostComponentVO>> response = hostComponentController.list(clusterId);

        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    void listByHostReturnsEmptyForInvalidHostId() {
        Long clusterId = 1L;
        Long hostId = 999L;
        when(hostComponentService.listByHost(clusterId, hostId)).thenReturn(List.of());

        ResponseEntity<List<HostComponentVO>> response = hostComponentController.listByHost(clusterId, hostId);

        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    void listByServiceReturnsEmptyForInvalidServiceId() {
        Long clusterId = 1L;
        Long serviceId = 999L;
        when(hostComponentService.listByService(clusterId, serviceId)).thenReturn(List.of());

        ResponseEntity<List<HostComponentVO>> response = hostComponentController.listByService(clusterId, serviceId);

        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty());
    }
}
