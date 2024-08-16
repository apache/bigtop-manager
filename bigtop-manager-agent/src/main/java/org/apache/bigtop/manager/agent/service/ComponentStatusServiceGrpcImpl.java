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

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.message.entity.payload.CommandPayload;
import org.apache.bigtop.manager.common.message.entity.pojo.ScriptInfo;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.grpc.generated.ComponentStatusReply;
import org.apache.bigtop.manager.grpc.generated.ComponentStatusRequest;
import org.apache.bigtop.manager.grpc.generated.ComponentStatusServiceGrpc;
import org.apache.bigtop.manager.stack.core.executor.StackExecutor;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
public class ComponentStatusServiceGrpcImpl extends ComponentStatusServiceGrpc.ComponentStatusServiceImplBase {

    @Override
    public void getComponentStatus(
            ComponentStatusRequest request, StreamObserver<ComponentStatusReply> responseObserver) {

        try {
            CommandPayload commandPayload = new CommandPayload();
            commandPayload.setCommand(Command.STATUS);
            commandPayload.setServiceName(request.getServiceName());
            commandPayload.setServiceUser(request.getServiceUser());
            commandPayload.setComponentName(request.getComponentName());
            commandPayload.setCommandScript(JsonUtils.readFromString(request.getCommandScript(), ScriptInfo.class));
            commandPayload.setRoot(request.getRoot());
            commandPayload.setStackName(request.getStackName());
            commandPayload.setStackVersion(request.getStackVersion());
            ShellResult shellResult = StackExecutor.execute(commandPayload);

            ComponentStatusReply reply = ComponentStatusReply.newBuilder()
                    .setStatus(shellResult.getExitCode())
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting component status", e);
            Status status = Status.UNKNOWN.withDescription(e.getMessage());
            responseObserver.onError(status.asRuntimeException());
        }
    }
}
