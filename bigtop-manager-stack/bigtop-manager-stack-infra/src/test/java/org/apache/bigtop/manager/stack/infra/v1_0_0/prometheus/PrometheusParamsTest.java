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
package org.apache.bigtop.manager.stack.infra.v1_0_0.prometheus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PrometheusParamsTest {

    private PrometheusParams prometheusParams;

    @BeforeEach
    public void setUp() {
        prometheusParams = mock(PrometheusParams.class);
        when(prometheusParams.stackHome()).thenReturn("/stack");
        when(prometheusParams.getServiceName()).thenCallRealMethod();
        when(prometheusParams.serviceHome()).thenCallRealMethod();
        when(prometheusParams.dataDir()).thenCallRealMethod();
        when(prometheusParams.targetsConfigFile("test_job")).thenCallRealMethod();
        when(prometheusParams.confDir()).thenCallRealMethod();
    }

    @Test
    public void testServiceHome() {
        assertEquals("/stack/prometheus", prometheusParams.serviceHome());
    }

    @Test
    public void testConfDir() {
        assertEquals("/stack/prometheus/conf", prometheusParams.confDir());
    }

    @Test
    public void testDataDir() {
        assertEquals("/stack/prometheus/data", prometheusParams.dataDir());
    }

    @Test
    public void testTargetsConfigFile() {
        assertEquals("/stack/prometheus/conf/test_job_targets.json", prometheusParams.targetsConfigFile("test_job"));
    }

    @Test
    public void testGetServiceName() {
        assertEquals("prometheus", prometheusParams.getServiceName());
    }
}
