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
import org.apache.bigtop.manager.server.command.stage.CacheFileUpdateStage;
import org.apache.bigtop.manager.server.command.stage.HostCheckStage;
import org.apache.bigtop.manager.server.command.stage.Stage;
import org.apache.bigtop.manager.server.command.stage.StageContext;
import org.apache.bigtop.manager.server.command.task.Task;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.converter.ClusterConverter;
import org.apache.bigtop.manager.server.model.dto.ClusterDTO;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.vo.ClusterVO;
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
        stages.add(new HostCheckStage(StageContext.fromJobContext(jobContext)));
        stages.add(new CacheFileUpdateStage(StageContext.fromJobContext(jobContext)));
    }

    @Override
    public void beforeRun() {
        super.beforeRun();

        // Save cluster
        CommandDTO commandDTO = jobContext.getCommandDTO();
        ClusterDTO clusterDTO = ClusterConverter.INSTANCE.fromCommand2DTO(commandDTO.getClusterCommand());
        ClusterVO clusterVO = clusterService.save(clusterDTO);
        getJobContext().getCommandDTO().setClusterId(clusterVO.getId());

        for (Stage stage : stages) {
            stage.getStageContext().setClusterId(clusterVO.getId());
            for (Task task : stage.getTasks()) {
                task.getTaskContext().setClusterId(clusterVO.getId());
            }
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        ClusterPO clusterPO = clusterDao.findById(getJobContext().getCommandDTO().getClusterId());

        // Update cluster state to installed
        clusterPO.setState(MaintainState.INSTALLED.getName());
        clusterDao.partialUpdateById(clusterPO);
    }

    @Override
    public String getName() {
        return "Create cluster";
    }
}
