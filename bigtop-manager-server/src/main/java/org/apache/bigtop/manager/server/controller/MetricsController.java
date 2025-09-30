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

import org.apache.bigtop.manager.server.model.vo.ClusterMetricsVO;
import org.apache.bigtop.manager.server.model.vo.HostMetricsVO;
import org.apache.bigtop.manager.server.model.vo.ServiceMetricsVO;
import org.apache.bigtop.manager.server.service.MetricsService;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.Resource;

@Tag(name = "Metrics Controller")
@RestController
@RequestMapping("/metrics")
public class MetricsController {

    @Resource
    private MetricsService metricsService;

    @Operation(summary = "host metrics", description = "host metrics")
    @GetMapping("/hosts/{id}")
    public ResponseEntity<HostMetricsVO> hostMetrics(
            @RequestParam(value = "interval", defaultValue = "1m") String interval, @PathVariable Long id) {
        return ResponseEntity.success(metricsService.hostMetrics(id, interval));
    }

    @Operation(summary = "cluster metrics", description = "cluster metrics")
    @GetMapping("/clusters/{id}")
    public ResponseEntity<ClusterMetricsVO> clusterMetrics(
            @RequestParam(value = "interval", defaultValue = "1m") String interval, @PathVariable Long id) {
        return ResponseEntity.success(metricsService.clusterMetrics(id, interval));
    }

    @Operation(summary = "service metrics", description = "service metrics")
    @GetMapping("/services/{id}")
    public ResponseEntity<ServiceMetricsVO> serviceMetrics(
            @RequestParam(value = "interval", defaultValue = "1m") String interval, @PathVariable Long id) {
        return ResponseEntity.success(metricsService.serviceMetrics(id, interval));
    }
}
