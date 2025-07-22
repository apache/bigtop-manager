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
package org.apache.bigtop.manager.agent.grpc.service;

import org.apache.bigtop.manager.agent.cache.Caches;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.grpc.generated.ComponentCommandReply;
import org.apache.bigtop.manager.grpc.generated.ComponentCommandRequest;
import org.apache.bigtop.manager.grpc.generated.ComponentCommandServiceGrpc;
import org.apache.bigtop.manager.grpc.payload.ComponentCommandPayload;
import org.apache.bigtop.manager.stack.core.executor.StackExecutor;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
public class ComponentCommandServiceGrpcImpl extends ComponentCommandServiceGrpc.ComponentCommandServiceImplBase {

    @Override
    public void exec(ComponentCommandRequest request, StreamObserver<ComponentCommandReply> responseObserver) {
        try {
            log.info("Running task {}", Caches.RUNNING_TASK);
            ComponentCommandPayload payload =
                    JsonUtils.readFromString(request.getPayload(), ComponentCommandPayload.class);
            ShellResult shellResult = StackExecutor.execute(payload);
            ComponentCommandReply reply = ComponentCommandReply.newBuilder()
                    .setCode(shellResult.getExitCode())
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
