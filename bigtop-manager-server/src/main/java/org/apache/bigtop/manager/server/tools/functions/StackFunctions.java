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

import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.server.model.vo.PropertyVO;
import org.apache.bigtop.manager.server.model.vo.ServiceConfigVO;
import org.apache.bigtop.manager.server.model.vo.ServiceVO;
import org.apache.bigtop.manager.server.model.vo.StackVO;
import org.apache.bigtop.manager.server.service.StackService;

import org.springframework.stereotype.Component;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.service.tool.ToolExecutor;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class StackFunctions {
    @Resource
    private StackService stackService;

    public Map<ToolSpecification, ToolExecutor> listStackAndService() {
        ToolSpecification toolSpecification = ToolSpecification.builder()
                .name("listStackAndService")
                .description("Retrieve the list of services in each stack")
                .build();
        ToolExecutor toolExecutor = (toolExecutionRequest, memoryId) -> {
            Map<String, List<String>> stackInfo = new HashMap<>();
            for (StackVO stackVO : stackService.list()) {
                List<String> services = new ArrayList<>();
                for (ServiceVO serviceVO : stackVO.getServices()) {
                    services.add(serviceVO.getName());
                }
                stackInfo.put(stackVO.getStackName(), services);
            }
            return JsonUtils.indentWriteAsString(stackInfo);
        };
        return Map.of(toolSpecification, toolExecutor);
    }

    public Map<ToolSpecification, ToolExecutor> getServiceByName() {
        ToolSpecification toolSpecification = ToolSpecification.builder()
                .name("getServiceByName")
                .description("Get service information and configs based on service name")
                .parameters(JsonObjectSchema.builder()
                        .addStringProperty("serviceName")
                        .description("Service name")
                        .build())
                .build();
        ToolExecutor toolExecutor = (toolExecutionRequest, memoryId) -> {
            Map<String, Object> arguments = JsonUtils.readFromString(toolExecutionRequest.arguments());
            String serviceName = arguments.get("serviceName").toString();
            for (StackVO stackVO : stackService.list()) {
                for (ServiceVO serviceVO : stackVO.getServices()) {
                    if (serviceVO.getName().equals(serviceName)) {
                        for (ServiceConfigVO serviceConfigVO : serviceVO.getConfigs()) {
                            for (PropertyVO propertyVO : serviceConfigVO.getProperties()) {
                                if (propertyVO.getName().equals("content")) {
                                    propertyVO.setValue(null);
                                }
                                if (propertyVO.getAttrs() != null
                                        && propertyVO.getAttrs().getType().equals("longtext")) {
                                    propertyVO.setValue(null);
                                }
                            }
                        }
                        return JsonUtils.indentWriteAsString(serviceVO);
                    }
                }
            }
            return "Service not found";
        };

        return Map.of(toolSpecification, toolExecutor);
    }

    public Map<ToolSpecification, ToolExecutor> getAllFunctions() {
        Map<ToolSpecification, ToolExecutor> functions = new HashMap<>();
        functions.putAll(listStackAndService());
        functions.putAll(getServiceByName());
        return functions;
    }
}
