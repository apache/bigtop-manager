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

import org.apache.bigtop.manager.server.service.MonitoringService;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.Resource;

@Tag(name = "Monitoring Controller")
@RestController
@RequestMapping("monitoring")
public class MonitoringController {

    @Resource
    private MonitoringService monitoringService;

    @Operation(summary = "agent healthy", description = "agent healthy check")
    @GetMapping("agenthealthy")
    public ResponseEntity<JsonNode> agentHostsHealthyStatus() {
        // 以json格式返回   响应结果 参数即为数据  数据格式为 json
        return ResponseEntity.success(monitoringService.queryAgentsHealthyStatus());
    }

    @Operation(summary = "agent Info", description = "agent info query")
    @GetMapping("agentinfo")
    public ResponseEntity<JsonNode> queryAgentsInfo() {
        // 以json格式返回   响应结果 参数即为数据  数据格式为 json
        return ResponseEntity.success(monitoringService.queryAgentsInfo());
    }

    @Operation(summary = "agent instant info", description = "agent instant info query")
    @GetMapping("agentinstinfo")
    public ResponseEntity<JsonNode> queryAgentsInstStatus() {
        // 以json格式返回   响应结果 参数即为数据  数据格式为 json
        return ResponseEntity.success(monitoringService.queryAgentsInstStatus());
    }
}
