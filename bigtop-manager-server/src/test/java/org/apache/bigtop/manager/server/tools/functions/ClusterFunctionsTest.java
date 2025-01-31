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
package org.apache.bigtop.manager.server.tools.functions;

import org.apache.bigtop.manager.server.model.vo.ClusterVO;
import org.apache.bigtop.manager.server.service.ClusterService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClusterFunctionsTest {

    @Mock
    private ClusterService clusterService;

    @InjectMocks
    private ClusterFunctions clusterFunctions;

    private ClusterVO testCluster;

    @BeforeEach
    void setUp() {
        testCluster = new ClusterVO();
        testCluster.setId(1L);
        testCluster.setName("test-cluster");
    }

    @Test
    void testListCluster() {
        // Mock clusterService response
        when(clusterService.list()).thenReturn(Collections.singletonList(testCluster));

        // Get the tool specification and executor
        Map<ToolSpecification, ToolExecutor> tools = clusterFunctions.listCluster();
        assertEquals(1, tools.size());

        ToolSpecification spec = tools.keySet().iterator().next();
        ToolExecutor executor = tools.get(spec);

        // Execute the tool
        String result = executor.execute(ToolExecutionRequest.builder().build(), "memoryId");

        // Verify results
        assertTrue(result.contains("test-cluster"));
        verify(clusterService, times(1)).list();
    }

    @Test
    void testGetClusterById() {
        // Mock clusterService response
        when(clusterService.get(1L)).thenReturn(testCluster);

        // Get the tool specification and executor
        Map<ToolSpecification, ToolExecutor> tools = clusterFunctions.getClusterById();
        assertEquals(1, tools.size());

        ToolSpecification spec = tools.keySet().iterator().next();
        ToolExecutor executor = tools.get(spec);

        // Build request with arguments
        String arguments = "{\"clusterId\": 1}";
        ToolExecutionRequest request =
                ToolExecutionRequest.builder().arguments(arguments).build();

        // Execute the tool
        String result = executor.execute(request, "memoryId");

        // Verify results
        assertTrue(result.contains("test-cluster"));
        verify(clusterService, times(1)).get(1L);
    }

    @Test
    void testGetClusterByIdWhenNotExists() {
        // Mock clusterService response
        when(clusterService.get(999L)).thenReturn(null);

        // Get the tool specification and executor
        Map<ToolSpecification, ToolExecutor> tools = clusterFunctions.getClusterById();
        ToolExecutor executor = tools.values().iterator().next();

        // Build request with arguments
        String arguments = "{\"clusterId\": 999}";
        ToolExecutionRequest request =
                ToolExecutionRequest.builder().arguments(arguments).build();

        // Execute the tool
        String result = executor.execute(request, "memoryId");

        // Verify results
        assertEquals("Cluster not found", result);
    }

    @Test
    void testGetClusterByName() {
        // Mock clusterService response
        when(clusterService.list()).thenReturn(Collections.singletonList(testCluster));

        // Get the tool specification and executor
        Map<ToolSpecification, ToolExecutor> tools = clusterFunctions.getClusterByName();
        ToolExecutor executor = tools.values().iterator().next();

        // Build request with arguments
        String arguments = "{\"clusterName\": \"test-cluster\"}";
        ToolExecutionRequest request =
                ToolExecutionRequest.builder().arguments(arguments).build();

        // Execute the tool
        String result = executor.execute(request, "memoryId");

        // Verify results
        assertTrue(result.contains("test-cluster"));
        verify(clusterService, times(1)).list();
    }

    @Test
    void testGetClusterByNameWhenNotExists() {
        // Mock clusterService response
        when(clusterService.list()).thenReturn(Collections.singletonList(testCluster));

        // Get the tool specification and executor
        Map<ToolSpecification, ToolExecutor> tools = clusterFunctions.getClusterByName();
        ToolExecutor executor = tools.values().iterator().next();

        // Build request with arguments
        String arguments = "{\"clusterName\": \"non-existent\"}";
        ToolExecutionRequest request =
                ToolExecutionRequest.builder().arguments(arguments).build();

        // Execute the tool
        String result = executor.execute(request, "memoryId");

        // Verify results
        assertEquals("Cluster not found", result);
    }

    @Test
    void testGetAllFunctions() {
        Map<ToolSpecification, ToolExecutor> functions = clusterFunctions.getAllFunctions();
        assertEquals(3, functions.size());

        List<String> expectedToolNames = List.of("listCluster", "getClusterById", "getClusterByName");
        assertTrue(functions.keySet().stream().map(ToolSpecification::name).allMatch(expectedToolNames::contains));
    }
}
