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
package org.apache.bigtop.manager.agent.metrics;

import org.apache.bigtop.manager.agent.monitoring.AgentHostMonitoring;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import io.micrometer.core.instrument.MultiGauge;

import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class MetricsCollectorTest {

    @Mock
    private MultiGauge diskMultiGauge;

    @Mock
    private MultiGauge memMultiGauge;

    @Mock
    private MultiGauge cpuMultiGauge;

    @Mock
    private MultiGauge diskIOMultiGauge;

    @InjectMocks
    private MetricsCollector metricsCollector;

    @Test
    public void testCollect() {
        try (MockedStatic<AgentHostMonitoring> mockedStatic = mockStatic(AgentHostMonitoring.class)) {
            metricsCollector.collect();
            mockedStatic.verify(() -> AgentHostMonitoring.diskMultiGaugeUpdateData(diskMultiGauge));
            mockedStatic.verify(() -> AgentHostMonitoring.memMultiGaugeUpdateData(memMultiGauge));
            mockedStatic.verify(() -> AgentHostMonitoring.cpuMultiGaugeUpdateData(cpuMultiGauge));
            mockedStatic.verify(() -> AgentHostMonitoring.diskIOMultiGaugeUpdateData(diskIOMultiGauge));
        }
    }
}
