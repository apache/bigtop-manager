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

import org.apache.bigtop.manager.common.message.entity.payload.CommandPayload;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.grpc.generated.CommandType;
import org.apache.bigtop.manager.stack.core.executor.StackExecutor;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ComponentCommandExecutor extends AbstractCommandExecutor {

    @Override
    public CommandType getCommandType() {
        return CommandType.COMPONENT;
    }

    @Override
    protected void doExecuteOnDevMode() {
        doExecute();
    }

    @Override
    public void doExecute() {
        CommandPayload commandPayload = JsonUtils.readFromString(commandRequest.getPayload(), CommandPayload.class);
        log.info("[agent executeTask] taskEvent is: {}", commandRequest);
        ShellResult shellResult = StackExecutor.execute(commandPayload);

        commandReplyBuilder.setCode(shellResult.getExitCode());
        commandReplyBuilder.setResult(shellResult.getResult());
    }
}
