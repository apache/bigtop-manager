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
import org.apache.bigtop.manager.dao.po.ServiceConfigPO;
import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.dao.po.TypeConfigPO;
import org.apache.bigtop.manager.dao.repository.ClusterDao;
import org.apache.bigtop.manager.dao.repository.ServiceConfigDao;
import org.apache.bigtop.manager.dao.repository.ServiceDao;
import org.apache.bigtop.manager.dao.repository.TypeConfigDao;
import org.apache.bigtop.manager.server.model.dto.PropertyDTO;
import org.apache.bigtop.manager.server.model.dto.TypeConfigDTO;
import org.apache.bigtop.manager.server.service.impl.ConfigServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConfigServiceTest {

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private ServiceDao serviceDao;

    @Mock
    private ServiceConfigDao serviceConfigDao;

    @Mock
    private TypeConfigDao typeConfigDao;

    @InjectMocks
    private ConfigService configService = new ConfigServiceImpl();

    @Test
    public void testListAndLatest() {
        configService.list(1L);
        configService.latest(1L);
    }

    @Test
    public void testUpsert() {
        when(clusterDao.findById(1L)).thenReturn(new ClusterPO());
        when(serviceDao.findById(1L)).thenReturn(new ServicePO());
        TypeConfigDTO typeConfigDTO = new TypeConfigDTO();
        typeConfigDTO.setTypeName("test");
        PropertyDTO propertyDTO = new PropertyDTO();
        propertyDTO.setName("test");
        typeConfigDTO.setProperties(List.of(propertyDTO));
        configService.upsert(1L, 1L, List.of(typeConfigDTO));

        ServiceConfigPO serviceConfigPO = new ServiceConfigPO();
        TypeConfigPO typeConfigPO = new TypeConfigPO();
        typeConfigPO.setTypeName("test");
        typeConfigPO.setPropertiesJson("[]");
        serviceConfigPO.setConfigs(List.of(typeConfigPO));
        serviceConfigPO.setVersion(1);
        when(serviceConfigDao.findByClusterIdAndServiceIdAndSelectedIsTrue(any(), any()))
                .thenReturn(serviceConfigPO);
        configService.upsert(1L, 1L, List.of(typeConfigDTO));
    }
}
