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
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.po.TaskPO;
import org.apache.bigtop.manager.dao.repository.HostDao;
import org.apache.bigtop.manager.dao.repository.TaskDao;
import org.apache.bigtop.manager.grpc.generated.CommandReply;
import org.apache.bigtop.manager.grpc.generated.CommandRequest;
import org.apache.bigtop.manager.grpc.generated.CommandServiceGrpc;
import org.apache.bigtop.manager.grpc.utils.ProtobufUtil;
import org.apache.bigtop.manager.server.grpc.GrpcClient;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.converter.HostConverter;
import org.apache.bigtop.manager.server.model.dto.HostDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTask implements Task {

    protected HostDao hostDao;

    protected TaskDao taskDao;

    protected TaskContext taskContext;

    /**
     * Do not use this directly, please use {@link #getTaskPO()} to make sure it's initialized.
     */
    private TaskPO taskPO;

    public AbstractTask(TaskContext taskContext) {
        this.taskContext = taskContext;

        injectBeans();
    }

    protected void injectBeans() {
        this.hostDao = SpringContextHolder.getBean(HostDao.class);
        this.taskDao = SpringContextHolder.getBean(TaskDao.class);
    }

    protected abstract Command getCommand();

    protected String getCustomCommand() {
        return null;
    }

    @Override
    public void beforeRun() {
        taskPO.setState(JobState.PROCESSING.getName());
        taskDao.partialUpdateById(taskPO);
    }

    @Override
    public Boolean run() {
        boolean taskSuccess;

        try {
            beforeRun();

            HostPO hostPO = hostDao.findByHostname(taskContext.getHostname());
            taskSuccess = doRun(hostPO.getHostname(), hostPO.getGrpcPort());
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

    // Send grpc request to agent with specific task implementation
    protected abstract Boolean doRun(String hostname, Integer grpcPort);

    @Override
    public void onSuccess() {
        TaskPO taskPO = getTaskPO();
        taskPO.setState(JobState.SUCCESSFUL.getName());
        taskDao.partialUpdateById(taskPO);
    }

    @Override
    public void onFailure() {
        TaskPO taskPO = getTaskPO();
        taskPO.setState(JobState.FAILED.getName());
        taskDao.partialUpdateById(taskPO);
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
