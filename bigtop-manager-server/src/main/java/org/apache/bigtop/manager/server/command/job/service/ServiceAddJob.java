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

import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.po.ServiceConfigPO;
import org.apache.bigtop.manager.dao.po.ServiceConfigSnapshotPO;
import org.apache.bigtop.manager.dao.po.ServicePO;
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
import org.apache.bigtop.manager.server.utils.StackUtils;

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
        // Update cache files
        super.createCacheStage();

        // Install components
        super.createAddStages();

        // Configure services
        super.createConfigureStages();

        // Start all master components
        super.createStartStages();

        // Check all master components after started
        super.createCheckStages();
    }

    @Override
    protected List<String> getComponentNames() {
        List<String> componentNames = new ArrayList<>();
        for (ServiceCommandDTO serviceCommand : jobContext.getCommandDTO().getServiceCommands()) {
            List<ComponentHostDTO> componentHosts = serviceCommand.getComponentHosts();
            for (ComponentHostDTO componentHost : componentHosts) {
                String componentName = componentHost.getComponentName();
                componentNames.add(componentName);
            }
        }

        return componentNames;
    }

    @Override
    protected List<String> findHostnamesByComponentName(String componentName) {
        for (ServiceCommandDTO serviceCommand : jobContext.getCommandDTO().getServiceCommands()) {
            List<ComponentHostDTO> componentHosts = serviceCommand.getComponentHosts();
            for (ComponentHostDTO componentHost : componentHosts) {
                if (componentHost.getComponentName().equals(componentName)) {
                    return componentHost.getHostnames();
                }
            }
        }

        return new ArrayList<>();
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
        CommandDTO commandDTO = jobContext.getCommandDTO();
        Long clusterId = commandDTO.getClusterId();
        String serviceName = serviceCommand.getServiceName();

        // 1. Persist service
        StackDTO stackDTO = StackUtils.getServiceStack(serviceName);
        ServiceDTO serviceDTO = StackUtils.getServiceDTO(serviceName);
        ServicePO servicePO = ServiceConverter.INSTANCE.fromDTO2PO(serviceDTO);
        servicePO.setClusterId(clusterId);
        servicePO.setStack(StackUtils.getFullStackName(stackDTO));
        servicePO.setStatus(HealthyStatusEnum.UNHEALTHY.getCode());
        serviceDao.save(servicePO);

        // 2. Persist components
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

        // 3. Persist current configs
        Map<String, String> confMap = new HashMap<>();
        List<ServiceConfigDTO> configs = serviceCommand.getConfigs();
        List<ServiceConfigPO> serviceConfigPOList = ServiceConfigConverter.INSTANCE.fromDTO2PO(configs);
        for (ServiceConfigPO serviceConfigPO : serviceConfigPOList) {
            serviceConfigPO.setClusterId(clusterId);
            serviceConfigPO.setServiceId(servicePO.getId());
            confMap.put(serviceConfigPO.getName(), serviceConfigPO.getPropertiesJson());
        }

        serviceConfigDao.saveAll(serviceConfigPOList);

        // 4. Create initial config snapshot
        ServiceConfigSnapshotPO serviceConfigSnapshotPO = new ServiceConfigSnapshotPO();
        serviceConfigSnapshotPO.setName("initial");
        serviceConfigSnapshotPO.setDesc("Initial config snapshot");
        serviceConfigSnapshotPO.setConfigJson(JsonUtils.writeAsString(confMap));
        serviceConfigSnapshotPO.setServiceId(servicePO.getId());
        serviceConfigSnapshotDao.save(serviceConfigSnapshotPO);
    }
}
