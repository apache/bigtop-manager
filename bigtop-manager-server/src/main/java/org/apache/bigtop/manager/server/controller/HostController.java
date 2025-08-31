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

import org.apache.bigtop.manager.dao.query.HostQuery;
import org.apache.bigtop.manager.server.model.converter.HostConverter;
import org.apache.bigtop.manager.server.model.dto.HostDTO;
import org.apache.bigtop.manager.server.model.req.HostReq;
import org.apache.bigtop.manager.server.model.req.IdsReq;
import org.apache.bigtop.manager.server.model.vo.ComponentVO;
import org.apache.bigtop.manager.server.model.vo.HostVO;
import org.apache.bigtop.manager.server.model.vo.InstalledStatusVO;
import org.apache.bigtop.manager.server.model.vo.PageVO;
import org.apache.bigtop.manager.server.service.HostService;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "Host Controller")
@RestController
@RequestMapping("/hosts")
public class HostController {

    @Resource
    private HostService hostService;

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
    public ResponseEntity<PageVO<HostVO>> list(HostQuery hostQuery) {
        return ResponseEntity.success(hostService.list(hostQuery));
    }

    @Operation(summary = "add", description = "Add a host")
    @PostMapping
    public ResponseEntity<List<HostVO>> add(@RequestBody @Validated HostReq hostReq) {
        HostDTO hostDTO = HostConverter.INSTANCE.fromReq2DTO(hostReq);
        return ResponseEntity.success(hostService.add(hostDTO));
    }

    @Operation(summary = "get", description = "Get a host")
    @GetMapping("/{id}")
    public ResponseEntity<HostVO> get(@PathVariable Long id) {
        return ResponseEntity.success(hostService.get(id));
    }

    @Operation(summary = "update", description = "Update a host")
    @PutMapping("/{id}")
    public ResponseEntity<HostVO> update(@PathVariable Long id, @RequestBody @Validated HostReq hostReq) {
        HostDTO hostDTO = HostConverter.INSTANCE.fromReq2DTO(hostReq);
        return ResponseEntity.success(hostService.update(id, hostDTO));
    }

    @Operation(summary = "remove", description = "Remove a host")
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> remove(@PathVariable Long id) {
        return ResponseEntity.success(hostService.remove(id));
    }

    @Operation(summary = "components", description = "Get host components")
    @GetMapping("/{id}/components")
    public ResponseEntity<List<ComponentVO>> components(@PathVariable Long id) {
        return ResponseEntity.success(hostService.components(id));
    }

    @Operation(summary = "batch remove", description = "Remove hosts")
    @DeleteMapping("/batch")
    public ResponseEntity<Boolean> batchRemove(@RequestBody IdsReq req) {
        return ResponseEntity.success(hostService.batchRemove(req.getIds()));
    }

    @Operation(summary = "Check connection", description = "Check connection for hosts")
    @PostMapping("/check-connection")
    public ResponseEntity<Boolean> checkConnection(@RequestBody @Validated HostReq hostReq) {
        HostDTO hostDTO = HostConverter.INSTANCE.fromReq2DTO(hostReq);
        return ResponseEntity.success(hostService.checkConnection(hostDTO));
    }

    @Operation(summary = "Install dependencies", description = "Install dependencies on a host")
    @PostMapping("/install-dependencies")
    public ResponseEntity<Boolean> installDependencies(@RequestBody @Validated List<HostReq> hostReqs) {
        List<HostDTO> hostDTOList = new ArrayList<>();
        hostReqs.forEach(hostReq -> hostDTOList.add(HostConverter.INSTANCE.fromReq2DTO(hostReq)));
        return ResponseEntity.success(hostService.installDependencies(hostDTOList));
    }

    @Operation(summary = "Installed status", description = "Install status for a host")
    @GetMapping("/installed-status")
    public ResponseEntity<List<InstalledStatusVO>> installedStatus() {
        return ResponseEntity.success(hostService.installedStatus());
    }

    @Operation(summary = "Start agent", description = "Start agent on the host")
    @PostMapping("/{id}/start-agent")
    public ResponseEntity<Boolean> startAgent(@PathVariable("id") Long id) {
        return ResponseEntity.success(hostService.startAgent(id));
    }

    @Operation(summary = "Stop agent", description = "Stop agent on the host")
    @PostMapping("/{id}/stop-agent")
    public ResponseEntity<Boolean> stopAgent(@PathVariable("id") Long id) {
        return ResponseEntity.success(hostService.stopAgent(id));
    }

    @Operation(summary = "Restart agent", description = "Restart agent on the host")
    @PostMapping("/{id}/restart-agent")
    public ResponseEntity<Boolean> restartAgent(@PathVariable("id") Long id) {
        return ResponseEntity.success(hostService.restartAgent(id));
    }

    @Operation(summary = "check-duplicate", description = "check hostname duplicate")
    @PostMapping("/check-duplicate")
    public ResponseEntity<Boolean> checkDuplicate(@RequestBody @Validated HostReq hostReq) {
        HostDTO hostDTO = HostConverter.INSTANCE.fromReq2DTO(hostReq);
        return ResponseEntity.success(hostService.checkDuplicate(hostDTO));
    }
}
