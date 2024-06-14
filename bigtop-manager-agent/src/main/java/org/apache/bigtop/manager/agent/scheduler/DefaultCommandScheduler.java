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
package org.apache.bigtop.manager.agent.scheduler;

import org.apache.bigtop.manager.agent.executor.CommandExecutor;
import org.apache.bigtop.manager.agent.executor.CommandExecutors;
import org.apache.bigtop.manager.common.message.entity.command.CommandRequestMessage;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
public class DefaultCommandScheduler implements CommandScheduler {

    private final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    private final Executor executor = Executors.newSingleThreadExecutor();

    private volatile boolean running = true;

    @Override
    public void submit(CommandRequestMessage message) {
        queue.offer(() -> {
            try {
                CommandExecutor commandExecutor = CommandExecutors.getCommandExecutor(message.getCommandMessageType());
                commandExecutor.execute(message);
            } catch (Exception e) {
                log.error("Error when running command", e);
            }
        });
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }

    @PostConstruct
    public void init() {
        executor.execute(() -> {
            while (running) {
                try {
                    Runnable runnable = queue.take();
                    runnable.run();
                } catch (InterruptedException e) {
                    log.warn("Error when running command", e);
                }
            }
        });
    }
}
