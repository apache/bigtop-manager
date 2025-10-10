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

import org.apache.bigtop.manager.server.model.vo.ClusterMetricsVO;
import org.apache.bigtop.manager.server.model.vo.HostMetricsVO;
import org.apache.bigtop.manager.server.service.MetricsService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricsControllerTest {

    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private MetricsController metricsController;

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
    void testQueryAgentInfo() {
        Long hostId = 1L;
        String interval = "1m";
        HostMetricsVO mockResponse = new HostMetricsVO();
        mockResponse.setCpuUsageCur("50%");
        mockResponse.setMemoryUsageCur("70%");

        when(metricsService.hostMetrics(hostId, interval)).thenReturn(mockResponse);

        ResponseEntity<HostMetricsVO> response = metricsController.hostMetrics(interval, hostId);

        assertEquals("Mocked message", response.getMessage());
        assertTrue(response.isSuccess());
        assertEquals(mockResponse, response.getData());
    }

    @Test
    void testQueryClusterInfo() {
        Long clusterId = 1L;
        String interval = "1m";
        ClusterMetricsVO mockResponse = new ClusterMetricsVO();
        mockResponse.setCpuUsageCur("60%");
        mockResponse.setMemoryUsageCur("80%");

        when(metricsService.clusterMetrics(clusterId, interval)).thenReturn(mockResponse);

        ResponseEntity<ClusterMetricsVO> response = metricsController.clusterMetrics(interval, clusterId);

        assertEquals("Mocked message", response.getMessage());
        assertTrue(response.isSuccess());
        assertEquals(mockResponse, response.getData());
    }
}
