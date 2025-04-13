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

import org.apache.bigtop.manager.server.model.req.ClusterReq;
import org.apache.bigtop.manager.server.model.vo.ClusterVO;
import org.apache.bigtop.manager.server.service.ClusterService;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClusterControllerTest {

    @Mock
    private ClusterService clusterService;

    @InjectMocks
    private ClusterController clusterController;

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
    void listReturnsAllClusters() {
        List<ClusterVO> clusters = Arrays.asList(new ClusterVO(), new ClusterVO());
        when(clusterService.list()).thenReturn(clusters);

        ResponseEntity<List<ClusterVO>> response = clusterController.list();

        assertTrue(response.isSuccess());
        assertEquals(clusters, response.getData());
    }

    @Test
    void getReturnsClusterById() {
        Long id = 1L;
        ClusterVO cluster = new ClusterVO();
        when(clusterService.get(id)).thenReturn(cluster);

        ResponseEntity<ClusterVO> response = clusterController.get(id);

        assertTrue(response.isSuccess());
        assertEquals(cluster, response.getData());
    }

    @Test
    void updateModifiesCluster() {
        Long id = 1L;
        ClusterReq clusterReq = new ClusterReq();
        ClusterVO updatedCluster = new ClusterVO();
        when(clusterService.update(eq(id), any())).thenReturn(updatedCluster);

        ResponseEntity<ClusterVO> response = clusterController.update(id, clusterReq);

        assertTrue(response.isSuccess());
        assertEquals(updatedCluster, response.getData());
    }

    @Test
    void getReturnsNotFoundForInvalidId() {
        Long id = 999L;
        when(clusterService.get(id)).thenReturn(null);

        ResponseEntity<ClusterVO> response = clusterController.get(id);

        assertTrue(response.isSuccess());
        assertNull(response.getData());
    }
}
