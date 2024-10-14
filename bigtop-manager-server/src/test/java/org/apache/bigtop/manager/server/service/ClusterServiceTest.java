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

package org.apache.bigtop.manager.server.service;

import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.StackPO;
import org.apache.bigtop.manager.dao.repository.ClusterDao;
import org.apache.bigtop.manager.dao.repository.StackDao;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.model.dto.ClusterDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.StackDTO;
import org.apache.bigtop.manager.server.service.impl.ClusterServiceImpl;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.apache.commons.lang3.tuple.ImmutablePair;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClusterServiceTest {

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private StackDao stackDao;

    @InjectMocks
    private ClusterServiceImpl clusterService = new ClusterServiceImpl();

    @Mock
    private HostService hostService;

    private ClusterPO clusterPO;
    private StackDTO stackDTO;
    private ClusterDTO clusterDTO;
    Map<String, ImmutablePair<StackDTO, List<ServiceDTO>>> mockStackKeyMap = new HashMap<>();

    @BeforeEach
    public void setup() {
        clusterPO = new ClusterPO();
        clusterPO.setId(1L);
        clusterPO.setClusterName("TestCluster");

        clusterDTO = new ClusterDTO();
        clusterDTO.setClusterName("TestCluster");
        clusterDTO.setStackName("TestStack");
        clusterDTO.setStackVersion("1.0.0");

        stackDTO = new StackDTO();
        stackDTO.setStackName("TestStack");
        mockStackKeyMap.put(
                StackUtils.fullStackName(clusterDTO.getStackName(), clusterDTO.getStackVersion()),
                new ImmutablePair<>(stackDTO, new ArrayList<>()));
    }

    @Test
    public void testListAndGetAndUpdate() {
        when(clusterDao.findAllByJoin()).thenReturn(List.of(clusterPO));
        assert clusterService.list().size() == 1;

        assertEquals(
                ApiExceptionEnum.CLUSTER_NOT_FOUND,
                assertThrows(ApiException.class, () -> clusterService.get(1L)).getEx());

        when(clusterDao.findByIdJoin(any())).thenReturn(clusterPO);
        assert clusterService.get(1L).getClusterName().equals("TestCluster");

        ClusterDTO clusterDTO = new ClusterDTO();
        clusterDTO.setClusterName("TestCluster");
        assert clusterService.update(1L, clusterDTO).getClusterName().equals("TestCluster");
    }

    @Test
    public void testSave() {
        when(stackDao.findByStackNameAndStackVersion(any(), any())).thenReturn(new StackPO());
        when(stackDao.findAll()).thenReturn(List.of(new StackPO()));
        when(hostService.batchSave(any(), any())).thenReturn(null);
        try (MockedStatic<StackUtils> mockedStackUtils = mockStatic(StackUtils.class, CALLS_REAL_METHODS)) {
            mockedStackUtils.when(StackUtils::getStackKeyMap).thenReturn(mockStackKeyMap);
            clusterService.save(clusterDTO);
        }
    }
}
