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
import org.apache.bigtop.manager.dao.query.HostQuery;
import org.apache.bigtop.manager.server.model.vo.HostVO;
import org.apache.bigtop.manager.server.model.vo.PageVO;
import org.apache.bigtop.manager.server.service.HostService;

import org.springframework.stereotype.Component;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.service.tool.ToolExecutor;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class HostFunctions {
    @Resource
    private HostService hostService;

    public Map<ToolSpecification, ToolExecutor> getHostById() {
        ToolSpecification toolSpecification = ToolSpecification.builder()
                .name("getHostById")
                .description("Get host information based on ID")
                .parameters(JsonObjectSchema.builder()
                        .description("Host ID")
                        .addNumberProperty("hostId")
                        .build())
                .build();
        ToolExecutor toolExecutor = (toolExecutionRequest, memoryId) -> {
            Map<String, Object> arguments = JsonUtils.readFromString(toolExecutionRequest.arguments());
            Long hostId = Long.valueOf(arguments.get("hostId").toString());
            HostVO hostVO = hostService.get(hostId);
            if (hostVO == null) {
                return "Host not found";
            }
            return JsonUtils.indentWriteAsString(hostVO);
        };

        return Map.of(toolSpecification, toolExecutor);
    }

    public Map<ToolSpecification, ToolExecutor> getHostByName() {
        ToolSpecification toolSpecification = ToolSpecification.builder()
                .name("getHostByName")
                .description("Get host information based on cluster name")
                .parameters(JsonObjectSchema.builder()
                        .description("Host name")
                        .addStringProperty("hostName")
                        .build())
                .build();
        ToolExecutor toolExecutor = (toolExecutionRequest, memoryId) -> {
            Map<String, Object> arguments = JsonUtils.readFromString(toolExecutionRequest.arguments());
            String hostName = arguments.get("hostName").toString();
            HostQuery hostQuery = new HostQuery();
            hostQuery.setHostname(hostName);
            PageVO<HostVO> hostVO = hostService.list(hostQuery);
            return JsonUtils.indentWriteAsString(hostVO);
        };

        return Map.of(toolSpecification, toolExecutor);
    }

    public Map<ToolSpecification, ToolExecutor> getAllFunctions() {
        Map<ToolSpecification, ToolExecutor> functions = new HashMap<>();
        functions.putAll(getHostById());
        functions.putAll(getHostByName());
        return functions;
    }
}
