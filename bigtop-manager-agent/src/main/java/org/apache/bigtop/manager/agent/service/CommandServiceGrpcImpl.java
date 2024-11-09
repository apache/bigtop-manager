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
package org.apache.bigtop.manager.agent.service;

import org.apache.bigtop.manager.agent.cache.Caches;
import org.apache.bigtop.manager.agent.executor.CommandExecutor;
import org.apache.bigtop.manager.agent.executor.CommandExecutors;
import org.apache.bigtop.manager.common.utils.ProjectPathUtils;
import org.apache.bigtop.manager.grpc.generated.CommandReply;
import org.apache.bigtop.manager.grpc.generated.CommandRequest;
import org.apache.bigtop.manager.grpc.generated.CommandServiceGrpc;

import org.slf4j.MDC;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

@Slf4j
@GrpcService
public class CommandServiceGrpcImpl extends CommandServiceGrpc.CommandServiceImplBase {

    @Override
    public void exec(CommandRequest request, StreamObserver<CommandReply> responseObserver) {
        try {
            // Truncate old logs if exists, only useful when it's retry command
            truncateLogFile(request.getTaskId());

            MDC.put("taskId", String.valueOf(request.getTaskId()));
            Caches.RUNNING_TASKS.add(request.getTaskId());
            CommandExecutor commandExecutor = CommandExecutors.getCommandExecutor(request.getType());
            CommandReply reply = commandExecutor.execute(request);
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error when running command", e);
            Status status = Status.UNKNOWN.withDescription(e.getMessage());
            responseObserver.onError(status.asRuntimeException());
        } finally {
            Caches.RUNNING_TASKS.remove(request.getTaskId());
            MDC.clear();
        }
    }

    private void truncateLogFile(Long taskId) {
        String filePath = ProjectPathUtils.getLogFilePath(taskId);
        File file = new File(filePath);
        if (file.exists()) {
            try (RandomAccessFile rf = new RandomAccessFile(file, "rw")) {
                rf.setLength(0);
            } catch (IOException e) {
                log.warn("Error when truncate file: {}", filePath, e);
            }
        }
    }
}
