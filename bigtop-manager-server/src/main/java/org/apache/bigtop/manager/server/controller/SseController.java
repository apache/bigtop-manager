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

import org.apache.bigtop.manager.server.service.TaskLogService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import jakarta.annotation.Resource;

@Tag(name = "Sse Controller")
@RestController
@RequestMapping("/sse/clusters/{clusterId}")
public class SseController {

    @Resource
    private TaskLogService taskLogService;

    @Operation(summary = "get task log", description = "Get a task log")
    @GetMapping("/tasks/{id}/log")
    public SseEmitter log(@PathVariable Long id, @PathVariable Long clusterId) {
        // Default timeout to 5 minutes
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        Flux<String> flux =
                Flux.create(sink -> taskLogService.registerSink(id, sink), FluxSink.OverflowStrategy.BUFFER);
        flux.subscribe(
                s -> {
                    try {
                        emitter.send(s);
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                },
                Throwable::printStackTrace,
                emitter::complete);

        emitter.onTimeout(emitter::complete);
        return emitter;
    }
}
