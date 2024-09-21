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
import org.apache.bigtop.manager.common.enums.JobState;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.po.TaskPO;
import org.apache.bigtop.manager.dao.repository.TaskDao;
import org.apache.bigtop.manager.grpc.generated.CommandReply;
import org.apache.bigtop.manager.grpc.generated.CommandRequest;
import org.apache.bigtop.manager.grpc.generated.CommandServiceGrpc;
import org.apache.bigtop.manager.grpc.utils.ProtobufUtil;
import org.apache.bigtop.manager.server.grpc.GrpcClient;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTask implements Task {

    @Getter
    @Setter
    private JobState state;

    protected TaskDao taskDao;

    @Getter
    protected TaskContext taskContext;

    protected CommandRequest commandRequest;

    public AbstractTask(TaskContext taskContext) {
        injectBeans();
        this.taskContext = taskContext;

        state = JobState.PENDING;
        if (taskContext.getTaskId() != null) {
            persistState();
        } else {
            TaskPO taskPO = new TaskPO();
            taskPO.setState(getState().getName());
            taskPO.setJobId(taskContext.getJobId());
            taskPO.setStageId(taskContext.getStageId());
            taskPO.setClusterId(taskContext.getClusterId());

            taskPO.setName(getName());
            taskPO.setContext(JsonUtils.writeAsString(taskContext));
            taskPO.setStackName(taskContext.getStackName());
            taskPO.setStackVersion(taskContext.getStackVersion());
            taskPO.setHostname(taskContext.getHostname());
            taskPO.setServiceName(taskContext.getServiceName());
            taskPO.setServiceUser(taskContext.getServiceUser());
            taskPO.setComponentName(taskContext.getComponentName());
            taskPO.setCommand(getCommand().getName());
            taskPO.setCustomCommand(getCustomCommand());

            taskDao.save(taskPO);

            taskContext.setTaskId(taskPO.getId());
        }
    }

    protected void injectBeans() {
        this.taskDao = SpringContextHolder.getBean(TaskDao.class);
    }

    protected abstract Command getCommand();

    protected String getCustomCommand() {
        return null;
    }

    protected abstract CommandRequest getCommandRequest();

    @Override
    public Boolean run() {
        boolean taskSuccess;

        try {
            beforeRun();

            CommandRequest.Builder builder = CommandRequest.newBuilder(getCommandRequest());
            builder.setTaskId(taskContext.getTaskId());
            commandRequest = builder.build();

            CommandServiceGrpc.CommandServiceBlockingStub stub = GrpcClient.getBlockingStub(
                    taskContext.getHostname(), CommandServiceGrpc.CommandServiceBlockingStub.class);
            CommandReply reply = stub.exec(commandRequest);

            taskSuccess = reply != null && reply.getCode() == MessageConstants.SUCCESS_CODE;
        } catch (Exception e) {
            log.error("task failed", e);
            taskSuccess = false;
        }

        if (taskSuccess) {
            onSuccess();
        } else {
            onFailure();
        }

        return taskSuccess;
    }

    @Override
    public void persistState() {
        TaskPO taskPO = taskDao.findOptionalById(taskContext.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));
        taskPO.setState(getState().getName());
        if (commandRequest != null) {
            taskPO.setContent(ProtobufUtil.toJson(commandRequest));
        }
        if (taskContext.getClusterId() != null) {
            taskPO.setClusterId(taskContext.getClusterId());
        }
        taskDao.partialUpdateById(taskPO);
    }
}
