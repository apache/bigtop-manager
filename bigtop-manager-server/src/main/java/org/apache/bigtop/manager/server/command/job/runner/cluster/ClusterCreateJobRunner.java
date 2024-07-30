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
package org.apache.bigtop.manager.server.command.job.runner.cluster;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.enums.MaintainState;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.StagePO;
import org.apache.bigtop.manager.dao.po.TaskPO;
import org.apache.bigtop.manager.dao.repository.ClusterRepository;
import org.apache.bigtop.manager.dao.repository.JobRepository;
import org.apache.bigtop.manager.dao.repository.StageRepository;
import org.apache.bigtop.manager.dao.repository.TaskRepository;
import org.apache.bigtop.manager.server.command.CommandIdentifier;
import org.apache.bigtop.manager.server.command.job.runner.AbstractJobRunner;
import org.apache.bigtop.manager.server.enums.CommandLevel;
import org.apache.bigtop.manager.server.model.converter.ClusterConverter;
import org.apache.bigtop.manager.server.model.dto.ClusterDTO;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.service.ClusterService;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;

@Slf4j
@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ClusterCreateJobRunner extends AbstractJobRunner {

    @Resource
    private ClusterService clusterService;

    @Resource
    private ClusterRepository clusterRepository;

    @Resource
    private JobRepository jobRepository;

    @Resource
    private StageRepository stageRepository;

    @Resource
    private TaskRepository taskRepository;

    @Override
    public CommandIdentifier getCommandIdentifier() {
        return new CommandIdentifier(CommandLevel.CLUSTER, Command.CREATE);
    }

    @Override
    public void beforeRun() {
        super.beforeRun();

        // Save cluster
        CommandDTO commandDTO = getCommandDTO();
        ClusterDTO clusterDTO = ClusterConverter.INSTANCE.fromCommand2DTO(commandDTO.getClusterCommand());
        clusterService.save(clusterDTO);
    }

    @Override
    public void onSuccess() {
        super.onSuccess();

        CommandDTO commandDTO = getCommandDTO();
        ClusterPO clusterPO = clusterRepository
                .findByClusterName(commandDTO.getClusterCommand().getClusterName())
                .orElse(new ClusterPO());

        // Update cluster state to installed
        clusterPO.setState(MaintainState.INSTALLED);
        clusterRepository.save(clusterPO);

        // Link job to cluster after cluster successfully added
        job.setClusterPO(clusterPO);
        jobRepository.save(job);

        for (StagePO stagePO : job.getStagePOList()) {
            stagePO.setClusterPO(clusterPO);
            stageRepository.save(stagePO);

            for (TaskPO taskPO : stagePO.getTaskPOList()) {
                taskPO.setClusterPO(clusterPO);
                taskRepository.save(taskPO);
            }
        }
    }
}
