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

import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.server.model.converter.ClusterConverter;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClusterInfoTools {

    @Tool("Get cluster list")
    public Map<ToolSpecification, ToolExecutor> list() {
        ToolSpecification toolSpecification = ToolSpecification.builder()
                .name("list")
                .description("Get cluster list")
                .build();
        ToolExecutor toolExecutor = (toolExecutionRequest, memoryId) -> {
            List<ClusterPO> clusterPOList = new ArrayList<>();
            ClusterPO mockClusterPO = new ClusterPO();
            mockClusterPO.setId(1L);
            mockClusterPO.setName("mock-cluster");
            clusterPOList.add(mockClusterPO);
            return ClusterConverter.INSTANCE.fromPO2VO(clusterPOList).toString();
        };

        return Map.of(toolSpecification, toolExecutor);
    }
}
