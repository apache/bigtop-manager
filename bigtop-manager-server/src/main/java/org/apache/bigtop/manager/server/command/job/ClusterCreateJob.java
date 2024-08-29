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

import org.apache.bigtop.manager.common.enums.MaintainState;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.JobPO;
import org.apache.bigtop.manager.dao.po.StagePO;
import org.apache.bigtop.manager.dao.po.TaskPO;
import org.apache.bigtop.manager.server.command.stage.CacheFileUpdateStage;
import org.apache.bigtop.manager.server.command.stage.HostCheckStage;
import org.apache.bigtop.manager.server.command.stage.Stage;
import org.apache.bigtop.manager.server.command.stage.StageContext;
import org.apache.bigtop.manager.server.command.task.Task;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.converter.ClusterConverter;
import org.apache.bigtop.manager.server.model.dto.ClusterDTO;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.service.ClusterService;

public class ClusterCreateJob extends AbstractJob {

    private ClusterService clusterService;

    public ClusterCreateJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();

        this.clusterService = SpringContextHolder.getBean(ClusterService.class);
    }

    @Override
    protected void createStages() {
        StageContext stageContext = StageContext.fromCommandDTO(jobContext.getCommandDTO());
        stages.add(new HostCheckStage(stageContext));
        stages.add(new CacheFileUpdateStage(stageContext));
    }

    @Override
    public void beforeRun() {
        super.beforeRun();

        // Save cluster
        CommandDTO commandDTO = jobContext.getCommandDTO();
        ClusterDTO clusterDTO = ClusterConverter.INSTANCE.fromCommand2DTO(commandDTO.getClusterCommand());
        clusterService.save(clusterDTO);
    }

    @Override
    public void onSuccess() {
        super.onSuccess();

        CommandDTO commandDTO = jobContext.getCommandDTO();
        ClusterPO clusterPO = clusterDao
                .findByClusterName(commandDTO.getClusterCommand().getClusterName())
                .orElse(new ClusterPO());

        // Update cluster state to installed
        clusterPO.setState(MaintainState.INSTALLED.getName());
        clusterDao.updateById(clusterPO);

        // Link job to cluster after cluster successfully added
        JobPO jobPO = getJobPO();
        jobPO.setClusterId(clusterPO.getId());
        jobDao.updateById(jobPO);

        for (Stage stage : getStages()) {
            StagePO stagePO = stage.getStagePO();
            stagePO.setClusterId(clusterPO.getId());
            stageDao.updateById(stagePO);

            for (Task task : stage.getTasks()) {
                TaskPO taskPO = task.getTaskPO();
                taskPO.setClusterId(clusterPO.getId());
                taskDao.updateById(taskPO);
            }
        }
    }

    @Override
    public String getName() {
        return "Create cluster";
    }
}
