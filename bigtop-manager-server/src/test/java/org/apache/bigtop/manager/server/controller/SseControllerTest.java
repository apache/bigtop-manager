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
import org.apache.bigtop.manager.server.utils.MessageSourceUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import reactor.core.publisher.FluxSink;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SseControllerTest {

    @Mock
    private TaskLogService taskLogService;

    @InjectMocks
    private SseController sseController;

    private MockedStatic<MessageSourceUtils> mockedMessageSourceUtils;

    @BeforeEach
    void setUp() {
        mockedMessageSourceUtils = Mockito.mockStatic(MessageSourceUtils.class);
        when(MessageSourceUtils.getMessage(any())).thenReturn("Mocked message");
    }

    @AfterEach
    void tearDown() {
        mockedMessageSourceUtils.close();
    }

    @Test
    void logReturnsSseEmitter() {
        Long taskId = 1L;
        Long clusterId = 1L;
        doAnswer(invocation -> {
                    FluxSink<String> sink = invocation.getArgument(1);
                    sink.next("log message");
                    sink.complete();
                    return null;
                })
                .when(taskLogService)
                .registerSink(eq(taskId), any());

        SseEmitter emitter = sseController.log(taskId, clusterId);

        assertNotNull(emitter);
    }

    @Test
    void logHandlesExceptionDuringEmission() {
        Long taskId = 1L;
        Long clusterId = 1L;
        doAnswer(invocation -> {
                    FluxSink<String> sink = invocation.getArgument(1);
                    sink.error(new RuntimeException("Test exception"));
                    return null;
                })
                .when(taskLogService)
                .registerSink(eq(taskId), any());

        SseEmitter emitter = sseController.log(taskId, clusterId);

        assertNotNull(emitter);
    }

    @Test
    void logCompletes() {
        Long taskId = 1L;
        Long clusterId = 1L;
        doAnswer(invocation -> {
                    FluxSink<String> sink = invocation.getArgument(1);
                    sink.complete();
                    return null;
                })
                .when(taskLogService)
                .registerSink(eq(taskId), any());

        SseEmitter emitter = sseController.log(taskId, clusterId);

        assertNotNull(emitter);
    }
}
