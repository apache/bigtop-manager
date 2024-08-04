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
import org.apache.bigtop.manager.dao.repository.ClusterRepository;
import org.apache.bigtop.manager.dao.repository.JobRepository;
import org.apache.bigtop.manager.dao.repository.StageRepository;
import org.apache.bigtop.manager.dao.repository.TaskRepository;
import org.apache.bigtop.manager.server.command.stage.Stage;
import org.apache.bigtop.manager.server.command.task.Task;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractJob implements Job {

    protected ClusterRepository clusterRepository;
    protected JobRepository jobRepository;
    protected StageRepository stageRepository;
    protected TaskRepository taskRepository;

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
    }

    protected void injectBeans() {
        this.clusterRepository = SpringContextHolder.getBean(ClusterRepository.class);
        this.jobRepository = SpringContextHolder.getBean(JobRepository.class);
        this.stageRepository = SpringContextHolder.getBean(StageRepository.class);
        this.taskRepository = SpringContextHolder.getBean(TaskRepository.class);
    }

    protected void beforeCreateStages() {
        Long clusterId = jobContext.getCommandDTO().getClusterId();
        this.clusterPO = clusterId == null ? new ClusterPO() : clusterRepository.getReferenceById(clusterId);
    }

    protected abstract void createStages();

    @Override
    public void beforeRun() {
        JobPO jobPO = getJobPO();
        jobPO.setState(JobState.PROCESSING);
        jobRepository.save(jobPO);
    }

    @Override
    public void run() {
        beforeRun();

        boolean success = true;
        LinkedBlockingQueue<Stage> queue = new LinkedBlockingQueue<>(stages);
        while (!queue.isEmpty()) {
            Stage stage = queue.poll();
            Boolean stageSuccess = stage.run();

            if (!stageSuccess) {
                success = false;
                break;
            }
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
        jobPO.setState(JobState.SUCCESSFUL);
        jobRepository.save(jobPO);
    }

    @Override
    public void onFailure() {
        JobPO jobPO = getJobPO();
        List<StagePO> stagePOList = new ArrayList<>();
        List<TaskPO> taskPOList = new ArrayList<>();

        jobPO.setState(JobState.FAILED);

        for (Stage stage : getStages()) {
            StagePO stagePO = stage.getStagePO();
            if (stagePO.getState() == JobState.PENDING) {
                stagePO.setState(JobState.CANCELED);
                stagePOList.add(stagePO);

                for (Task task : stage.getTasks()) {
                    TaskPO taskPO = task.getTaskPO();
                    taskPO.setState(JobState.CANCELED);
                    taskPOList.add(taskPO);
                }
            }
        }

        taskRepository.saveAll(taskPOList);
        stageRepository.saveAll(stagePOList);
        jobRepository.save(jobPO);
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
