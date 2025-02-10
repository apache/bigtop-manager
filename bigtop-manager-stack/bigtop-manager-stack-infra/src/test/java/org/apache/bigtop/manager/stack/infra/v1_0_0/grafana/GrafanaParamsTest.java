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
package org.apache.bigtop.manager.stack.infra.v1_0_0.grafana;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GrafanaParamsTest {

    private GrafanaParams grafanaParams;

    @BeforeEach
    public void setUp() {
        grafanaParams = mock(GrafanaParams.class);
        when(grafanaParams.stackHome()).thenReturn("/stack");
        when(grafanaParams.getServiceName()).thenCallRealMethod();
        when(grafanaParams.serviceHome()).thenCallRealMethod();
        when(grafanaParams.confDir()).thenCallRealMethod();
        when(grafanaParams.dataDir()).thenCallRealMethod();
        when(grafanaParams.provisioningDir()).thenCallRealMethod();
        when(grafanaParams.dataSourceDir()).thenCallRealMethod();
        when(grafanaParams.dashboardsDir()).thenCallRealMethod();
        when(grafanaParams.dashboardConfigDir("test")).thenCallRealMethod();
    }

    @Test
    public void testServiceHome() {
        assertEquals("/stack/grafana", grafanaParams.serviceHome());
    }

    @Test
    public void testConfDir() {
        assertEquals("/stack/grafana/conf", grafanaParams.confDir());
    }

    @Test
    public void testDataDir() {
        assertEquals("/stack/grafana/data", grafanaParams.dataDir());
    }

    @Test
    public void testProvisioningDir() {
        assertEquals("/stack/grafana/conf/provisioning", grafanaParams.provisioningDir());
    }

    @Test
    public void testDataSourceDir() {
        assertEquals("/stack/grafana/conf/provisioning/datasources", grafanaParams.dataSourceDir());
    }

    @Test
    public void testDashboardsDir() {
        assertEquals("/stack/grafana/conf/provisioning/dashboards", grafanaParams.dashboardsDir());
    }

    @Test
    public void testDashboardConfigDir() {
        assertEquals("/stack/grafana/conf/provisioning/dashboards/test", grafanaParams.dashboardConfigDir("test"));
    }

    @Test
    public void testGetServiceName() {
        assertEquals("grafana", grafanaParams.getServiceName());
    }
}
