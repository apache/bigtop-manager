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

import org.apache.bigtop.manager.server.annotations.Audit;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.req.EnableYarnRmHaReq;
import org.apache.bigtop.manager.server.model.vo.CommandVO;
import org.apache.bigtop.manager.server.service.CommandService;
import org.apache.bigtop.manager.server.service.YarnHaService;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

@Tag(name = "YARN HA Controller")
@RestController
@RequestMapping("/clusters/{clusterId}/services/{serviceId}/actions")
public class YarnHaController {

    @Resource
    private YarnHaService yarnHaService;

    @Resource
    private CommandService commandService;

    @Audit
    @Operation(summary = "enableYarnRmHa", description = "Enable YARN ResourceManager HA")
    @PostMapping("/enable-yarn-rm-ha")
    public ResponseEntity<CommandVO> enableYarnRmHa(
            @PathVariable Long clusterId, @PathVariable Long serviceId, @RequestBody @Valid EnableYarnRmHaReq req) {

        CommandDTO commandDTO = yarnHaService.buildEnableYarnRmHaCommand(clusterId, serviceId, req);
        return ResponseEntity.success(commandService.command(commandDTO));
    }
}
