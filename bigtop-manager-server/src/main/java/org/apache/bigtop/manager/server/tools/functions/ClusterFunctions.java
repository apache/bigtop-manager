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
import org.apache.bigtop.manager.server.model.vo.ClusterVO;
import org.apache.bigtop.manager.server.service.ClusterService;

import org.springframework.stereotype.Component;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.service.tool.ToolExecutor;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ClusterFunctions {
    @Resource
    private ClusterService clusterService;

    public Map<ToolSpecification, ToolExecutor> listCluster() {
        ToolSpecification toolSpecification = ToolSpecification.builder()
                .name("listCluster")
                .description("Get cluster list")
                .build();
        ToolExecutor toolExecutor =
                (toolExecutionRequest, memoryId) -> JsonUtils.indentWriteAsString(clusterService.list());

        return Map.of(toolSpecification, toolExecutor);
    }

    public Map<ToolSpecification, ToolExecutor> getClusterById() {
        ToolSpecification toolSpecification = ToolSpecification.builder()
                .name("getClusterById")
                .description("Get cluster information based on ID")
                .parameters(JsonObjectSchema.builder()
                        .description("Cluster ID")
                        .addNumberProperty("clusterId")
                        .build())
                .build();
        ToolExecutor toolExecutor = (toolExecutionRequest, memoryId) -> {
            Map<String, Object> arguments = JsonUtils.readFromString(toolExecutionRequest.arguments());
            Long clusterId = Long.valueOf(arguments.get("clusterId").toString());
            ClusterVO clusterVO = clusterService.get(clusterId);
            if (clusterVO == null) {
                return "Cluster not found";
            }
            return JsonUtils.indentWriteAsString(clusterVO);
        };

        return Map.of(toolSpecification, toolExecutor);
    }

    public Map<ToolSpecification, ToolExecutor> getClusterByName() {
        ToolSpecification toolSpecification = ToolSpecification.builder()
                .name("getClusterByName")
                .description("Get cluster information based on cluster name")
                .parameters(JsonObjectSchema.builder()
                        .description("Cluster name")
                        .addStringProperty("clusterName")
                        .build())
                .build();
        ToolExecutor toolExecutor = (toolExecutionRequest, memoryId) -> {
            Map<String, Object> arguments = JsonUtils.readFromString(toolExecutionRequest.arguments());
            String clusterName = arguments.get("clusterName").toString();
            List<ClusterVO> clusterVOS = clusterService.list();
            for (ClusterVO clusterVO : clusterVOS) {
                if (clusterVO.getName().equals(clusterName)) {
                    return JsonUtils.indentWriteAsString(clusterVO);
                }
            }
            return "Cluster not found";
        };

        return Map.of(toolSpecification, toolExecutor);
    }

    public Map<ToolSpecification, ToolExecutor> getAllFunctions() {
        Map<ToolSpecification, ToolExecutor> functions = new HashMap<>();
        functions.putAll(listCluster());
        functions.putAll(getClusterById());
        functions.putAll(getClusterByName());
        return functions;
    }
}
