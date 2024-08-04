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
package org.apache.bigtop.manager.server.service.impl;

import org.apache.bigtop.manager.common.enums.JobState;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.JobPO;
import org.apache.bigtop.manager.dao.po.StagePO;
import org.apache.bigtop.manager.dao.po.TaskPO;
import org.apache.bigtop.manager.dao.repository.ClusterRepository;
import org.apache.bigtop.manager.dao.repository.JobRepository;
import org.apache.bigtop.manager.dao.repository.StageRepository;
import org.apache.bigtop.manager.dao.repository.TaskRepository;
import org.apache.bigtop.manager.server.command.CommandIdentifier;
import org.apache.bigtop.manager.server.command.job.Job;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.command.factory.JobFactories;
import org.apache.bigtop.manager.server.command.factory.JobFactory;
import org.apache.bigtop.manager.server.command.validator.ValidatorContext;
import org.apache.bigtop.manager.server.command.validator.ValidatorExecutionChain;
import org.apache.bigtop.manager.server.command.scheduler.JobScheduler;
import org.apache.bigtop.manager.server.command.stage.Stage;
import org.apache.bigtop.manager.server.command.task.Task;
import org.apache.bigtop.manager.server.model.converter.JobConverter;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.vo.CommandVO;
import org.apache.bigtop.manager.server.service.CommandService;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;

@Slf4j
@Service
public class CommandServiceImpl implements CommandService {

    @Resource
    private JobScheduler jobScheduler;

    @Resource
    private ClusterRepository clusterRepository;

    @Resource
    private JobRepository jobRepository;

    @Resource
    private StageRepository stageRepository;

    @Resource
    private TaskRepository taskRepository;

    @Override
    public CommandVO command(CommandDTO commandDTO) {
        CommandIdentifier commandIdentifier =
                new CommandIdentifier(commandDTO.getCommandLevel(), commandDTO.getCommand());

        // Validate command params
        ValidatorContext validatorContext = new ValidatorContext();
        validatorContext.setCommandDTO(commandDTO);
        ValidatorExecutionChain.execute(validatorContext, commandIdentifier);

        // Create job
        JobContext jobContext = new JobContext();
        jobContext.setCommandDTO(commandDTO);
        JobFactory jobFactory = JobFactories.getJobFactory(commandIdentifier);
        Job job = jobFactory.createJob(jobContext);

        // Save job
        JobPO jobPO = saveJob(job);

        // Submit job
        jobScheduler.submit(job);

        return JobConverter.INSTANCE.fromPO2CommandVO(jobPO);
    }

    protected JobPO saveJob(Job job) {
        Long clusterId = job.getJobContext().getCommandDTO().getClusterId();
        ClusterPO clusterPO = clusterId == null ? null : clusterRepository.getReferenceById(clusterId);

        JobPO jobPO = job.getJobPO();
        jobPO.setClusterPO(clusterPO);
        jobPO.setState(JobState.PENDING);
        jobRepository.save(jobPO);

        for (int i = 0; i < job.getStages().size(); i++) {
            Stage stage = job.getStages().get(i);
            StagePO stagePO = stage.getStagePO();
            stagePO.setClusterPO(clusterPO);
            stagePO.setJobPO(jobPO);
            stagePO.setOrder(i + 1);
            stagePO.setState(JobState.PENDING);
            stageRepository.save(stagePO);

            for (int j = 0; j < stage.getTasks().size(); j++) {
                Task task = stage.getTasks().get(j);
                TaskPO taskPO = task.getTaskPO();
                taskPO.setClusterPO(clusterPO);
                taskPO.setJobPO(jobPO);
                taskPO.setStagePO(stagePO);
                taskPO.setState(JobState.PENDING);
                taskRepository.save(taskPO);
            }
        }

        return jobPO;
    }
}
