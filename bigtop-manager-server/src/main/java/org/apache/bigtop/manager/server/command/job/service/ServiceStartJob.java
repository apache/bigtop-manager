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
package org.apache.bigtop.manager.server.command.job.service;

import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.server.command.helper.ComponentStageHelper;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.enums.HealthyStatusEnum;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.dto.command.ServiceCommandDTO;

import java.util.List;
import java.util.Map;

public class ServiceStartJob extends AbstractServiceJob {

    public ServiceStartJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void createStages() {
        CommandDTO commandDTO = jobContext.getCommandDTO();
        Map<String, List<String>> componentHostsMap = getComponentHostsMap();

        stages.addAll(ComponentStageHelper.createComponentStages(componentHostsMap, commandDTO));
    }

    @Override
    public void onSuccess() {
        super.onSuccess();

        CommandDTO commandDTO = jobContext.getCommandDTO();
        List<ServiceCommandDTO> serviceCommands = commandDTO.getServiceCommands();
        for (ServiceCommandDTO serviceCommand : serviceCommands) {
            Long clusterId = commandDTO.getClusterId();
            String serviceName = serviceCommand.getServiceName();
            ServicePO servicePO = serviceDao.findByClusterIdAndName(clusterId, serviceName);
            servicePO.setRestartFlag(false);
            servicePO.setStatus(HealthyStatusEnum.HEALTHY.getCode());
            serviceDao.partialUpdateById(servicePO);
        }
    }

    @Override
    public String getName() {
        return "Start services";
    }
}
