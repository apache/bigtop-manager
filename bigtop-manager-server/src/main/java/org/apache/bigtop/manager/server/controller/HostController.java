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

import org.apache.bigtop.manager.server.model.dto.HostDTO;
import org.apache.bigtop.manager.server.model.converter.HostConverter;
import org.apache.bigtop.manager.server.model.req.HostReq;
import org.apache.bigtop.manager.server.model.req.HostnamesReq;
import org.apache.bigtop.manager.server.model.vo.HostVO;
import org.apache.bigtop.manager.server.service.HostService;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.Resource;
import java.util.List;

@Tag(name = "Cluster Host Controller")
@RestController
@RequestMapping("/clusters/{clusterId}/hosts")
public class HostController {

    @Resource
    private HostService hostService;

    @Operation(summary = "list", description = "List hosts")
    @GetMapping
    public ResponseEntity<List<HostVO>> list(@PathVariable Long clusterId) {
        return ResponseEntity.success(hostService.list(clusterId));
    }

    @Operation(summary = "get", description = "Get a host")
    // @GetMapping("/{id}")
    public ResponseEntity<HostVO> get(@PathVariable Long id) {
        return ResponseEntity.success(hostService.get(id));
    }

    @Operation(summary = "update", description = "Update a host")
    // @PutMapping("/{id}")
    public ResponseEntity<HostVO> update(@PathVariable Long id, @RequestBody @Validated HostReq hostReq) {
        HostDTO hostDTO = HostConverter.INSTANCE.fromReq2DTO(hostReq);
        return ResponseEntity.success(hostService.update(id, hostDTO));
    }

    @Operation(summary = "delete", description = "Delete a host")
    // @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable Long id) {
        return ResponseEntity.success(hostService.delete(id));
    }

    @Operation(summary = "Check connection", description = "Check connection for hosts")
    @PostMapping("/check-connection")
    public ResponseEntity<Boolean> checkConnection(
            @PathVariable Long clusterId, @RequestBody @Validated HostnamesReq hostnamesReq) {
        return ResponseEntity.success(hostService.checkConnection(hostnamesReq.getHostnames()));
    }
}
