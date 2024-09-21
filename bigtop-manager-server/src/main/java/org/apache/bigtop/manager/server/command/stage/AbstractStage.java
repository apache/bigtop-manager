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
package org.apache.bigtop.manager.server.command.stage;

import org.apache.bigtop.manager.common.enums.JobState;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.po.StagePO;
import org.apache.bigtop.manager.dao.po.TaskPO;
import org.apache.bigtop.manager.dao.repository.StageDao;
import org.apache.bigtop.manager.dao.repository.TaskDao;
import org.apache.bigtop.manager.server.command.task.Task;
import org.apache.bigtop.manager.server.command.task.TaskContext;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public abstract class AbstractStage implements Stage {

    @Getter
    @Setter
    private JobState state;

    protected StageDao stageDao;
    protected TaskDao taskDao;

    @Getter
    protected StageContext stageContext;

    @Getter
    protected List<Task> tasks;

    public AbstractStage(StageContext stageContext) {
        injectBeans();
        this.stageContext = stageContext;
        this.tasks = new ArrayList<>();

        state = JobState.PENDING;

        // obtain all tasks
        if (getStageContext().isRetry()) {
            persistState();
        } else {
            StagePO stagePO = new StagePO();
            stagePO.setState(getState().getName());
            stagePO.setJobId(stageContext.getJobId());
            stagePO.setClusterId(stageContext.getClusterId());

            stagePO.setOrder(getStageContext().getOrder());
            stagePO.setName(getName());
            stagePO.setServiceName(getServiceName());
            stagePO.setComponentName(getComponentName());
            stagePO.setContext(JsonUtils.writeAsString(stageContext));
            stageDao.save(stagePO);

            stageContext.setStageId(stagePO.getId());
        }

        beforeCreateTasks();

        if (getStageContext().isRetry()) {
            List<TaskPO> taskPOS = taskDao.findByStageId(stageContext.getStageId());
            for (TaskPO taskPO : taskPOS) {
                TaskContext taskContext = TaskContext.fromStageContext(stageContext);
                taskContext.setTaskId(taskPO.getId());
                taskContext.setHostname(taskPO.getHostname());
                tasks.add(createTask(taskContext));
            }
        } else {
            for (String hostname : stageContext.getHostnames()) {
                TaskContext taskContext = TaskContext.fromStageContext(stageContext);
                taskContext.setHostname(hostname);
                tasks.add(createTask(taskContext));
            }
        }
    }

    protected void injectBeans() {
        this.stageDao = SpringContextHolder.getBean(StageDao.class);
        this.taskDao = SpringContextHolder.getBean(TaskDao.class);
    }

    protected abstract void beforeCreateTasks();

    protected abstract Task createTask(TaskContext taskContext);

    protected String getServiceName() {
        return "cluster";
    }

    protected String getComponentName() {
        return "agent";
    }

    @Override
    public Boolean run() {
        boolean allTaskSuccess;

        try {
            beforeRun();

            List<CompletableFuture<Boolean>> futures = new ArrayList<>();
            for (Task task : tasks) {
                futures.add(CompletableFuture.supplyAsync(task::run));
            }

            List<Boolean> taskResults = futures.stream()
                    .map((future) -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            log.error("stage failed,", e);
                            return false;
                        }
                    })
                    .toList();

            allTaskSuccess = taskResults.stream().allMatch(Boolean::booleanValue);
        } catch (Exception e) {
            log.error("stage failed", e);
            allTaskSuccess = false;
        }

        if (allTaskSuccess) {
            onSuccess();
        } else {
            onFailure();
        }

        return allTaskSuccess;
    }

    @Override
    public void persistState() {
        StagePO stagePO = stageDao.findOptionalById(stageContext.getStageId())
                .orElseThrow(() -> new RuntimeException("Stage not found"));
        stagePO.setState(getState().getName());

        if (stageContext.getClusterId() != null) {
            stagePO.setClusterId(stageContext.getClusterId());
        }

        stageDao.partialUpdateById(stagePO);
    }

    @Override
    public void cancelRemainingTasks() {
        for (Task task : tasks) {
            if (task.getState() == JobState.PENDING) {
                task.setState(JobState.CANCELED);
                task.persistState();
            }
        }
    }

    @Override
    public void onFailure() {
        Stage.super.onFailure();
        cancelRemainingTasks();
    }
}
