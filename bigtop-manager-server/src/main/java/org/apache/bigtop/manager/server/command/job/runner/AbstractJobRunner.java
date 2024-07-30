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
package org.apache.bigtop.manager.server.command.job.runner;

import org.apache.bigtop.manager.common.enums.JobState;
import org.apache.bigtop.manager.dao.po.Job;
import org.apache.bigtop.manager.dao.po.StagePO;
import org.apache.bigtop.manager.dao.po.TaskPO;
import org.apache.bigtop.manager.dao.repository.JobRepository;
import org.apache.bigtop.manager.dao.repository.StageRepository;
import org.apache.bigtop.manager.dao.repository.TaskRepository;
import org.apache.bigtop.manager.server.command.job.factory.JobContext;
import org.apache.bigtop.manager.server.command.stage.runner.StageRunner;
import org.apache.bigtop.manager.server.command.stage.runner.StageRunners;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;

import jakarta.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractJobRunner implements JobRunner {

    @Resource
    private JobRepository jobRepository;

    @Resource
    private StageRepository stageRepository;

    @Resource
    private TaskRepository taskRepository;

    protected Job job;

    protected JobContext jobContext;

    @Override
    public void setJob(Job job) {
        this.job = job;
    }

    @Override
    public void setJobContext(JobContext jobContext) {
        this.jobContext = jobContext;
    }

    @Override
    public void run() {
        beforeRun();

        // Sort stage
        List<StagePO> stagePOList = job.getStagePOList();
        stagePOList.sort(Comparator.comparingInt(StagePO::getOrder));

        boolean success = true;
        LinkedBlockingQueue<StagePO> queue = new LinkedBlockingQueue<>(stagePOList);
        while (!queue.isEmpty()) {
            StagePO stagePO = queue.poll();
            StageRunner runner = StageRunners.getStageRunner(stagePO);
            runner.run();

            if (stagePO.getState() == JobState.FAILED) {
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
    public void beforeRun() {
        job.setState(JobState.PROCESSING);
        jobRepository.save(job);
    }

    @Override
    public void onSuccess() {
        job.setState(JobState.SUCCESSFUL);
        jobRepository.save(job);
    }

    @Override
    public void onFailure() {
        job.setState(JobState.FAILED);
        jobRepository.save(job);

        for (StagePO stagePO : job.getStagePOList()) {
            if (stagePO.getState() == JobState.PENDING) {
                stagePO.setState(JobState.CANCELED);
                stageRepository.save(stagePO);

                for (TaskPO taskPO : stagePO.getTaskPOList()) {
                    taskPO.setState(JobState.CANCELED);
                    taskRepository.save(taskPO);
                }
            }
        }
    }

    protected CommandDTO getCommandDTO() {
        return jobContext.getCommandDTO();
    }
}
