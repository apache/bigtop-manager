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

public abstract class AbstractTask implements Task {

    protected TaskDao taskDao;

    protected TaskContext taskContext;

    protected CommandRequest commandRequest;

    /**
     * Do not use this directly, please use {@link #getTaskPO()} to make sure it's initialized.
     */
    private TaskPO taskPO;

    public AbstractTask(TaskContext taskContext) {
        this.taskContext = taskContext;

        injectBeans();
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
    public void beforeRun() {
        taskPO.setState(JobState.PROCESSING.getName());
        taskDao.updateById(taskPO);
    }

    @Override
    public Boolean run() {
        boolean taskSuccess;

        try {
            beforeRun();

            CommandRequest.Builder builder = CommandRequest.newBuilder(getCommandRequest());
            builder.setTaskId(getTaskPO().getId());
            commandRequest = builder.build();

            CommandServiceGrpc.CommandServiceBlockingStub stub = GrpcClient.getBlockingStub(
                    taskContext.getHostname(), CommandServiceGrpc.CommandServiceBlockingStub.class);
            CommandReply reply = stub.exec(commandRequest);

            taskSuccess = reply != null && reply.getCode() == MessageConstants.SUCCESS_CODE;
        } catch (Exception e) {
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
    public void onSuccess() {
        TaskPO taskPO = getTaskPO();
        taskPO.setContent(ProtobufUtil.toJson(commandRequest));
        taskPO.setState(JobState.SUCCESSFUL.getName());
        taskDao.updateById(taskPO);
    }

    @Override
    public void onFailure() {
        TaskPO taskPO = getTaskPO();
        taskPO.setContent(ProtobufUtil.toJson(commandRequest));
        taskPO.setState(JobState.FAILED.getName());
        taskDao.updateById(taskPO);
    }

    @Override
    public TaskContext getTaskContext() {
        return taskContext;
    }

    @Override
    public void loadTaskPO(TaskPO taskPO) {
        this.taskPO = taskPO;
    }

    @Override
    public TaskPO getTaskPO() {
        if (taskPO == null) {
            taskPO = new TaskPO();
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
        }

        return taskPO;
    }
}
