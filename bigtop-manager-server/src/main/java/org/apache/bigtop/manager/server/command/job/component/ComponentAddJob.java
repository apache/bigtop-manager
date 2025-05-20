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
package org.apache.bigtop.manager.server.command.job.component;

import org.apache.bigtop.manager.common.constants.ComponentCategories;
import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.server.command.helper.ComponentStageHelper;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.enums.HealthyStatusEnum;
import org.apache.bigtop.manager.server.model.converter.ComponentConverter;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.dto.ComponentDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.command.ComponentCommandDTO;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentAddJob extends AbstractComponentJob {

    public ComponentAddJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();
    }

    @Override
    protected void createStages() {
        CommandDTO commandDTO = jobContext.getCommandDTO();
        Map<String, List<String>> componentHostsMap = getComponentHostsMap();

        // Install components
        stages.addAll(ComponentStageHelper.createComponentStages(componentHostsMap, Command.ADD, commandDTO));

        // Configure components
        stages.addAll(ComponentStageHelper.createComponentStages(componentHostsMap, Command.CONFIGURE, commandDTO));

        // Init/Start/Prepare components
        List<Command> commands = List.of(Command.INIT, Command.START, Command.PREPARE);
        stages.addAll(ComponentStageHelper.createComponentStages(componentHostsMap, commands, commandDTO));
    }

    @Override
    public void beforeRun() {
        super.beforeRun();

        // Skip persistent if it's a retry job
        if (jobContext.getRetryFlag()) {
            return;
        }

        CommandDTO commandDTO = jobContext.getCommandDTO();
        List<ComponentCommandDTO> componentCommands = commandDTO.getComponentCommands();
        for (ComponentCommandDTO componentCommand : componentCommands) {
            saveComponents(componentCommand);
        }
    }

    @Override
    public String getName() {
        return "Add components";
    }

    private void saveComponents(ComponentCommandDTO componentCommand) {
        Long clusterId = jobContext.getCommandDTO().getClusterId();
        String componentName = componentCommand.getComponentName();
        ServiceDTO serviceDTO = StackUtils.getServiceDTOByComponentName(componentName);
        ServicePO servicePO = serviceDao.findByClusterIdAndName(clusterId, serviceDTO.getName());

        List<ComponentPO> componentPOList = new ArrayList<>();
        List<String> hostnames = componentCommand.getHostnames();
        for (String hostname : hostnames) {
            if (checkComponentInstalled(componentName, hostname)) {
                continue;
            }

            HostPO hostPO = hostDao.findByHostname(hostname);
            ComponentDTO componentDTO = StackUtils.getComponentDTO(componentName);
            ComponentPO componentPO = ComponentConverter.INSTANCE.fromDTO2PO(componentDTO);
            componentPO.setClusterId(clusterId);
            componentPO.setHostId(hostPO.getId());
            componentPO.setServiceId(servicePO.getId());
            HealthyStatusEnum status = ComponentCategories.CLIENT.equals(componentDTO.getCategory())
                    ? HealthyStatusEnum.HEALTHY
                    : HealthyStatusEnum.UNKNOWN;
            componentPO.setStatus(status.getCode());
            componentPOList.add(componentPO);
        }

        if (CollectionUtils.isNotEmpty(componentPOList)) {
            componentDao.saveAll(componentPOList);

            // Require restart after adding new components
            servicePO.setRestartFlag(true);
            serviceDao.partialUpdateById(servicePO);
        }
    }

    protected Map<String, List<String>> getComponentHostsMap() {
        Map<String, List<String>> componentHostsMap = new HashMap<>();

        jobContext.getCommandDTO().getComponentCommands().forEach(componentCommand -> {
            String componentName = componentCommand.getComponentName();
            List<String> hostnames = componentCommand.getHostnames();
            for (String hostname : hostnames) {
                if (!checkComponentInstalled(componentName, hostname)) {
                    List<String> list = componentHostsMap.computeIfAbsent(componentName, k -> new ArrayList<>());
                    list.add(hostname);
                }
            }
        });

        return componentHostsMap;
    }

    private Boolean checkComponentInstalled(String componentName, String hostname) {
        ComponentPO componentPO = componentDao.findByNameAndHostname(componentName, hostname);
        return componentPO != null;
    }
}
