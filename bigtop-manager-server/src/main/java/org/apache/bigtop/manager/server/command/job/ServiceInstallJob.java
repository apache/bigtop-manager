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
import org.apache.bigtop.manager.dao.mapper.HostMapper;
import org.apache.bigtop.manager.dao.mapper.ServiceMapper;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.po.HostComponentPO;
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.converter.ComponentConverter;
import org.apache.bigtop.manager.server.model.converter.ServiceConverter;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.dto.ComponentDTO;
import org.apache.bigtop.manager.server.model.dto.ComponentHostDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.command.ServiceCommandDTO;
import org.apache.bigtop.manager.server.service.ConfigService;
import org.apache.bigtop.manager.server.utils.StackUtils;

import java.util.ArrayList;
import java.util.List;

public class ServiceInstallJob extends AbstractServiceJob {

    private ConfigService configService;
    private ServiceMapper serviceMapper;
    private HostMapper hostMapper;

    public ServiceInstallJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();

        this.configService = SpringContextHolder.getBean(ConfigService.class);
        this.serviceMapper = SpringContextHolder.getBean(ServiceMapper.class);
        this.hostMapper = SpringContextHolder.getBean(HostMapper.class);
    }

    @Override
    protected void createStages() {
        // Install components
        super.createInstallStages();

        // Update cache files after installed
        super.createCacheStage();

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
                    List<String> hostnames = new ArrayList<>(componentHost.getHostnames());
                    if (serviceCommand.getInstalled()) {
                        List<String> existHostnames = hostComponentMapper
                                .findAllByClusterIdAndComponentNameAndHostnameIn(
                                        clusterPO.getId(), componentName, hostnames)
                                .stream()
                                .map(HostComponentPO::getHostname)
                                .toList();

                        hostnames.removeAll(existHostnames);
                    }

                    return hostnames;
                }
            }
        }

        return new ArrayList<>();
    }

    @Override
    public void beforeRun() {
        super.beforeRun();

        CommandDTO commandDTO = jobContext.getCommandDTO();
        List<ServiceCommandDTO> serviceCommands = commandDTO.getServiceCommands();
        Long clusterId = commandDTO.getClusterId();

        // Persist service, component and hostComponent metadata to database
        for (ServiceCommandDTO serviceCommand : serviceCommands) {
            String serviceName = serviceCommand.getServiceName();
            ServicePO servicePO = serviceMapper.findByClusterIdAndServiceName(clusterId, serviceName);
            upsertService(servicePO, serviceCommand);
        }
    }

    private void upsertService(ServicePO servicePO, ServiceCommandDTO serviceCommand) {
        CommandDTO commandDTO = jobContext.getCommandDTO();
        Long clusterId = commandDTO.getClusterId();
        String serviceName = serviceCommand.getServiceName();
        ClusterPO clusterPO = clusterMapper.findByIdJoin(clusterId);

        String stackName = clusterPO.getStackName();
        String stackVersion = clusterPO.getStackVersion();

        // 1. Persist service and components
        if (servicePO == null) {
            ServiceDTO serviceDTO = StackUtils.getServiceDTO(stackName, stackVersion, serviceName);
            servicePO = ServiceConverter.INSTANCE.fromDTO2PO(serviceDTO, clusterPO);
            serviceMapper.save(servicePO);
        }

        // 2. Update configs
        configService.upsert(clusterId, servicePO.getId(), serviceCommand.getConfigs());

        for (ComponentHostDTO componentHostDTO : serviceCommand.getComponentHosts()) {
            String componentName = componentHostDTO.getComponentName();

            // 3. Persist component
            ComponentPO componentPO = componentMapper.findByClusterIdAndComponentName(clusterId, componentName);
            if (componentPO == null) {
                ComponentDTO componentDTO = StackUtils.getComponentDTO(stackName, stackVersion, componentName);
                componentPO = ComponentConverter.INSTANCE.fromDTO2PO(componentDTO, servicePO, clusterPO);
                componentMapper.save(componentPO);
            }

            // 4. Persist hostComponent
            for (String hostname : componentHostDTO.getHostnames()) {
                HostComponentPO hostComponentPO = hostComponentMapper.findByClusterIdAndComponentNameAndHostname(
                        clusterId, componentName, hostname);
                if (hostComponentPO == null) {
                    HostPO hostPO = hostMapper.findByHostname(hostname);

                    hostComponentPO = new HostComponentPO();
                    hostComponentPO.setHostId(hostPO.getId());
                    hostComponentPO.setComponentId(componentPO.getId());
                    hostComponentPO.setState(MaintainState.UNINSTALLED.getName());
                    hostComponentMapper.save(hostComponentPO);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Install services";
    }
}
