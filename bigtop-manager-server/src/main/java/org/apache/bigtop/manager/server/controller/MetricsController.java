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

import org.apache.bigtop.manager.server.service.MetricsService;
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

@Tag(name = "Metrics Controller")
@RestController
@RequestMapping("metrics")
public class MetricsController {

    @Resource
    private MetricsService metricsService;

    @Operation(summary = "hosts healthy", description = "hosts healthy check")
    @GetMapping("hostshealthy")
    public ResponseEntity<JsonNode> agentHostsHealthyStatus() {
        return ResponseEntity.success(metricsService.queryAgentsHealthyStatus());
    }

    @Operation(summary = "host info", description = "host info query")
    @GetMapping("hosts/{id}")
    public ResponseEntity<JsonNode> queryAgentInfo(
            @RequestParam(value = "interval", defaultValue = "1m") String interval, @PathVariable String id) {
        return ResponseEntity.success(metricsService.queryAgentsInfo(Long.valueOf(id), interval));
    }

    @Operation(summary = "cluster info", description = "cluster info query")
    @GetMapping("clusters/{id}")
    public ResponseEntity<JsonNode> queryCluster(
            @RequestParam(value = "interval", defaultValue = "1m") String interval, @PathVariable String id) {
        return ResponseEntity.success(metricsService.queryClustersInfo(Long.valueOf(id), interval));
    }
}
