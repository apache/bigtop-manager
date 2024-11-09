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

import org.apache.bigtop.manager.dao.repository.HostComponentDao;
import org.apache.bigtop.manager.server.service.impl.HostComponentServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HostComponentServiceTest {
    @Mock
    private HostComponentDao hostComponentDao;

    @InjectMocks
    private HostComponentService hostComponentService = new HostComponentServiceImpl();

    @Test
    public void testListHostComponent() {
        when(hostComponentDao.findAllByClusterId(any())).thenReturn(new ArrayList<>());
        assert hostComponentService.list(1L) != null;

        when(hostComponentDao.findAllByClusterIdAndHostId(any(), any())).thenReturn(null);
        assert hostComponentService.listByHost(1L, 1L) == null;

        when(hostComponentDao.findAllByClusterIdAndServiceId(any(), any())).thenReturn(null);
        assert hostComponentService.listByService(1L, 1L) == null;
    }
}
