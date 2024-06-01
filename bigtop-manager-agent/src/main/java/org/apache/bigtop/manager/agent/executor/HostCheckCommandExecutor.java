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
import org.apache.bigtop.manager.common.message.entity.command.CommandMessageType;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.common.utils.os.TimeSyncDetection;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class HostCheckCommandExecutor extends AbstractCommandExecutor {

    @Override
    public CommandMessageType getCommandMessageType() {
        return CommandMessageType.HOST_CHECK;
    }

    @Override
    public void doExecute() {
        ShellResult shellResult = runChecks(List.of(this::checkTimeSync));
        commandResponseMessage.setCode(shellResult.getExitCode());
        commandResponseMessage.setResult(shellResult.getResult());
    }

    private ShellResult runChecks(List<Supplier<ShellResult>> suppliers) {
        ShellResult shellResult = ShellResult.success();
        for (Supplier<ShellResult> supplier : suppliers) {
            shellResult = supplier.get();
            if (shellResult.getExitCode() != MessageConstants.SUCCESS_CODE) {
                return shellResult;
            }
        }

        return shellResult;
    }

    private ShellResult checkTimeSync() {
        ShellResult shellResult = TimeSyncDetection.checkTimeSync();
        log.info("Time sync check result: {}", shellResult.getResult());

        return shellResult;
    }
}
