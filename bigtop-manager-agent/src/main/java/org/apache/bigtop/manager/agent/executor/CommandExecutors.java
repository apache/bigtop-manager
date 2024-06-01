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
import org.apache.bigtop.manager.common.message.entity.command.CommandMessageType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandExecutors {

    private static final AtomicBoolean LOADED = new AtomicBoolean(false);

    private static final Map<CommandMessageType, String> COMMAND_EXECUTORS = new HashMap<>();

    public static CommandExecutor getCommandExecutor(CommandMessageType commandMessageType) {
        if (!LOADED.get()) {
            load();
        }

        String beanName = COMMAND_EXECUTORS.get(commandMessageType);
        return SpringContextHolder.getApplicationContext().getBean(beanName, CommandExecutor.class);
    }

    private static synchronized void load() {
        if (LOADED.get()) {
            return;
        }

        for (Map.Entry<String, CommandExecutor> entry : SpringContextHolder.getCommandExecutors().entrySet()) {
            String beanName = entry.getKey();
            CommandExecutor commandExecutor = entry.getValue();
            if (COMMAND_EXECUTORS.containsKey(commandExecutor.getCommandMessageType())) {
                log.error("Duplicate CommandExecutor with message type: {}", commandExecutor.getCommandMessageType());
                continue;
            }

            COMMAND_EXECUTORS.put(commandExecutor.getCommandMessageType(), beanName);
            log.info("Load JobRunner: {} with identifier: {}", commandExecutor.getClass().getName(),
                    commandExecutor.getCommandMessageType());
        }

        LOADED.set(true);
    }
}
