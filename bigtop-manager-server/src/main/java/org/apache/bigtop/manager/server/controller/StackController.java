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

import org.apache.bigtop.manager.server.model.vo.ClusterVO;
import org.apache.bigtop.manager.server.model.vo.StackVO;
import org.apache.bigtop.manager.server.service.StackService;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.Resource;
import java.util.List;

@Tag(name = "Stack Controller")
@RestController
@RequestMapping("/stacks")
public class StackController {

    @Resource
    private StackService stackService;

    @Operation(summary = "list", description = "List stacks")
    @GetMapping
    public ResponseEntity<List<StackVO>> list() {
        return ResponseEntity.success(stackService.list());
    }

    @Operation(summary = "service clusters", description = "Get service clusters")
    @GetMapping("/services/{serviceName}/clusters")
    public ResponseEntity<List<ClusterVO>> serviceClusters(@PathVariable String serviceName) {
        return ResponseEntity.success(stackService.serviceClusters(serviceName));
    }
}
