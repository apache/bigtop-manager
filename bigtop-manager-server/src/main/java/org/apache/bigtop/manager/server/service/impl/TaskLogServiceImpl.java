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
package org.apache.bigtop.manager.server.service.impl;

import org.apache.bigtop.manager.common.enums.JobState;
import org.apache.bigtop.manager.dao.po.TaskPO;
import org.apache.bigtop.manager.dao.repository.TaskRepository;
import org.apache.bigtop.manager.grpc.generated.TaskLogReply;
import org.apache.bigtop.manager.grpc.generated.TaskLogRequest;
import org.apache.bigtop.manager.grpc.generated.TaskLogServiceGrpc;
import org.apache.bigtop.manager.server.grpc.GrpcClient;
import org.apache.bigtop.manager.server.service.TaskLogService;

import org.springframework.stereotype.Service;

import io.grpc.stub.StreamObserver;
import reactor.core.publisher.FluxSink;

import jakarta.annotation.Resource;

@Service
public class TaskLogServiceImpl implements TaskLogService {

    @Resource
    private TaskRepository taskRepository;

    public void registerSink(Long taskId, FluxSink<String> sink) {
        TaskPO taskPO = taskRepository.getReferenceById(taskId);
        String hostname = taskPO.getHostname();

        if (taskPO.getState() == JobState.PENDING || taskPO.getState() == JobState.CANCELED) {
            new Thread(() -> {
                        sink.next("There is no log when task is in status: "
                                + taskPO.getState().name().toLowerCase()
                                + ", please reopen the window when status changed");
                        sink.complete();
                    })
                    .start();
        } else {
            TaskLogServiceGrpc.TaskLogServiceStub asyncStub =
                    GrpcClient.getAsyncStub(hostname, TaskLogServiceGrpc.TaskLogServiceStub.class);
            TaskLogRequest request =
                    TaskLogRequest.newBuilder().setTaskId(taskId).build();
            asyncStub.getLog(request, new LogReader(sink));
        }
    }

    private record LogReader(FluxSink<String> sink) implements StreamObserver<TaskLogReply> {

        @Override
        public void onNext(TaskLogReply reply) {
            sink.next(reply.getText());
        }

        @Override
        public void onError(Throwable t) {
            sink.error(t);
        }

        @Override
        public void onCompleted() {
            sink.complete();
        }
    }
}
