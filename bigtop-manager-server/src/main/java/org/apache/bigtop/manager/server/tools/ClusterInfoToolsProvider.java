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
package org.apache.bigtop.manager.server.tools;

import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.server.model.vo.ClusterVO;
import org.apache.bigtop.manager.server.service.ClusterService;

import org.springframework.stereotype.Component;

import dev.langchain4j.agent.tool.JsonSchemaProperty;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderRequest;
import dev.langchain4j.service.tool.ToolProviderResult;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ClusterInfoToolsProvider implements ToolProvider {
    @Resource
    private ClusterService clusterService;

    @Tool("Get cluster list")
    public Map<ToolSpecification, ToolExecutor> list() {
        ToolSpecification toolSpecification = ToolSpecification.builder()
                .name("list")
                .description("Get cluster list")
                .build();
        ToolExecutor toolExecutor =
                (toolExecutionRequest, memoryId) -> clusterService.list().toString();

        return Map.of(toolSpecification, toolExecutor);
    }

    @Tool("Get cluster information based on ID")
    public Map<ToolSpecification, ToolExecutor> get() {
        ToolSpecification toolSpecification = ToolSpecification.builder()
                .name("get")
                .description("Get cluster information based on ID")
                .addParameter("clusterId", JsonSchemaProperty.NUMBER, JsonSchemaProperty.description("cluster id"))
                .build();
        ToolExecutor toolExecutor = (toolExecutionRequest, memoryId) -> {
            Map<String, Object> arguments = JsonUtils.readFromString(toolExecutionRequest.arguments());
            Long clusterId = Long.valueOf(arguments.get("clusterId").toString());
            return clusterService.get(clusterId).toString();
        };

        return Map.of(toolSpecification, toolExecutor);
    }

    @Tool("Get cluster information based on cluster name")
    public Map<ToolSpecification, ToolExecutor> getByName() {
        ToolSpecification toolSpecification = ToolSpecification.builder()
                .name("getByName")
                .description("Get cluster information based on cluster name")
                .addParameter("clusterName", JsonSchemaProperty.STRING, JsonSchemaProperty.description("cluster name"))
                .build();
        ToolExecutor toolExecutor = (toolExecutionRequest, memoryId) -> {
            Map<String, Object> arguments = JsonUtils.readFromString(toolExecutionRequest.arguments());
            String clusterName = arguments.get("clusterName").toString();
            List<ClusterVO> clusterVOS = clusterService.list();
            for (ClusterVO clusterVO : clusterVOS) {
                if (clusterVO.getName().equals(clusterName)) {
                    return clusterVO.toString();
                }
            }
            return "Cluster not found";
        };

        return Map.of(toolSpecification, toolExecutor);
    }

    @Override
    public ToolProviderResult provideTools(ToolProviderRequest toolProviderRequest) {
        return ToolProviderResult.builder()
                .addAll(list())
                .addAll(get())
                .addAll(getByName())
                .build();
    }
}
