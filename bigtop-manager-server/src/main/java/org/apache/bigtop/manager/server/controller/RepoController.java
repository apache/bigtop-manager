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

import org.apache.bigtop.manager.server.model.converter.RepoConverter;
import org.apache.bigtop.manager.server.model.req.RepoReq;
import org.apache.bigtop.manager.server.model.vo.RepoVO;
import org.apache.bigtop.manager.server.service.RepoService;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.Resource;
import java.util.List;

@Tag(name = "Repo Controller")
@RestController
@RequestMapping("/repos")
public class RepoController {

    @Resource
    private RepoService repoService;

    @Operation(summary = "list", description = "List repos")
    @GetMapping
    public ResponseEntity<List<RepoVO>> list() {
        return ResponseEntity.success(repoService.list());
    }

    @Operation(summary = "update", description = "Update repos")
    @PutMapping
    public ResponseEntity<List<RepoVO>> update(@RequestBody List<RepoReq> repoReqList) {
        return ResponseEntity.success(repoService.update(RepoConverter.INSTANCE.fromReq2DTO(repoReqList)));
    }
}
