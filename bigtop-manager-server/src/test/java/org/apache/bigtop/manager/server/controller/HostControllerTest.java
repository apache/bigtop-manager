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

import org.apache.bigtop.manager.server.model.dto.HostDTO;
import org.apache.bigtop.manager.server.model.req.HostReq;
import org.apache.bigtop.manager.server.model.req.HostnamesReq;
import org.apache.bigtop.manager.server.model.vo.HostVO;
import org.apache.bigtop.manager.server.service.HostService;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HostControllerTest {

    @Mock
    private HostService hostService;

    @InjectMocks
    private HostController hostController;

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
    void listReturnsAllHosts() {
        Long clusterId = 1L;
        List<HostVO> hosts = Arrays.asList(new HostVO(), new HostVO());
        when(hostService.list(clusterId)).thenReturn(hosts);

        ResponseEntity<List<HostVO>> response = hostController.list(clusterId);

        assertTrue(response.isSuccess());
        assertEquals(hosts, response.getData());
    }

    @Test
    void getReturnsHost() {
        Long clusterId = 1L;
        Long hostId = 1L;
        HostVO host = new HostVO();
        when(hostService.get(hostId)).thenReturn(host);

        ResponseEntity<HostVO> response = hostController.get(hostId, clusterId);

        assertTrue(response.isSuccess());
        assertEquals(host, response.getData());
    }

    @Test
    void updateReturnsUpdatedHost() {
        Long clusterId = 1L;
        Long hostId = 1L;
        HostReq hostReq = new HostReq();
        HostVO updatedHost = new HostVO();
        when(hostService.update(anyLong(), any(HostDTO.class))).thenReturn(updatedHost);

        ResponseEntity<HostVO> response = hostController.update(clusterId, hostId, hostReq);

        assertTrue(response.isSuccess());
        assertEquals(updatedHost, response.getData());
    }

    @Test
    void deleteReturnsSuccess() {
        Long clusterId = 1L;
        Long hostId = 1L;
        when(hostService.delete(hostId)).thenReturn(true);

        ResponseEntity<Boolean> response = hostController.delete(clusterId, hostId);

        assertTrue(response.isSuccess());
        assertTrue(response.getData());
    }

    @Test
    void checkConnectionReturnsSuccess() {
        Long clusterId = 1L;
        HostnamesReq hostnamesReq = new HostnamesReq();
        hostnamesReq.setHostnames(Arrays.asList("host1", "host2"));
        when(hostService.checkConnection(hostnamesReq.getHostnames())).thenReturn(true);

        ResponseEntity<Boolean> response = hostController.checkConnection(clusterId, hostnamesReq);

        assertTrue(response.isSuccess());
        assertTrue(response.getData());
    }

    @Test
    void listReturnsEmptyForInvalidClusterId() {
        Long clusterId = 999L;
        when(hostService.list(clusterId)).thenReturn(List.of());

        ResponseEntity<List<HostVO>> response = hostController.list(clusterId);

        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    void getReturnsNullForInvalidHostId() {
        Long clusterId = 1L;
        Long hostId = 999L;
        when(hostService.get(hostId)).thenReturn(null);

        ResponseEntity<HostVO> response = hostController.get(hostId, clusterId);

        assertTrue(response.isSuccess());
        assertNull(response.getData());
    }

    @Test
    void updateReturnsNullForInvalidHostId() {
        Long clusterId = 1L;
        Long hostId = 999L;
        HostReq hostReq = new HostReq();
        when(hostService.update(anyLong(), any(HostDTO.class))).thenReturn(null);

        ResponseEntity<HostVO> response = hostController.update(clusterId, hostId, hostReq);

        assertTrue(response.isSuccess());
        assertNull(response.getData());
    }

    @Test
    void deleteReturnsFalseForInvalidHostId() {
        Long clusterId = 1L;
        Long hostId = 999L;
        when(hostService.delete(hostId)).thenReturn(false);

        ResponseEntity<Boolean> response = hostController.delete(clusterId, hostId);

        assertTrue(response.isSuccess());
        assertFalse(response.getData());
    }

    @Test
    void checkConnectionReturnsFalseForInvalidHostnames() {
        Long clusterId = 1L;
        HostnamesReq hostnamesReq = new HostnamesReq();
        hostnamesReq.setHostnames(Arrays.asList("invalidHost1", "invalidHost2"));
        when(hostService.checkConnection(hostnamesReq.getHostnames())).thenReturn(false);

        ResponseEntity<Boolean> response = hostController.checkConnection(clusterId, hostnamesReq);

        assertTrue(response.isSuccess());
        assertFalse(response.getData());
    }
}
