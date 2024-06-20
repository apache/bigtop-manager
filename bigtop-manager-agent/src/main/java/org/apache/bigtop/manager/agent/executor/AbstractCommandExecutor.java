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

import org.apache.bigtop.manager.agent.holder.SpringContextHolder;
import org.apache.bigtop.manager.common.constants.MessageConstants;
import org.apache.bigtop.manager.common.message.entity.command.CommandRequestMessage;
import org.apache.bigtop.manager.common.message.entity.command.CommandResponseMessage;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.common.utils.Environments;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractCommandExecutor implements CommandExecutor {

    protected CommandRequestMessage commandRequestMessage;

    protected CommandResponseMessage commandResponseMessage;

    @Override
    public void execute(CommandRequestMessage message) {
        commandRequestMessage = message;
        commandResponseMessage = new CommandResponseMessage();

        try {
            if (Environments.isDevMode()) {
                doExecuteOnDevMode();
            } else {
                doExecute();
            }
        } catch (Exception e) {
            commandResponseMessage.setCode(MessageConstants.FAIL_CODE);
            commandResponseMessage.setResult(e.getMessage());

            log.error("Run command failed, {}", message, e);
        }

        commandResponseMessage.setCommandMessageType(message.getCommandMessageType());
        commandResponseMessage.setMessageId(message.getMessageId());
        commandResponseMessage.setHostname(message.getHostname());
        commandResponseMessage.setTaskId(message.getTaskId());
        commandResponseMessage.setStageId(message.getStageId());
        commandResponseMessage.setJobId(message.getJobId());
        SpringContextHolder.getAgentWebSocket().sendMessage(commandResponseMessage);
    }

    protected void doExecuteOnDevMode() {
        commandResponseMessage.setCode(MessageConstants.SUCCESS_CODE);
        commandResponseMessage.setResult(ShellResult.success().getResult());
    }

    protected abstract void doExecute();
}
