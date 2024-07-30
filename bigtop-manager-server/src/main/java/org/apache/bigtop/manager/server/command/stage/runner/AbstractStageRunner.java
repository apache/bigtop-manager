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
import org.apache.bigtop.manager.dao.po.StagePO;
import org.apache.bigtop.manager.dao.po.TaskPO;
import org.apache.bigtop.manager.dao.repository.StageRepository;
import org.apache.bigtop.manager.dao.repository.TaskRepository;
import org.apache.bigtop.manager.grpc.generated.CommandReply;
import org.apache.bigtop.manager.grpc.generated.CommandRequest;
import org.apache.bigtop.manager.grpc.generated.CommandServiceGrpc;
import org.apache.bigtop.manager.grpc.utils.ProtobufUtil;
import org.apache.bigtop.manager.server.command.stage.factory.StageContext;
import org.apache.bigtop.manager.server.grpc.GrpcClient;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public abstract class AbstractStageRunner implements StageRunner {

    @Resource
    protected StageRepository stageRepository;

    @Resource
    protected TaskRepository taskRepository;

    protected StagePO stagePO;

    protected StageContext stageContext;

    @Override
    public void setStage(StagePO stagePO) {
        this.stagePO = stagePO;
    }

    @Override
    public void setStageContext(StageContext stageContext) {
        this.stageContext = stageContext;
    }

    @Override
    public void run() {
        beforeRun();

        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (TaskPO taskPO : stagePO.getTaskPOList()) {
            beforeRunTask(taskPO);

            CommandRequest protoRequest = ProtobufUtil.fromJson(taskPO.getContent(), CommandRequest.class);
            CommandRequest.Builder builder = CommandRequest.newBuilder(protoRequest);
            builder.setTaskId(taskPO.getId());
            builder.setStageId(stagePO.getId());
            builder.setJobId(stagePO.getJob().getId());
            CommandRequest request = builder.build();

            futures.add(CompletableFuture.supplyAsync(() -> {
                CommandServiceGrpc.CommandServiceBlockingStub stub = GrpcClient.getBlockingStub(
                        taskPO.getHostname(), CommandServiceGrpc.CommandServiceBlockingStub.class);
                CommandReply reply = stub.exec(request);

                log.info("Execute task {} completed: {}", taskPO.getId(), reply);
                boolean taskSuccess = reply != null && reply.getCode() == MessageConstants.SUCCESS_CODE;

                if (taskSuccess) {
                    onTaskSuccess(taskPO);
                } else {
                    onTaskFailure(taskPO);
                }

                return taskSuccess;
            }));
        }

        List<Boolean> taskResults = futures.stream()
                .map((future) -> {
                    try {
                        return future.get();
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
        stagePO.setState(JobState.PROCESSING);
        stageRepository.save(stagePO);
    }

    @Override
    public void onSuccess() {
        stagePO.setState(JobState.SUCCESSFUL);
        stageRepository.save(stagePO);
    }

    @Override
    public void onFailure() {
        stagePO.setState(JobState.FAILED);
        stageRepository.save(stagePO);
    }

    @Override
    public void beforeRunTask(TaskPO taskPO) {
        taskPO.setState(JobState.PROCESSING);
        taskRepository.save(taskPO);
    }

    @Override
    public void onTaskSuccess(TaskPO taskPO) {
        taskPO.setState(JobState.SUCCESSFUL);
        taskRepository.save(taskPO);
    }

    @Override
    public void onTaskFailure(TaskPO taskPO) {
        taskPO.setState(JobState.FAILED);
        taskRepository.save(taskPO);
    }
}
