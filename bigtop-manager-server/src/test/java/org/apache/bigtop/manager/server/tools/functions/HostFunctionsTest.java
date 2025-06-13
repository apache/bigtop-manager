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

import org.apache.bigtop.manager.dao.query.HostQuery;
import org.apache.bigtop.manager.server.model.vo.HostVO;
import org.apache.bigtop.manager.server.model.vo.PageVO;
import org.apache.bigtop.manager.server.service.HostService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.service.tool.ToolExecutor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HostFunctionsTest {

    @Mock
    private HostService hostService;

    @InjectMocks
    private HostFunctions hostFunctions;

    private HostVO testHost;
    private PageVO<HostVO> testPage;

    @BeforeEach
    void setUp() {
        testHost = new HostVO();
        testHost.setId(1L);
        testHost.setHostname("test-host");

        testPage = new PageVO<>();
        testPage.setContent(List.of(testHost));
        testPage.setTotal(1L);
    }

    @Test
    void testGetHostByIdToolSpecification() {
        Map<ToolSpecification, ToolExecutor> tools = hostFunctions.getHostById();
        assertEquals(1, tools.size());

        ToolSpecification spec = tools.keySet().iterator().next();
        Map<String, JsonSchemaElement> params = spec.parameters().properties();

        assertEquals(1, params.size());
        assertTrue(params.containsKey("hostId"));
    }

    @Test
    void testGetHostByIdExecutorFound() throws Exception {
        when(hostService.get(1L)).thenReturn(testHost);

        Map<ToolSpecification, ToolExecutor> tools = hostFunctions.getHostById();
        ToolExecutor executor = tools.values().iterator().next();

        String arguments = "{\"hostId\": 1}";
        String result = executor.execute(
                ToolExecutionRequest.builder().arguments(arguments).build(), null);

        // Use system-independent newline character regex
        String expectedPattern = ".*\"hostname\"\\s*:\\s*\"test-host\".*";
        assertTrue(
                result.replaceAll("\\R", System.lineSeparator()).matches("(?s)" + expectedPattern),
                "Hostname should match with any line separators");
    }

    @Test
    void testGetHostByIdExecutorNotFound() {
        when(hostService.get(anyLong())).thenReturn(null);

        Map<ToolSpecification, ToolExecutor> tools = hostFunctions.getHostById();
        ToolExecutor executor = tools.values().iterator().next();

        String arguments = "{\"hostId\": 999}";
        String result = executor.execute(
                ToolExecutionRequest.builder().arguments(arguments).build(), null);

        assertEquals("Host not found", result);
    }

    @Test
    void testGetHostByNameToolSpecification() {
        Map<ToolSpecification, ToolExecutor> tools = hostFunctions.getHostByName();
        assertEquals(1, tools.size());

        ToolSpecification spec = tools.keySet().iterator().next();
        assertEquals("getHostByName", spec.name());
        assertEquals("Get host information based on cluster name", spec.description());
        Map<String, JsonSchemaElement> params = spec.parameters().properties();
        assertEquals(1, params.size());
        assertTrue(params.containsKey("hostName"));
    }

    @Test
    void testGetHostByNameExecutor() {
        HostQuery query = new HostQuery();
        query.setHostname("test-host");
        when(hostService.list(query)).thenReturn(testPage);

        Map<ToolSpecification, ToolExecutor> tools = hostFunctions.getHostByName();
        ToolExecutor executor = tools.values().iterator().next();

        String arguments = "{\"hostName\":\"test-host\"}";
        String result = executor.execute(
                ToolExecutionRequest.builder().arguments(arguments).build(), null);

        // System-independent matching pattern
        String totalPattern = "(?s).*\"total\"\\s*:\\s*1.*";
        String hostPattern = "(?s).*\"hostname\"\\s*:\\s*\"test-host\".*";
        assertTrue(result.matches(totalPattern), "Should contain total=1");
        assertTrue(result.matches(hostPattern), "Should contain hostname=test-host");
    }

    @Test
    void testGetAllFunctions() {
        Map<ToolSpecification, ToolExecutor> functions = hostFunctions.getAllFunctions();
        assertEquals(2, functions.size());
        assertTrue(functions.keySet().stream().anyMatch(s -> s.name().equals("getHostById")));
        assertTrue(functions.keySet().stream().anyMatch(s -> s.name().equals("getHostByName")));
    }
}
