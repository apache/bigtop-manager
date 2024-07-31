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

import org.apache.bigtop.manager.server.model.converter.ClusterConverter;
import org.apache.bigtop.manager.server.model.dto.ClusterDTO;
import org.apache.bigtop.manager.server.model.req.ClusterReq;
import org.apache.bigtop.manager.server.model.vo.ClusterVO;
import org.apache.bigtop.manager.server.service.ClusterService;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.Resource;
import java.util.List;

@Tag(name = "Cluster Controller")
@RestController
@RequestMapping("/clusters")
public class ClusterController {

    @Resource
    private ClusterService clusterService;

    @Operation(summary = "list", description = "List clusters")
    @GetMapping
    public ResponseEntity<List<ClusterVO>> list() {
        return ResponseEntity.success(clusterService.list());
    }

    @Operation(summary = "get", description = "Get a cluster")
    @GetMapping("/{id}")
    public ResponseEntity<ClusterVO> get(@PathVariable Long id) {
        return ResponseEntity.success(clusterService.get(id));
    }

    @Operation(summary = "update", description = "Update a cluster")
    @PutMapping("/{id}")
    public ResponseEntity<ClusterVO> update(@PathVariable Long id, @RequestBody @Validated ClusterReq clusterReq) {
        ClusterDTO clusterDTO = ClusterConverter.INSTANCE.fromReq2DTO(clusterReq);
        return ResponseEntity.success(clusterService.update(id, clusterDTO));
    }
}
