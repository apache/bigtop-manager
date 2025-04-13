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
package org.apache.bigtop.manager.server.command.job;

import org.apache.bigtop.manager.common.enums.JobState;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.JobPO;
import org.apache.bigtop.manager.dao.po.StagePO;
import org.apache.bigtop.manager.dao.po.TaskPO;
import org.apache.bigtop.manager.dao.repository.ClusterDao;
import org.apache.bigtop.manager.dao.repository.JobDao;
import org.apache.bigtop.manager.dao.repository.StageDao;
import org.apache.bigtop.manager.dao.repository.TaskDao;
import org.apache.bigtop.manager.server.command.helper.JobCacheHelper;
import org.apache.bigtop.manager.server.command.stage.Stage;
import org.apache.bigtop.manager.server.command.stage.StageContext;
import org.apache.bigtop.manager.server.command.task.Task;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;

import org.apache.commons.collections4.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public abstract class AbstractJob implements Job {

    protected ClusterDao clusterDao;
    protected JobDao jobDao;
    protected StageDao stageDao;
    protected TaskDao taskDao;

    protected JobContext jobContext;
    protected List<Stage> stages;

    protected ClusterPO clusterPO;

    /**
     * Do not use this directly, please use {@link #getJobPO()} to make sure it's initialized.
     */
    private JobPO jobPO;

    public AbstractJob(JobContext jobContext) {
        this.jobContext = jobContext;
        this.stages = new ArrayList<>();

        injectBeans();

        beforeCreateStages();

        createStages();

        if (CollectionUtils.isEmpty(stages)) {
            throw new ApiException(ApiExceptionEnum.JOB_HAS_NO_STAGES);
        }
    }

    protected void injectBeans() {
        this.clusterDao = SpringContextHolder.getBean(ClusterDao.class);

        this.jobDao = SpringContextHolder.getBean(JobDao.class);
        this.stageDao = SpringContextHolder.getBean(StageDao.class);
        this.taskDao = SpringContextHolder.getBean(TaskDao.class);
    }

    protected void beforeCreateStages() {
        Long clusterId = jobContext.getCommandDTO().getClusterId();
        this.clusterPO = clusterId == null ? new ClusterPO() : clusterDao.findById(clusterId);
    }

    protected abstract void createStages();

    @Override
    public void beforeRun() {
        jobPO.setState(JobState.PROCESSING.getName());
        jobDao.partialUpdateById(jobPO);
    }

    @Override
    public void run() {
        boolean success = true;

        try {
            // Persist job state and required data.
            beforeRun();

            // Send job cache to agents
            List<String> hostnames = stages.stream()
                    .map(Stage::getStageContext)
                    .map(StageContext::getHostnames)
                    .flatMap(List::stream)
                    .distinct()
                    .toList();
            JobCacheHelper.sendJobCache(jobPO.getId(), hostnames);

            LinkedBlockingQueue<Stage> queue = new LinkedBlockingQueue<>(stages);
            while (!queue.isEmpty()) {
                Stage stage = queue.poll();
                Boolean stageSuccess = stage.run();

                if (!stageSuccess) {
                    success = false;
                    break;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            success = false;
        }

        if (success) {
            onSuccess();
        } else {
            onFailure();
        }
    }

    @Override
    public void onSuccess() {
        JobPO jobPO = getJobPO();
        jobPO.setState(JobState.SUCCESSFUL.getName());
        jobDao.partialUpdateById(jobPO);
    }

    @Override
    public void onFailure() {
        JobPO jobPO = getJobPO();
        List<StagePO> stagePOList = new ArrayList<>();
        List<TaskPO> taskPOList = new ArrayList<>();

        jobPO.setState(JobState.FAILED.getName());

        for (Stage stage : getStages()) {
            StagePO stagePO = stage.getStagePO();
            if (JobState.fromString(stagePO.getState()) == JobState.PENDING) {
                stagePO.setState(JobState.CANCELED.getName());
                stagePOList.add(stagePO);

                for (Task task : stage.getTasks()) {
                    TaskPO taskPO = task.getTaskPO();
                    taskPO.setState(JobState.CANCELED.getName());
                    taskPOList.add(taskPO);
                }
            }
        }

        taskDao.partialUpdateByIds(taskPOList);
        stageDao.partialUpdateByIds(stagePOList);
        jobDao.partialUpdateById(jobPO);
    }

    @Override
    public JobContext getJobContext() {
        return jobContext;
    }

    @Override
    public List<Stage> getStages() {
        return stages;
    }

    @Override
    public void loadJobPO(JobPO jobPO) {
        this.jobPO = jobPO;
    }

    @Override
    public JobPO getJobPO() {
        if (jobPO == null) {
            jobPO = new JobPO();
            jobPO.setName(getName());
            jobPO.setContext(JsonUtils.writeAsString(jobContext));
        }

        return jobPO;
    }
}
