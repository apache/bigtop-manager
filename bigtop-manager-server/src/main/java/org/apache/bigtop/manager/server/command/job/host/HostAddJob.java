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
package org.apache.bigtop.manager.server.command.job.host;

import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.command.stage.HostCheckStage;
import org.apache.bigtop.manager.server.command.stage.SetupJdkStage;
import org.apache.bigtop.manager.server.command.stage.StageContext;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.converter.HostConverter;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.dto.HostDTO;
import org.apache.bigtop.manager.server.service.HostService;

import java.util.List;

public class HostAddJob extends AbstractHostJob {

    private HostService hostService;

    public HostAddJob(JobContext jobContext) {
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
        stages.add(new SetupJdkStage(stageContext));
    }

    @Override
    public void beforeRun() {
        super.beforeRun();

        if (jobContext.getRetryFlag()) {
            return;
        }

        saveHosts();
    }

    @Override
    public String getName() {
        return "Add hosts";
    }

    protected void saveHosts() {
        CommandDTO commandDTO = jobContext.getCommandDTO();
        List<HostDTO> hostDTOList = HostConverter.INSTANCE.fromCommand2DTO(commandDTO.getHostCommands());
        for (HostDTO hostDTO : hostDTOList) {
            hostDTO.setClusterId(clusterPO.getId());
            hostService.add(hostDTO);
        }
    }
}
