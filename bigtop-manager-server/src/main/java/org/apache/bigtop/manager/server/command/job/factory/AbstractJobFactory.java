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
package org.apache.bigtop.manager.server.command.job.factory;

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

import jakarta.annotation.Resource;
import org.apache.bigtop.manager.server.command.job.Job;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractJobFactory implements JobFactory {

    @Resource
    private ClusterRepository clusterRepository;

    @Resource
    private JobRepository jobRepository;

    @Resource
    private StageRepository stageRepository;

    @Resource
    private TaskRepository taskRepository;

    protected JobContext jobContext;

    protected ClusterPO clusterPO;

    protected JobPO jobPO;

    protected List<StagePO> stagePOList = new ArrayList<>();

    @Override
    public JobPO createJobOld(JobContext jobContext) {
        this.jobContext = jobContext;

        // Create and init job
        initJob();

        // Create stages and tasks for job
        createStagesAndTasks();

        // Save job
        saveJob();

        return this.jobPO;
    }

    protected abstract void createStagesAndTasks();

    private void initJob() {
        Long clusterId = jobContext.getCommandDTO().getClusterId();
        this.clusterPO = clusterId == null ? new ClusterPO() : clusterRepository.getReferenceById(clusterId);

        this.jobPO = new JobPO();
        jobPO.setName(jobContext.getCommandDTO().getContext());
        jobPO.setState(JobState.PENDING);
        jobPO.setClusterPO(clusterPO.getId() == null ? null : clusterPO);
        jobPO.setContext(JsonUtils.writeAsString(jobContext));
        jobPO.setStagePOList(stagePOList);
    }

    protected void saveJob() {
        jobRepository.save(jobPO);

        for (int i = 0; i < jobPO.getStagePOList().size(); i++) {
            StagePO stagePO = jobPO.getStagePOList().get(i);
            stagePO.setClusterPO(clusterPO.getId() == null ? null : clusterPO);
            stagePO.setJobPO(jobPO);
            stagePO.setOrder(i + 1);
            stagePO.setState(JobState.PENDING);
            stageRepository.save(stagePO);

            for (TaskPO taskPO : stagePO.getTaskPOList()) {
                taskPO.setClusterPO(clusterPO.getId() == null ? null : clusterPO);
                taskPO.setJobPO(jobPO);
                taskPO.setStagePO(stagePO);
                taskPO.setState(JobState.PENDING);
                taskRepository.save(taskPO);
            }
        }
    }

    @Override
    public Job createJob(JobContext jobContext) {
        return null;
    }
}
