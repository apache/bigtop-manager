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

import org.apache.bigtop.manager.agent.executor.CommandExecutor;
import org.apache.bigtop.manager.agent.executor.CommandExecutors;
import org.apache.bigtop.manager.grpc.generated.CommandReply;
import org.apache.bigtop.manager.grpc.generated.CommandRequest;
import org.apache.bigtop.manager.grpc.generated.CommandServiceGrpc;

import org.slf4j.MDC;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
public class CommandServiceGrpcImpl extends CommandServiceGrpc.CommandServiceImplBase {

    @Override
    public void exec(CommandRequest request, StreamObserver<CommandReply> responseObserver) {
        try {
            MDC.put("taskId", String.valueOf(request.getTaskId()));
            CommandExecutor commandExecutor = CommandExecutors.getCommandExecutor(request.getType());
            CommandReply reply = commandExecutor.execute(request);
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error when running command", e);
            Status status = Status.UNKNOWN.withDescription(e.getMessage());
            responseObserver.onError(status.asRuntimeException());
        } finally {
            MDC.clear();
        }
    }
}
