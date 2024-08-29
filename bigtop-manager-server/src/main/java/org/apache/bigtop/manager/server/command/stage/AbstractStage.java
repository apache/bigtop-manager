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
import org.apache.bigtop.manager.dao.repository.StageDao;
import org.apache.bigtop.manager.server.command.task.Task;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractStage implements Stage {

    protected StageDao stageDao;

    protected StageContext stageContext;
    protected List<Task> tasks;

    /**
     * Do not use this directly, please use {@link #getStagePO()} to make sure it's initialized.
     */
    private StagePO stagePO;

    public AbstractStage(StageContext stageContext) {
        this.stageContext = stageContext;
        this.tasks = new ArrayList<>();

        injectBeans();

        beforeCreateTasks();

        for (String hostname : stageContext.getHostnames()) {
            tasks.add(createTask(hostname));
        }
    }

    protected void injectBeans() {
        this.stageDao = SpringContextHolder.getBean(StageDao.class);
    }

    protected abstract void beforeCreateTasks();

    protected abstract Task createTask(String hostname);

    protected String getServiceName() {
        return "cluster";
    }

    protected String getComponentName() {
        return "agent";
    }

    @Override
    public void beforeRun() {
        stagePO.setState(JobState.PROCESSING.getName());
        stageDao.updateById(stagePO);
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
                            return false;
                        }
                    })
                    .toList();

            allTaskSuccess = taskResults.stream().allMatch(Boolean::booleanValue);
        } catch (Exception e) {
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
    public void onSuccess() {
        StagePO stagePO = getStagePO();
        stagePO.setState(JobState.SUCCESSFUL.getName());
        stageDao.updateById(stagePO);
    }

    @Override
    public void onFailure() {
        StagePO stagePO = getStagePO();
        stagePO.setState(JobState.FAILED.getName());
        stageDao.updateById(stagePO);
    }

    @Override
    public StageContext getStageContext() {
        return stageContext;
    }

    @Override
    public List<Task> getTasks() {
        return tasks;
    }

    @Override
    public void loadStagePO(StagePO stagePO) {
        this.stagePO = stagePO;
    }

    @Override
    public StagePO getStagePO() {
        if (stagePO == null) {
            stagePO = new StagePO();
            stagePO.setName(getName());
            stagePO.setServiceName(getServiceName());
            stagePO.setComponentName(getComponentName());
            stagePO.setContext(JsonUtils.writeAsString(stageContext));
        }

        return stagePO;
    }
}
