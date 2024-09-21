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
import org.apache.bigtop.manager.dao.repository.ClusterDao;
import org.apache.bigtop.manager.dao.repository.JobDao;
import org.apache.bigtop.manager.dao.repository.StackDao;
import org.apache.bigtop.manager.dao.repository.StageDao;
import org.apache.bigtop.manager.dao.repository.TaskDao;
import org.apache.bigtop.manager.server.command.stage.Stage;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
public abstract class AbstractJob implements Job {

    @Getter
    @Setter
    private JobState state;

    protected StackDao stackDao;
    protected ClusterDao clusterDao;
    protected JobDao jobDao;
    protected StageDao stageDao;
    protected TaskDao taskDao;

    @Getter
    protected JobContext jobContext;

    protected List<Stage> stages;

    protected ClusterPO clusterPO;

    public AbstractJob(JobContext jobContext) {
        injectBeans();
        this.jobContext = jobContext;
        this.stages = new ArrayList<>();

        state = JobState.PENDING;
        if (jobContext.getJobId() != null) {
            persistState();
            List<StagePO> stagePOs = stageDao.findByJobId(jobContext.getJobId());
            jobContext.setStagePOS(stagePOs);

        } else {
            JobPO jobPO = new JobPO();
            jobPO.setState(getState().getName());
            jobPO.setClusterId(jobContext.getCommandDTO().getClusterId());

            jobPO.setName(getName());
            jobPO.setContext(JsonUtils.writeAsString(jobContext));
            jobDao.save(jobPO);

            jobContext.setJobId(jobPO.getId());
        }

        beforeCreateStages();

        createStages();
    }

    protected void injectBeans() {
        this.stackDao = SpringContextHolder.getBean(StackDao.class);
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
    public Boolean run() {
        boolean success = true;

        try {
            beforeRun();

            // Execute in order
            stages.sort(Comparator.comparingInt(x -> x.getStageContext().getOrder()));

            for (Stage stage : stages) {
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

        return success;
    }

    @Override
    public void persistState() {
        JobPO jobPO =
                jobDao.findOptionalById(jobContext.getJobId()).orElseThrow(() -> new RuntimeException("Job not found"));
        jobPO.setState(getState().getName());
        jobPO.setClusterId(jobContext.getCommandDTO().getClusterId());

        if (jobContext.getCommandDTO().getClusterId() != null) {
            jobPO.setClusterId(jobContext.getCommandDTO().getClusterId());
        }

        jobDao.partialUpdateById(jobPO);
    }

    private void cancelRemainingStages() {
        for (Stage stage : stages) {
            if (stage.getState() == JobState.PENDING) {
                stage.setState(JobState.CANCELED);
                stage.persistState();
                stage.cancelRemainingTasks();
            }
        }
    }

    @Override
    public void onFailure() {
        Job.super.onFailure();
        cancelRemainingStages();
    }
}
