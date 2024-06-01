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
import org.apache.bigtop.manager.dao.entity.Job;
import org.apache.bigtop.manager.dao.entity.Stage;
import org.apache.bigtop.manager.dao.entity.Task;
import org.apache.bigtop.manager.dao.repository.JobRepository;
import org.apache.bigtop.manager.dao.repository.StageRepository;
import org.apache.bigtop.manager.dao.repository.TaskRepository;
import org.apache.bigtop.manager.server.command.job.factory.JobContext;
import org.apache.bigtop.manager.server.command.stage.runner.StageRunner;
import org.apache.bigtop.manager.server.command.stage.runner.StageRunners;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import jakarta.annotation.Resource;

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
        List<Stage> stages = job.getStages();
        stages.sort(Comparator.comparingInt(Stage::getOrder));

        boolean success = true;
        LinkedBlockingQueue<Stage> queue = new LinkedBlockingQueue<>(stages);
        while (!queue.isEmpty()) {
            Stage stage = queue.poll();
            StageRunner runner = StageRunners.getStageRunner(stage);
            runner.run();

            if (stage.getState() == JobState.FAILED) {
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

        for (Stage stage : job.getStages()) {
            if (stage.getState() == JobState.PENDING) {
                stage.setState(JobState.CANCELED);
                stageRepository.save(stage);

                for (Task task : stage.getTasks()) {
                    task.setState(JobState.CANCELED);
                    taskRepository.save(task);
                }
            }
        }
    }

    protected CommandDTO getCommandDTO() {
        return jobContext.getCommandDTO();
    }
}
