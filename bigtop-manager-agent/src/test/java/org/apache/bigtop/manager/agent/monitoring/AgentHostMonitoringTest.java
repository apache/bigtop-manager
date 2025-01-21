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
package org.apache.bigtop.manager.agent.monitoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.core.instrument.MultiGauge;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentHostMonitoringTest {

    @Mock
    private SystemInfo systemInfo;

    @Mock
    private HardwareAbstractionLayer hardwareAbstractionLayer;

    @Mock
    private GlobalMemory globalMemory;

    @Mock
    private MultiGauge diskMultiGauge;

    @Mock
    private MultiGauge memMultiGauge;

    @Mock
    private MultiGauge cpuMultiGauge;

    @Mock
    private MultiGauge diskIOMultiGauge;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(systemInfo.getOperatingSystem()).thenReturn(mock(OperatingSystem.class));
        when(systemInfo.getHardware()).thenReturn(hardwareAbstractionLayer);
        when(hardwareAbstractionLayer.getMemory()).thenReturn(globalMemory);
        when(globalMemory.getAvailable()).thenReturn(100000L);
        when(globalMemory.getTotal()).thenReturn(200000L);
    }

    @Test
    void testGetHostInfo() throws UnknownHostException {
        JsonNode hostInfo = AgentHostMonitoring.getHostInfo();

        // Check if the host information contains expected fields
        assertTrue(hostInfo.has(AgentHostMonitoring.AGENT_BASE_INFO));
        assertTrue(hostInfo.has(AgentHostMonitoring.BOOT_TIME));
        assertTrue(hostInfo.has(AgentHostMonitoring.MEM_IDLE));
        assertTrue(hostInfo.has(AgentHostMonitoring.MEM_TOTAL));
    }

    @Test
    void testGetDiskGauge() {
        // Create a mock JsonNode object
        JsonNode agentMonitoring = mock(JsonNode.class);

        // Create a mock agentHostInfo object and set its fields
        ObjectNode agentHostInfo = mock(ObjectNode.class);
        when(agentMonitoring.get(AgentHostMonitoring.AGENT_BASE_INFO)).thenReturn(agentHostInfo);
        when(agentHostInfo.fieldNames()).thenReturn(Collections.emptyIterator());

        // Create a mock disksBaseInfo object and set its fields
        ArrayNode disksBaseInfo = mock(ArrayNode.class);
        when(agentMonitoring.get(AgentHostMonitoring.DISKS_BASE_INFO)).thenReturn(disksBaseInfo);

        // Create a mock diskJsonNode object and set its fields
        ObjectNode diskJsonNode = mock(ObjectNode.class);
        when(disksBaseInfo.get(0)).thenReturn(diskJsonNode);
        when(diskJsonNode.get(AgentHostMonitoring.DISK_NAME)).thenReturn(mock(JsonNode.class));
        when(diskJsonNode.get(AgentHostMonitoring.DISK_IDLE)).thenReturn(mock(JsonNode.class));
        when(diskJsonNode.get(AgentHostMonitoring.DISK_TOTAL)).thenReturn(mock(JsonNode.class));
        when(diskJsonNode.get(AgentHostMonitoring.DISK_NAME).asText()).thenReturn("disk1");
        when(diskJsonNode.get(AgentHostMonitoring.DISK_IDLE).asDouble()).thenReturn(0.9);
        when(diskJsonNode.get(AgentHostMonitoring.DISK_TOTAL).asDouble()).thenReturn(100.0);

        // Call the getDiskGauge method
        Map<ArrayList<String>, Map<ArrayList<String>, Double>> diskGauge =
                AgentHostMonitoring.getDiskGauge(agentMonitoring);

        // Assert that the disk gauge data is populated correctly
        assertNotNull(diskGauge);
        assertFalse(diskGauge.isEmpty());
    }

    @Test
    void testGetCPUGauge() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode agentMonitoring = objectMapper.createObjectNode();
        ObjectNode agentHostInfo = objectMapper.createObjectNode();

        // Set AGENT_BASE_INFO fields
        agentHostInfo.put("field1", "value1");
        agentHostInfo.put("field2", "value2");
        agentMonitoring.set(AgentHostMonitoring.AGENT_BASE_INFO, agentHostInfo);

        // Set CPU related fields
        agentMonitoring
                .put(AgentHostMonitoring.CPU_LOAD_AVG_MIN_1, 1.0)
                .put(AgentHostMonitoring.CPU_LOAD_AVG_MIN_5, 5.0)
                .put(AgentHostMonitoring.CPU_LOAD_AVG_MIN_15, 15.0)
                .put(AgentHostMonitoring.CPU_USAGE, 10.0);

        Map<ArrayList<String>, Map<ArrayList<String>, Double>> cpuGauge =
                AgentHostMonitoring.getCPUGauge(agentMonitoring);

        // Assert that the CPU gauge data is populated correctly
        assertNotNull(cpuGauge);
        assertFalse(cpuGauge.isEmpty());
    }

    @Test
    void testGetMEMGauge() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode agentMonitoring = objectMapper.createObjectNode();
        ObjectNode agentHostInfo = objectMapper.createObjectNode();

        // Set AGENT_BASE_INFO fields
        agentHostInfo.put("field1", "value1");
        agentHostInfo.put("field2", "value2");
        agentMonitoring.set(AgentHostMonitoring.AGENT_BASE_INFO, agentHostInfo);

        // Set MEM related fields
        agentMonitoring.put(AgentHostMonitoring.MEM_IDLE, 2000.0).put(AgentHostMonitoring.MEM_TOTAL, 4000.0);

        Map<ArrayList<String>, Map<ArrayList<String>, Double>> memGauge =
                AgentHostMonitoring.getMEMGauge(agentMonitoring);

        // Assert that the MEM gauge data is populated correctly
        assertNotNull(memGauge);
        assertFalse(memGauge.isEmpty());
    }

    @Test
    void testMultiGaugeUpdateData() {
        // Create a mock Map object
        Map<ArrayList<String>, Map<ArrayList<String>, Double>> diskGauge = new HashMap<>();
        Map<ArrayList<String>, Double> innerMap = new HashMap<>();
        innerMap.put(new ArrayList<>(Arrays.asList("label1", "label2")), 1.0);
        diskGauge.put(new ArrayList<>(Arrays.asList("label1", "label2")), innerMap);

        // Create a mock MultiGauge object
        MultiGauge diskMultiGauge = mock(MultiGauge.class);

        // Call the test method
        AgentHostMonitoring.multiGaugeUpdateData(diskMultiGauge, diskGauge);

        // Verify that the register method is called once
        verify(diskMultiGauge, times(1)).register(anyList(), eq(true));
    }

    @Test
    void testDiskMultiGaugeUpdateData() {
        AgentHostMonitoring.diskMultiGaugeUpdateData(diskMultiGauge);

        // Verify that the multi-gauge update method was called
        verify(diskMultiGauge, times(1)).register(anyList(), eq(true));
    }

    @Test
    void testMemMultiGaugeUpdateData() {
        AgentHostMonitoring.memMultiGaugeUpdateData(memMultiGauge);

        // Verify that the multi-gauge update method was called
        verify(memMultiGauge, times(1)).register(anyList(), eq(true));
    }

    @Test
    void testCpuMultiGaugeUpdateData() {
        AgentHostMonitoring.cpuMultiGaugeUpdateData(cpuMultiGauge);

        // Verify that the multi-gauge update method was called
        verify(cpuMultiGauge, times(1)).register(anyList(), eq(true));
    }

    @Test
    void testDiskIOMultiGaugeUpdateData() {
        AgentHostMonitoring.diskIOMultiGaugeUpdateData(diskIOMultiGauge);

        // Verify that the multi-gauge update method was called
        verify(diskIOMultiGauge, times(1)).register(anyList(), eq(true));
    }
}
