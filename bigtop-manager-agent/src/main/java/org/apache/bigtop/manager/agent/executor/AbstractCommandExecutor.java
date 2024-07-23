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
package org.apache.bigtop.manager.agent.executor;

import org.apache.bigtop.manager.common.constants.MessageConstants;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.common.utils.Environments;
import org.apache.bigtop.manager.grpc.generated.CommandReply;
import org.apache.bigtop.manager.grpc.generated.CommandRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractCommandExecutor implements CommandExecutor {

    protected CommandRequest commandRequest;

    protected CommandReply.Builder commandReplyBuilder;

    @Override
    public CommandReply execute(CommandRequest request) {
        log.info("Running task: {}", request.getTaskId());
        commandRequest = request;
        commandReplyBuilder = CommandReply.newBuilder();

        try {
            if (Environments.isDevMode()) {
                doExecuteOnDevMode();
            } else {
                doExecute();
            }
        } catch (Exception e) {
            commandReplyBuilder.setCode(MessageConstants.FAIL_CODE);
            commandReplyBuilder.setResult(e.getMessage());

            log.error("Run command failed, {}", request, e);
        }

        commandReplyBuilder.setType(request.getType());
        commandReplyBuilder.setHostname(request.getHostname());
        commandReplyBuilder.setTaskId(request.getTaskId());
        commandReplyBuilder.setStageId(request.getStageId());
        commandReplyBuilder.setJobId(request.getJobId());
        return commandReplyBuilder.build();
    }

    protected void doExecuteOnDevMode() {
        log.info("Running command on dev mode");
        commandReplyBuilder.setCode(MessageConstants.SUCCESS_CODE);
        commandReplyBuilder.setResult(ShellResult.success().getResult());
    }

    protected abstract void doExecute();
}
