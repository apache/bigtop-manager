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

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.po.ServiceConfigPO;
import org.apache.bigtop.manager.dao.po.ServiceConfigSnapshotPO;
import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.server.command.helper.ComponentStageHelper;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.enums.HealthyStatusEnum;
import org.apache.bigtop.manager.server.model.converter.ComponentConverter;
import org.apache.bigtop.manager.server.model.converter.ServiceConfigConverter;
import org.apache.bigtop.manager.server.model.converter.ServiceConverter;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.dto.ComponentDTO;
import org.apache.bigtop.manager.server.model.dto.ComponentHostDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceConfigDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.StackDTO;
import org.apache.bigtop.manager.server.model.dto.command.ServiceCommandDTO;
import org.apache.bigtop.manager.server.utils.StackConfigUtils;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceAddJob extends AbstractServiceJob {

    public ServiceAddJob(JobContext jobContext) {
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
        // Since the order of these stages might be mixed up, we need to sort and add them together.
        // For example, the order usually is init -> start -> prepare, but Hive Metastore init requires MySQL Server to
        // be prepared.
        List<Command> commands = List.of(Command.INIT, Command.START, Command.PREPARE);
        stages.addAll(ComponentStageHelper.createComponentStages(componentHostsMap, commands, commandDTO));

        // Check all master components after started
        stages.addAll(ComponentStageHelper.createComponentStages(componentHostsMap, Command.CHECK, commandDTO));
    }

    @Override
    protected Map<String, List<String>> getComponentHostsMap() {
        Map<String, List<String>> componentHostsMap = new HashMap<>();

        jobContext.getCommandDTO().getServiceCommands().stream()
                .filter(command -> !command.getInstalled())
                .map(ServiceCommandDTO::getComponentHosts)
                .forEach(componentHosts -> {
                    for (ComponentHostDTO componentHost : componentHosts) {
                        String componentName = componentHost.getComponentName();
                        List<String> hostnames = componentHost.getHostnames();
                        if (CollectionUtils.isEmpty(hostnames)) {
                            //                            throw new RuntimeException("No hostnames found for component "
                            // + componentName);
                            continue;
                        }
                        componentHostsMap.put(componentName, hostnames);
                    }
                });

        return componentHostsMap;
    }

    @Override
    public void beforeRun() {
        super.beforeRun();

        // Skip persistent if it's a retry job
        if (jobContext.getRetryFlag()) {
            return;
        }

        CommandDTO commandDTO = jobContext.getCommandDTO();
        List<ServiceCommandDTO> serviceCommands = commandDTO.getServiceCommands();
        for (ServiceCommandDTO serviceCommand : serviceCommands) {
            saveService(serviceCommand);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();

        List<ServicePO> servicePOList = new ArrayList<>();
        CommandDTO commandDTO = jobContext.getCommandDTO();
        Long clusterId = commandDTO.getClusterId();
        List<ServiceCommandDTO> serviceCommands = commandDTO.getServiceCommands();
        for (ServiceCommandDTO serviceCommand : serviceCommands) {
            if (serviceCommand.getInstalled()) {
                continue;
            }

            String serviceName = serviceCommand.getServiceName();
            ServicePO servicePO = serviceDao.findByClusterIdAndName(clusterId, serviceName);
            servicePO.setStatus(HealthyStatusEnum.HEALTHY.getCode());
            servicePOList.add(servicePO);
        }

        serviceDao.partialUpdateByIds(servicePOList);
    }

    @Override
    public String getName() {
        return "Add services";
    }

    private void saveService(ServiceCommandDTO serviceCommand) {
        if (serviceCommand.getInstalled()) {
            return;
        }

        CommandDTO commandDTO = jobContext.getCommandDTO();
        Long clusterId = commandDTO.getClusterId();
        String serviceName = serviceCommand.getServiceName();

        // Persist services
        StackDTO stackDTO = StackUtils.getServiceStack(serviceName);
        ServiceDTO serviceDTO = StackUtils.getServiceDTO(serviceName);
        ServicePO servicePO = ServiceConverter.INSTANCE.fromDTO2PO(serviceDTO);
        servicePO.setClusterId(clusterId);
        servicePO.setStack(StackUtils.getFullStackName(stackDTO));
        servicePO.setStatus(HealthyStatusEnum.UNHEALTHY.getCode());
        serviceDao.save(servicePO);

        // Persist components
        List<ComponentPO> componentPOList = new ArrayList<>();
        for (ComponentHostDTO componentHostDTO : serviceCommand.getComponentHosts()) {
            String componentName = componentHostDTO.getComponentName();
            List<HostPO> hostPOList = hostDao.findAllByHostnames(componentHostDTO.getHostnames());

            for (HostPO hostPO : hostPOList) {
                ComponentDTO componentDTO = StackUtils.getComponentDTO(componentName);
                ComponentPO componentPO = ComponentConverter.INSTANCE.fromDTO2PO(componentDTO);
                componentPO.setClusterId(clusterId);
                componentPO.setHostId(hostPO.getId());
                componentPO.setServiceId(servicePO.getId());
                componentPO.setStatus(HealthyStatusEnum.UNKNOWN.getCode());
                componentPOList.add(componentPO);
            }
        }

        componentDao.saveAll(componentPOList);

        // Persist current configs
        Map<String, String> confMap = new HashMap<>();
        List<ServiceConfigDTO> oriConfigs = StackUtils.SERVICE_CONFIG_MAP.get(serviceName);
        List<ServiceConfigDTO> newConfigs = serviceCommand.getConfigs();
        List<ServiceConfigDTO> mergedConfigs = StackConfigUtils.mergeServiceConfigs(oriConfigs, newConfigs);
        List<ServiceConfigPO> serviceConfigPOList = ServiceConfigConverter.INSTANCE.fromDTO2PO(mergedConfigs);
        for (ServiceConfigPO serviceConfigPO : serviceConfigPOList) {
            serviceConfigPO.setClusterId(clusterId);
            serviceConfigPO.setServiceId(servicePO.getId());
            confMap.put(serviceConfigPO.getName(), serviceConfigPO.getPropertiesJson());
        }

        serviceConfigDao.saveAll(serviceConfigPOList);

        // Create initial config snapshot
        ServiceConfigSnapshotPO serviceConfigSnapshotPO = new ServiceConfigSnapshotPO();
        serviceConfigSnapshotPO.setName("initial");
        serviceConfigSnapshotPO.setDesc("Initial config snapshot");
        serviceConfigSnapshotPO.setConfigJson(JsonUtils.writeAsString(confMap));
        serviceConfigSnapshotPO.setServiceId(servicePO.getId());
        serviceConfigSnapshotDao.save(serviceConfigSnapshotPO);
    }
}
