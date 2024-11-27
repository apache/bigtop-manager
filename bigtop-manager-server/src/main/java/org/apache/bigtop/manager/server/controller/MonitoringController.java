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
package org.apache.bigtop.manager.server.controller;

import org.apache.bigtop.manager.server.model.vo.ClusterVO;
import org.apache.bigtop.manager.server.service.ClusterService;
import org.apache.bigtop.manager.server.service.HostService;
import org.apache.bigtop.manager.server.service.MonitoringService;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.Resource;

import javax.naming.ldap.PagedResultsControl;

@Tag(name = "Monitoring Controller")
@RestController
@RequestMapping("monitoring")
public class MonitoringController {

    @Resource
    private MonitoringService monitoringService;
    @Resource
    private ClusterService clusterService;

    @Resource
    private HostService hostService;

    @Operation(summary = "agent healthy", description = "agent healthy check")
    @GetMapping("agenthealthy")
    public ResponseEntity<JsonNode> agentHostsHealthyStatus() {
        // json for response
        return ResponseEntity.success(monitoringService.queryAgentsHealthyStatus());
    }

    @Operation(summary = "agent Info", description = "agent info query")
    @GetMapping("agentinfo")
    public ResponseEntity<JsonNode> queryAgentsInfo() {
        return ResponseEntity.success(monitoringService.queryAgentsInfo());
    }

    @Operation(summary = "agent instant info", description = "agent instant info query")
    @GetMapping("agentinstinfo")
    public ResponseEntity<JsonNode> queryAgentsInstStatus() {
        return ResponseEntity.success(monitoringService.queryAgentsInstStatus());
    }

    @Operation(summary = "cluster info", description = "cluster info")
    @GetMapping("clusterinfo")
    public ResponseEntity<JsonNode> queryCluster(
            @RequestParam(value = "clusterId", required = true) String clusterId,
            @RequestParam(value = "timeDec", required = true, defaultValue = "0") String timeDec) {
        ClusterVO clusterVO = clusterService.get(Long.getLong(clusterId));
        String clusterName = clusterVO.getName();
        return ResponseEntity.success(monitoringService.queryClusterInfo(clusterName));
    }
}
