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
package org.apache.bigtop.manager.server.command.task;

import org.apache.bigtop.manager.common.constants.MessageConstants;
import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.grpc.generated.HostCheckReply;
import org.apache.bigtop.manager.grpc.generated.HostCheckRequest;
import org.apache.bigtop.manager.grpc.generated.HostCheckServiceGrpc;
import org.apache.bigtop.manager.server.grpc.GrpcClient;

public class HostCheckTask extends AbstractTask {

    public HostCheckTask(TaskContext taskContext) {
        super(taskContext);
    }

    @Override
    protected Command getCommand() {
        return Command.CUSTOM;
    }

    @Override
    protected String getCustomCommand() {
        return "check_host";
    }

    @Override
    protected Boolean doRun(String hostname, Integer grpcPort) {
        HostCheckRequest.Builder builder = HostCheckRequest.newBuilder();
        builder.setTaskId(getTaskPO().getId());
        HostCheckRequest request = builder.build();

        HostCheckServiceGrpc.HostCheckServiceBlockingStub stub =
                GrpcClient.getBlockingStub(hostname, grpcPort, HostCheckServiceGrpc.HostCheckServiceBlockingStub.class);
        HostCheckReply reply = stub.check(request);

        return reply != null && reply.getCode() == MessageConstants.SUCCESS_CODE;
    }

    @Override
    public String getName() {
        return "Check host " + taskContext.getHostname();
    }
}
