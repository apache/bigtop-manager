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

import org.apache.bigtop.manager.server.model.vo.ComponentVO;
import org.apache.bigtop.manager.server.service.ComponentService;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Cluster Component Controller")
@RestController
@RequestMapping("/clusters/{clusterId}/components")
public class ComponentController {

    @Resource
    private ComponentService componentService;

    @Operation(summary = "list", description = "List components")
    @GetMapping
    public ResponseEntity<List<ComponentVO>> list(@PathVariable Long clusterId) {
        return ResponseEntity.success(componentService.list(clusterId));
    }

    @Operation(summary = "get", description = "Get a component")
    @GetMapping("/{id}")
    public ResponseEntity<ComponentVO> get(@PathVariable Long id) {
        return ResponseEntity.success(componentService.get(id));
    }

}
