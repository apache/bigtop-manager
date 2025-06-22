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
import org.apache.bigtop.manager.dao.po.JobPO;
import org.apache.bigtop.manager.dao.po.StagePO;
import org.apache.bigtop.manager.dao.po.TaskPO;
import org.apache.bigtop.manager.dao.repository.JobDao;
import org.apache.bigtop.manager.dao.repository.StageDao;
import org.apache.bigtop.manager.dao.repository.TaskDao;
import org.apache.bigtop.manager.server.command.CommandIdentifier;
import org.apache.bigtop.manager.server.command.factory.JobFactories;
import org.apache.bigtop.manager.server.command.factory.JobFactory;
import org.apache.bigtop.manager.server.command.job.Job;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.command.scheduler.JobScheduler;
import org.apache.bigtop.manager.server.command.stage.Stage;
import org.apache.bigtop.manager.server.command.task.Task;
import org.apache.bigtop.manager.server.command.validator.ValidatorContext;
import org.apache.bigtop.manager.server.command.validator.ValidatorExecutionChain;
import org.apache.bigtop.manager.server.model.converter.JobConverter;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.vo.CommandVO;
import org.apache.bigtop.manager.server.service.CommandService;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;

@Slf4j
@Service
public class CommandServiceImpl implements CommandService {

    @Resource
    private JobScheduler jobScheduler;

    @Resource
    private JobDao jobDao;

    @Resource
    private StageDao stageDao;

    @Resource
    private TaskDao taskDao;

    @Override
    @Tool(name = "Command", description = "Add cluster or add services")
    public CommandVO command(
            @ToolParam(
                            required = true,
                            description =
                                    """
            Example of adding a cluster:
            {
                "command": "add", # Do not modify
                "commandLevel": "cluster", # Do not modify
                "clusterCommand": {
                    "name": "cluster_name",
                    "desc": "cluster_desc",
                    "type": 1,
                    "userGroup": "hadoop",
                    "rootDir": "root directory of cluster",
                    "hosts": {
                        "hostnames": [
                            "hostname"
                        ],
                        "agentDir": "path of bigtop-manager agent",
                        "sshUser": "username",
                        "sshPort": 22,
                        "authType": 3,
                        "grpcPort": 8835,
                        "desc": "vm"
                    }
                }
            }
            Example of adding services:
            {
                "command": "add", # Do not modify
                "clusterId": 1,
                "commandLevel": "service", # Do not modify
                "serviceCommands": [
                    {
                        "serviceName": "prometheus",
                        "componentHosts": [
                            {
                                "componentName": "prometheus_server",
                                "hostnames": [ # The host that has been added to the cluster
                                    "hostname"
                                ]
                            }
                        ],
                        "configs": [
                            {
                                "name": "prometheus",
                                "properties": [
                                    {
                                        "name": "port",
                                        "value": "9090",
                                    }
                                ]
                            },
                            {
                                "name": "prometheus-rule",
                                "properties": [
                                    {
                                        "name": "rules_file_name",
                                        "value": "prometheus_rules.yml",
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
            """)
                    CommandDTO commandDTO) {
        CommandIdentifier commandIdentifier =
                new CommandIdentifier(commandDTO.getCommandLevel(), commandDTO.getCommand());

        // Validate command params
        ValidatorContext validatorContext = new ValidatorContext();
        validatorContext.setCommandDTO(commandDTO);
        ValidatorExecutionChain.execute(validatorContext, commandIdentifier);

        // Create job
        JobContext jobContext = new JobContext();
        jobContext.setCommandDTO(commandDTO);
        jobContext.setRetryFlag(false);
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

        JobPO jobPO = job.getJobPO();
        jobPO.setClusterId(clusterId);
        jobPO.setState(JobState.PENDING.getName());
        jobDao.save(jobPO);
        job.loadJobPO(jobPO);

        for (int i = 0; i < job.getStages().size(); i++) {
            Stage stage = job.getStages().get(i);
            StagePO stagePO = stage.getStagePO();
            stagePO.setClusterId(clusterId);
            stagePO.setJobId(jobPO.getId());
            stagePO.setOrder(i + 1);
            stagePO.setState(JobState.PENDING.getName());
            stageDao.save(stagePO);
            stage.loadStagePO(stagePO);

            for (int j = 0; j < stage.getTasks().size(); j++) {
                Task task = stage.getTasks().get(j);
                TaskPO taskPO = task.getTaskPO();
                taskPO.setClusterId(clusterId);
                taskPO.setJobId(jobPO.getId());
                taskPO.setStageId(stagePO.getId());
                taskPO.setState(JobState.PENDING.getName());
                taskDao.save(taskPO);
                task.loadTaskPO(taskPO);
            }
        }

        return jobPO;
    }
}
