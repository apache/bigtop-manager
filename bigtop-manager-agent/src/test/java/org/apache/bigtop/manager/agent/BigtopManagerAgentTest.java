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
package org.apache.bigtop.manager.agent;

import org.apache.bigtop.manager.agent.monitoring.AgentHostMonitoring;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BigtopManagerAgentTest {

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private MultiGauge diskMultiGauge;

    @Mock
    private MultiGauge cpuMultiGauge;

    @Mock
    private MultiGauge memMultiGauge;

    @Mock
    private MultiGauge diskIOMultiGauge;

    @InjectMocks
    private BigtopManagerAgent bigtopManagerAgent;

    @Test
    public void testDiskMultiGaugeBean() {
        try (MockedStatic<AgentHostMonitoring> mockedStatic = mockStatic(AgentHostMonitoring.class)) {
            when(AgentHostMonitoring.newDiskMultiGauge(meterRegistry)).thenReturn(diskMultiGauge);
            MultiGauge result = bigtopManagerAgent.diskMultiGauge(meterRegistry);
            assertNotNull(result);
        }
    }

    @Test
    public void testCPUMultiGaugeBean() {
        try (MockedStatic<AgentHostMonitoring> mockedStatic = mockStatic(AgentHostMonitoring.class)) {
            when(AgentHostMonitoring.newCPUMultiGauge(meterRegistry)).thenReturn(cpuMultiGauge);
            MultiGauge result = bigtopManagerAgent.cpuMultiGauge(meterRegistry);
            assertNotNull(result);
        }
    }

    @Test
    public void testMemMultiGaugeBean() {
        try (MockedStatic<AgentHostMonitoring> mockedStatic = mockStatic(AgentHostMonitoring.class)) {
            when(AgentHostMonitoring.newMemMultiGauge(meterRegistry)).thenReturn(memMultiGauge);
            MultiGauge result = bigtopManagerAgent.memMultiGauge(meterRegistry);
            assertNotNull(result);
        }
    }

    @Test
    public void testDiskIOMultiGaugeBean() {
        try (MockedStatic<AgentHostMonitoring> mockedStatic = mockStatic(AgentHostMonitoring.class)) {
            when(AgentHostMonitoring.newDiskIOMultiGauge(meterRegistry)).thenReturn(diskIOMultiGauge);
            MultiGauge result = bigtopManagerAgent.diskIOMultiGauge(meterRegistry);
            assertNotNull(result);
        }
    }

    @Test
    public void testMainMethod() {
        try (MockedStatic<SpringApplication> mockedStatic = mockStatic(SpringApplication.class)) {
            BigtopManagerAgent.main(new String[] {});
            mockedStatic.verify(() -> SpringApplication.run(BigtopManagerAgent.class, new String[] {}));
        }
    }
}
