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
package org.apache.bigtop.manager.server.command.stage.runner;

import org.apache.bigtop.manager.common.constants.MessageConstants;
import org.apache.bigtop.manager.common.enums.JobState;
import org.apache.bigtop.manager.common.message.entity.command.CommandRequestMessage;
import org.apache.bigtop.manager.common.message.entity.command.CommandResponseMessage;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.entity.Stage;
import org.apache.bigtop.manager.dao.entity.Task;
import org.apache.bigtop.manager.dao.repository.StageRepository;
import org.apache.bigtop.manager.dao.repository.TaskRepository;
import org.apache.bigtop.manager.server.command.stage.factory.StageContext;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.service.CommandLogService;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.apache.bigtop.manager.common.constants.Constants.COMMAND_MESSAGE_RESPONSE_TIMEOUT;

@Slf4j
public abstract class AbstractStageRunner implements StageRunner {

    @Resource
    private CommandLogService commandLogService;

    @Resource
    protected StageRepository stageRepository;

    @Resource
    protected TaskRepository taskRepository;

    protected Stage stage;

    protected StageContext stageContext;

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void setStageContext(StageContext stageContext) {
        this.stageContext = stageContext;
    }

    @Override
    public void run() {
        beforeRun();

        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (Task task : stage.getTasks()) {
            beforeRunTask(task);

            CommandRequestMessage message = JsonUtils.readFromString(task.getContent(), CommandRequestMessage.class);
            message.setTaskId(task.getId());
            message.setStageId(stage.getId());
            message.setJobId(stage.getJob().getId());

            futures.add(CompletableFuture.supplyAsync(() -> {
                commandLogService.onLogStarted(task.getId(), task.getHostname());
                CommandResponseMessage res =
                        SpringContextHolder.getServerWebSocket().sendRequestMessage(task.getHostname(), message);

                log.info("Execute task {} completed: {}", task.getId(), res);
                boolean taskSuccess = res != null && res.getCode() == MessageConstants.SUCCESS_CODE;

                if (taskSuccess) {
                    onTaskSuccess(task);
                } else {
                    onTaskFailure(task);
                }

                commandLogService.onLogEnded(task.getId(), task.getHostname());
                return taskSuccess;
            }));
        }

        List<Boolean> taskResults = futures.stream()
                .map((future) -> {
                    try {
                        return future.get(COMMAND_MESSAGE_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("Error running task", e);
                        return false;
                    }
                })
                .toList();

        boolean allTaskSuccess = taskResults.stream().allMatch(Boolean::booleanValue);
        if (allTaskSuccess) {
            onSuccess();
        } else {
            onFailure();
        }
    }

    @Override
    public void beforeRun() {
        stage.setState(JobState.PROCESSING);
        stageRepository.save(stage);
    }

    @Override
    public void onSuccess() {
        stage.setState(JobState.SUCCESSFUL);
        stageRepository.save(stage);
    }

    @Override
    public void onFailure() {
        stage.setState(JobState.FAILED);
        stageRepository.save(stage);
    }

    @Override
    public void beforeRunTask(Task task) {
        task.setState(JobState.PROCESSING);
        taskRepository.save(task);
    }

    @Override
    public void onTaskSuccess(Task task) {
        task.setState(JobState.SUCCESSFUL);
        taskRepository.save(task);
    }

    @Override
    public void onTaskFailure(Task task) {
        task.setState(JobState.FAILED);
        taskRepository.save(task);
    }
}
