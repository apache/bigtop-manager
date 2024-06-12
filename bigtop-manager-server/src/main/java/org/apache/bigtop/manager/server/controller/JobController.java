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

import org.apache.bigtop.manager.server.model.vo.JobVO;
import org.apache.bigtop.manager.server.model.vo.PageVO;
import org.apache.bigtop.manager.server.service.JobService;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.Resource;

@Tag(name = "Job Controller")
@RestController
@RequestMapping("/clusters/{clusterId}/jobs")
public class JobController {

    @Resource
    private JobService jobService;

    @Operation(summary = "list", description = "List jobs")
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
    @GetMapping
    public ResponseEntity<PageVO<JobVO>> list(@PathVariable Long clusterId) {
        return ResponseEntity.success(jobService.list(clusterId));
    }

    @Operation(summary = "get", description = "Get a job")
    @GetMapping("/{id}")
    public ResponseEntity<JobVO> get(@PathVariable Long id, @PathVariable Long clusterId) {
        return ResponseEntity.success(jobService.get(id));
    }
}
