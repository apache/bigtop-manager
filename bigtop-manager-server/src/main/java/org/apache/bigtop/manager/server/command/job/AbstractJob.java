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
import org.apache.bigtop.manager.dao.mapper.ClusterMapper;
import org.apache.bigtop.manager.dao.mapper.JobMapper;
import org.apache.bigtop.manager.dao.mapper.StackMapper;
import org.apache.bigtop.manager.dao.mapper.StageMapper;
import org.apache.bigtop.manager.dao.mapper.TaskMapper;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.JobPO;
import org.apache.bigtop.manager.dao.po.StagePO;
import org.apache.bigtop.manager.dao.po.TaskPO;
import org.apache.bigtop.manager.server.command.stage.Stage;
import org.apache.bigtop.manager.server.command.task.Task;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractJob implements Job {

    protected StackMapper stackMapper;
    protected ClusterMapper clusterMapper;
    protected JobMapper jobMapper;
    protected StageMapper stageMapper;
    protected TaskMapper taskMapper;

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
        this.stackMapper = SpringContextHolder.getBean(StackMapper.class);
        this.clusterMapper = SpringContextHolder.getBean(ClusterMapper.class);

        this.jobMapper = SpringContextHolder.getBean(JobMapper.class);
        this.stageMapper = SpringContextHolder.getBean(StageMapper.class);
        this.taskMapper = SpringContextHolder.getBean(TaskMapper.class);
    }

    protected void beforeCreateStages() {
        Long clusterId = jobContext.getCommandDTO().getClusterId();
        this.clusterPO = clusterId == null ? new ClusterPO() : clusterMapper.findById(clusterId);
    }

    protected abstract void createStages();

    @Override
    public void beforeRun() {
        jobPO.setState(JobState.PROCESSING.getName());
        jobMapper.updateById(jobPO);
    }

    @Override
    public void run() {
        boolean success = true;

        try {
            beforeRun();

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
        jobMapper.updateById(jobPO);
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

        taskMapper.updateStateByIds(taskPOList);
        stageMapper.updateStateByIds(stagePOList);
        jobMapper.updateById(jobPO);
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
