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

import org.apache.bigtop.manager.server.model.vo.AttrsVO;
import org.apache.bigtop.manager.server.model.vo.PropertyVO;
import org.apache.bigtop.manager.server.model.vo.ServiceConfigVO;
import org.apache.bigtop.manager.server.model.vo.ServiceVO;
import org.apache.bigtop.manager.server.model.vo.StackVO;
import org.apache.bigtop.manager.server.service.StackService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StackFunctionsTest {

    @Mock
    private StackService stackService;

    @InjectMocks
    private StackFunctions stackFunctions;

    private StackVO testStack;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testStack = new StackVO();
        testStack.setStackName("test-stack");

        ServiceVO testService = new ServiceVO();
        testService.setName("test-service");

        ServiceConfigVO config = new ServiceConfigVO();
        PropertyVO normalProp = new PropertyVO();
        normalProp.setName("normal");
        normalProp.setValue("value");
        PropertyVO contentProp = new PropertyVO();
        contentProp.setName("content");
        contentProp.setValue("secret");
        PropertyVO longtextProp = new PropertyVO();
        longtextProp.setName("longtext");
        longtextProp.setValue("very long text");
        AttrsVO attrs = new AttrsVO();
        attrs.setType("longtext");
        longtextProp.setAttrs(attrs);

        config.setProperties(List.of(normalProp, contentProp, longtextProp));
        testService.setConfigs(List.of(config));

        testStack.setServices(List.of(testService));
    }

    @Test
    void testListStackAndService() {
        // 模拟服务层返回数据
        when(stackService.list()).thenReturn(List.of(testStack));

        // 获取工具
        Map<ToolSpecification, ToolExecutor> tools = stackFunctions.listStackAndService();
        assertEquals(1, tools.size());

        // 验证工具规范
        ToolSpecification spec = tools.keySet().iterator().next();
        assertEquals("listStackAndService", spec.name());
        assertEquals("Retrieve the list of services in each stack", spec.description());

        // 执行工具
        ToolExecutor executor = tools.values().iterator().next();
        String result =
                executor.execute(ToolExecutionRequest.builder().arguments("{}").build(), null);

        // 验证结果
        String expectedJson =
                """
                {
                  "test-stack": ["test-service"]
                }""";
        assertEquals(expectedJson.replaceAll("\\s", ""), result.replaceAll("\\s", ""));
    }

    @Test
    void testGetServiceByNameFound() {
        // 模拟服务层返回数据
        when(stackService.list()).thenReturn(List.of(testStack));

        // 获取工具
        Map<ToolSpecification, ToolExecutor> tools = stackFunctions.getServiceByName();
        ToolExecutor executor = tools.values().iterator().next();

        // 执行查询
        String arguments = "{\"serviceName\" : \"test-service\"}";
        String result = executor.execute(
                ToolExecutionRequest.builder().arguments(arguments).build(), null);

        // 验证结果
        assertAll(
                () -> assertTrue(result.contains("\"name\" : \"test-service\"")),
                () -> assertTrue(result.contains("\"name\" : \"normal\"")),
                () -> assertTrue(result.contains("\"name\" : \"content\"")),
                () -> assertTrue(result.contains("\"name\" : \"longtext\"")));
    }

    @Test
    void testGetServiceByNameNotFound() {
        when(stackService.list()).thenReturn(List.of(testStack));

        Map<ToolSpecification, ToolExecutor> tools = stackFunctions.getServiceByName();
        ToolExecutor executor = tools.values().iterator().next();

        String arguments = "{\"serviceName\":\"non-existent\"}";
        String result = executor.execute(
                ToolExecutionRequest.builder().arguments(arguments).build(), null);

        assertEquals("Service not found", result);
    }

    @Test
    void testGetServiceByNameToolSpecification() {
        Map<ToolSpecification, ToolExecutor> tools = stackFunctions.getServiceByName();
        ToolSpecification spec = tools.keySet().iterator().next();
        Map<String, Map<String, Object>> params = spec.parameters().properties();
        assertAll(
                () -> assertEquals("getServiceByName", spec.name()),
                () -> assertEquals("Get service information and configs based on service name", spec.description()),
                () -> assertEquals(1, params.size()),
                () -> assertTrue(params.containsKey("serviceName")),
                () -> assertEquals("string", params.get("serviceName").get("type")),
                () -> assertEquals(
                        "Lowercase service name", params.get("serviceName").get("description")));
    }

    @Test
    void testGetAllFunctions() {
        Map<ToolSpecification, ToolExecutor> functions = stackFunctions.getAllFunctions();
        assertEquals(2, functions.size());
        assertTrue(functions.keySet().stream().anyMatch(s -> s.name().equals("listStackAndService")));
        assertTrue(functions.keySet().stream().anyMatch(s -> s.name().equals("getServiceByName")));
    }
}
