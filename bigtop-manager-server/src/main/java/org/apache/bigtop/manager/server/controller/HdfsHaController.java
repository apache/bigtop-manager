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
import org.apache.bigtop.manager.server.model.req.EnableHdfsHaReq;
import org.apache.bigtop.manager.server.model.vo.CommandVO;
import org.apache.bigtop.manager.server.service.CommandService;
import org.apache.bigtop.manager.server.service.HdfsHaService;
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

@Tag(name = "HDFS HA Controller")
@RestController
@RequestMapping("/clusters/{clusterId}/services/{serviceId}/actions")
public class HdfsHaController {

    @Resource
    private HdfsHaService hdfsHaService;

    @Resource
    private CommandService commandService;

    @Audit
    @Operation(summary = "enableHdfsHa", description = "Enable HDFS HA for an existing single NameNode cluster")
    @PostMapping("/enable-hdfs-ha")
    public ResponseEntity<CommandVO> enableHdfsHa(
            @PathVariable Long clusterId, @PathVariable Long serviceId, @RequestBody @Valid EnableHdfsHaReq req) {
        return ResponseEntity.success(hdfsHaService.buildEnableHdfsHaCommand(clusterId, serviceId, req));
    }
}
