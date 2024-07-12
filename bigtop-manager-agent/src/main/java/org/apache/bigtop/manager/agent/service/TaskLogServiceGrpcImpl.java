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
import org.apache.bigtop.manager.agent.utils.LogFileUtils;
import org.apache.bigtop.manager.grpc.generated.TaskLogReply;
import org.apache.bigtop.manager.grpc.generated.TaskLogRequest;
import org.apache.bigtop.manager.grpc.generated.TaskLogServiceGrpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.io.RandomAccessFile;

@Slf4j
@GrpcService
public class TaskLogServiceGrpcImpl extends TaskLogServiceGrpc.TaskLogServiceImplBase {

    @Override
    public void getLog(TaskLogRequest request, StreamObserver<TaskLogReply> responseObserver) {
        String path = LogFileUtils.getLogFilePath(request.getTaskId());
        try (RandomAccessFile file = new RandomAccessFile(path, "r")) {
            // Read from beginning
            long fileLength = file.length();
            while (file.getFilePointer() < fileLength) {
                String line = file.readLine();
                if (line != null) {
                    responseObserver.onNext(
                            TaskLogReply.newBuilder().setText(line).build());
                }
            }

            // Waiting for new logs
            boolean isTaskRunning = true;
            while (isTaskRunning) {
                isTaskRunning = Caches.RUNNING_TASKS.contains(request.getTaskId());
                readNewLogs(file, responseObserver);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            String errMsg = "Error when reading task log: " + e.getMessage() + ", please fix it";
            responseObserver.onNext(TaskLogReply.newBuilder().setText(errMsg).build());

            log.error("Error reading task log", e);
            Status status = Status.UNKNOWN.withDescription(e.getMessage());
            responseObserver.onError(status.asRuntimeException());
        }
    }

    private void readNewLogs(RandomAccessFile file, StreamObserver<TaskLogReply> responseObserver) throws Exception {
        long position = file.getFilePointer();
        if (position < file.length()) {
            // Read new logs
            file.seek(position);
            if (file.readByte() != '\n') {
                file.seek(position);
            }

            String line = file.readLine();
            while (line != null) {
                responseObserver.onNext(TaskLogReply.newBuilder().setText(line).build());
                line = file.readLine();
            }
        }
    }
}
