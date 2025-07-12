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
import org.apache.bigtop.manager.dao.repository.ClusterDao;
import org.apache.bigtop.manager.dao.repository.ServiceDao;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.model.dto.ClusterDTO;
import org.apache.bigtop.manager.server.service.impl.ClusterServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClusterServiceTest {
    private static final String CLUSTER_NAME = "TestCluster";

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private ServiceDao serviceDao;

    @InjectMocks
    private ClusterService clusterService = new ClusterServiceImpl();

    private ClusterPO clusterPO;

    @BeforeEach
    public void setup() {
        clusterPO = new ClusterPO();
        clusterPO.setId(1L);
        clusterPO.setName(CLUSTER_NAME);
    }

    @Test
    public void testListAndGetAndUpdate() {
        when(clusterDao.findAll()).thenReturn(List.of(clusterPO));
        assert clusterService.list().size() == 1;

        assertEquals(
                ApiExceptionEnum.CLUSTER_NOT_FOUND,
                assertThrows(ApiException.class, () -> clusterService.get(1L)).getEx());

        when(clusterDao.findDetailsById(any())).thenReturn(clusterPO);
        when(serviceDao.countByClusterId(any())).thenReturn(1);
        assert clusterService.get(1L).getName().equals(CLUSTER_NAME);

        ClusterDTO clusterDTO = new ClusterDTO();
        clusterDTO.setName(CLUSTER_NAME);
        assert clusterService.update(1L, clusterDTO).getName().equals(CLUSTER_NAME);
    }
}
