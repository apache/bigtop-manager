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

import org.apache.bigtop.manager.dao.query.ServiceQuery;
import org.apache.bigtop.manager.server.model.req.ServiceConfigReq;
import org.apache.bigtop.manager.server.model.req.ServiceConfigSnapshotReq;
import org.apache.bigtop.manager.server.model.vo.PageVO;
import org.apache.bigtop.manager.server.model.vo.ServiceConfigSnapshotVO;
import org.apache.bigtop.manager.server.model.vo.ServiceConfigVO;
import org.apache.bigtop.manager.server.model.vo.ServiceVO;
import org.apache.bigtop.manager.server.service.ServiceService;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.List;

@Slf4j
@Tag(name = "Cluster Service Controller")
@RestController
@RequestMapping("/clusters/{clusterId}/services")
public class ServiceController {

    @Resource
    private ServiceService serviceService;

    @Parameters({
        @Parameter(in = ParameterIn.QUERY, name = "pageNum", schema = @Schema(type = "integer", defaultValue = "1")),
        @Parameter(in = ParameterIn.QUERY, name = "pageSize", schema = @Schema(type = "integer", defaultValue = "10")),
        @Parameter(in = ParameterIn.QUERY, name = "orderBy", schema = @Schema(type = "string", defaultValue = "id")),
        @Parameter(
                in = ParameterIn.QUERY,
                name = "sort",
                description = "asc/desc",
                schema = @Schema(type = "string", defaultValue = "asc"))
    })
    @Operation(summary = "list", description = "List hosts")
    @GetMapping
    public ResponseEntity<PageVO<ServiceVO>> list(@PathVariable Long clusterId, @RequestBody ServiceQuery query) {
        return ResponseEntity.success(serviceService.list(query));
    }

    @Operation(summary = "get", description = "Get a service")
    @GetMapping("/{id}")
    public ResponseEntity<ServiceVO> get(@PathVariable Long clusterId, @PathVariable Long id) {
        return ResponseEntity.success(serviceService.get(id));
    }

    @Operation(summary = "list service configs", description = "List service configs")
    @GetMapping("/{id}/configs")
    public ResponseEntity<List<ServiceConfigVO>> listConf(@PathVariable Long clusterId, @PathVariable Long id) {
        return ResponseEntity.success(serviceService.listConf(clusterId, id));
    }

    @Operation(summary = "update service configs", description = "Update service configs")
    @PostMapping("/{id}/configs")
    public ResponseEntity<List<ServiceConfigVO>> updateConf(
            @PathVariable Long clusterId, @PathVariable Long id, @RequestBody List<ServiceConfigReq> reqs) {
        return ResponseEntity.success(serviceService.updateConf(clusterId, id, reqs));
    }

    @Operation(summary = "list config snapshots", description = "List config snapshots")
    @GetMapping("/{id}/config-snapshots")
    public ResponseEntity<List<ServiceConfigSnapshotVO>> listConfSnapshot(
            @PathVariable Long clusterId, @PathVariable Long id) {
        return ResponseEntity.success(serviceService.listConfSnapshots(clusterId, id));
    }

    @Operation(summary = "take config snapshot", description = "Take config snapshot")
    @PostMapping("/{id}/config-snapshots")
    public ResponseEntity<ServiceConfigSnapshotVO> takeConfSnapshot(
            @PathVariable Long clusterId, @PathVariable Long id, @RequestBody ServiceConfigSnapshotReq req) {
        return ResponseEntity.success(serviceService.takeConfSnapshot(clusterId, id, req));
    }

    @Operation(summary = "recovery config snapshot", description = "Recovery config snapshot")
    @PostMapping("/{id}/config-snapshots/{snapshotId}")
    public ResponseEntity<List<ServiceConfigVO>> recoveryConfSnapshot(
            @PathVariable Long clusterId, @PathVariable Long id, @PathVariable Long snapshotId) {
        return ResponseEntity.success(serviceService.recoveryConfSnapshot(clusterId, id, snapshotId));
    }

    @Operation(summary = "delete config snapshot", description = "Delete config snapshot")
    @DeleteMapping("/{id}/config-snapshots/{snapshotId}")
    public ResponseEntity<Boolean> deleteConfSnapshot(
            @PathVariable Long clusterId, @PathVariable Long id, @PathVariable Long snapshotId) {
        return ResponseEntity.success(serviceService.deleteConfSnapshot(clusterId, id, snapshotId));
    }
}
