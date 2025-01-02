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

import org.apache.bigtop.manager.dao.po.JobPO;
import org.apache.bigtop.manager.dao.po.StagePO;
import org.apache.bigtop.manager.dao.po.TaskPO;
import org.apache.bigtop.manager.server.command.stage.CacheFileUpdateStage;
import org.apache.bigtop.manager.server.command.stage.HostCheckStage;
import org.apache.bigtop.manager.server.command.stage.SetupJdkStage;
import org.apache.bigtop.manager.server.command.stage.Stage;
import org.apache.bigtop.manager.server.command.stage.StageContext;
import org.apache.bigtop.manager.server.command.task.Task;
import org.apache.bigtop.manager.server.enums.HealthyStatusEnum;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.converter.ClusterConverter;
import org.apache.bigtop.manager.server.model.dto.ClusterDTO;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.dto.HostDTO;
import org.apache.bigtop.manager.server.service.HostService;

import java.util.List;

public class ClusterAddJob extends AbstractJob {

    private HostService hostService;

    public ClusterAddJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();

        hostService = SpringContextHolder.getBean(HostService.class);
    }

    @Override
    protected void createStages() {
        StageContext stageContext = StageContext.fromCommandDTO(jobContext.getCommandDTO());
        stages.add(new HostCheckStage(stageContext));
        stages.add(new CacheFileUpdateStage(stageContext));
        stages.add(new SetupJdkStage(stageContext));
    }

    @Override
    public void beforeRun() {
        super.beforeRun();

        if (jobContext.getRetryFlag()) {
            return;
        }

        saveCluster();

        saveHosts();

        linkJobToCluster();
    }

    @Override
    public String getName() {
        return "Add cluster";
    }

    private void saveCluster() {
        CommandDTO commandDTO = jobContext.getCommandDTO();
        ClusterDTO clusterDTO = ClusterConverter.INSTANCE.fromCommand2DTO(commandDTO.getClusterCommand());
        clusterPO = clusterDao.findByName(clusterDTO.getName());
        if (clusterPO == null) {
            clusterPO = ClusterConverter.INSTANCE.fromDTO2PO(clusterDTO);
        }

        clusterPO.setStatus(HealthyStatusEnum.HEALTHY.getCode());
        clusterDao.save(clusterPO);
    }

    private void saveHosts() {
        CommandDTO commandDTO = jobContext.getCommandDTO();
        List<HostDTO> hostDTOList = commandDTO.getClusterCommand().getHosts();
        for (HostDTO hostDTO : hostDTOList) {
            hostDTO.setClusterId(clusterPO.getId());
            hostService.add(hostDTO);
        }
    }

    private void linkJobToCluster() {
        JobPO jobPO = getJobPO();
        jobPO.setClusterId(clusterPO.getId());
        jobDao.partialUpdateById(jobPO);

        for (Stage stage : getStages()) {
            StagePO stagePO = stage.getStagePO();
            stagePO.setClusterId(clusterPO.getId());
            stageDao.partialUpdateById(stagePO);

            for (Task task : stage.getTasks()) {
                TaskPO taskPO = task.getTaskPO();
                taskPO.setClusterId(clusterPO.getId());
                taskDao.partialUpdateById(taskPO);
            }
        }
    }
}
