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
package org.apache.bigtop.manager.server.command.job.runner.service;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.enums.MaintainState;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.Component;
import org.apache.bigtop.manager.dao.po.Host;
import org.apache.bigtop.manager.dao.po.HostComponent;
import org.apache.bigtop.manager.dao.po.Service;
import org.apache.bigtop.manager.dao.repository.ClusterRepository;
import org.apache.bigtop.manager.dao.repository.ComponentRepository;
import org.apache.bigtop.manager.dao.repository.HostComponentRepository;
import org.apache.bigtop.manager.dao.repository.HostRepository;
import org.apache.bigtop.manager.dao.repository.ServiceRepository;
import org.apache.bigtop.manager.server.command.CommandIdentifier;
import org.apache.bigtop.manager.server.command.job.runner.AbstractJobRunner;
import org.apache.bigtop.manager.server.enums.CommandLevel;
import org.apache.bigtop.manager.server.model.converter.ComponentConverter;
import org.apache.bigtop.manager.server.model.converter.ServiceConverter;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.dto.ComponentDTO;
import org.apache.bigtop.manager.server.model.dto.ComponentHostDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.command.ServiceCommandDTO;
import org.apache.bigtop.manager.server.service.ConfigService;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.List;

@Slf4j
@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ServiceInstallJobRunner extends AbstractJobRunner {

    @Resource
    private ConfigService configService;

    @Resource
    private ClusterRepository clusterRepository;

    @Resource
    private ServiceRepository serviceRepository;

    @Resource
    private ComponentRepository componentRepository;

    @Resource
    private HostRepository hostRepository;

    @Resource
    private HostComponentRepository hostComponentRepository;

    @Override
    public CommandIdentifier getCommandIdentifier() {
        return new CommandIdentifier(CommandLevel.SERVICE, Command.INSTALL);
    }

    @Override
    public void beforeRun() {
        super.beforeRun();

        CommandDTO commandDTO = getCommandDTO();
        List<ServiceCommandDTO> serviceCommands = commandDTO.getServiceCommands();
        Long clusterId = commandDTO.getClusterId();

        // Persist service, component and hostComponent metadata to database
        for (ServiceCommandDTO serviceCommand : serviceCommands) {
            String serviceName = serviceCommand.getServiceName();
            Service service = serviceRepository.findByClusterIdAndServiceName(clusterId, serviceName);
            upsertService(service, serviceCommand);
        }
    }

    private void upsertService(Service service, ServiceCommandDTO serviceCommand) {
        CommandDTO commandDTO = getCommandDTO();
        Long clusterId = commandDTO.getClusterId();
        String serviceName = serviceCommand.getServiceName();
        ClusterPO clusterPO = clusterRepository.getReferenceById(clusterId);

        String stackName = clusterPO.getStack().getStackName();
        String stackVersion = clusterPO.getStack().getStackVersion();

        // 1. Persist service
        if (service == null) {
            ServiceDTO serviceDTO = StackUtils.getServiceDTO(stackName, stackVersion, serviceName);
            service = ServiceConverter.INSTANCE.fromDTO2Entity(serviceDTO, clusterPO);
            service = serviceRepository.save(service);
        }

        // 2. Update configs
        configService.upsert(clusterId, service.getId(), serviceCommand.getConfigs());

        for (ComponentHostDTO componentHostDTO : serviceCommand.getComponentHosts()) {
            String componentName = componentHostDTO.getComponentName();

            // 3. Persist component
            Component component = componentRepository.findByClusterIdAndComponentName(clusterId, componentName);
            if (component == null) {
                ComponentDTO componentDTO = StackUtils.getComponentDTO(stackName, stackVersion, componentName);
                component = ComponentConverter.INSTANCE.fromDTO2Entity(componentDTO, service, clusterPO);
                component = componentRepository.save(component);
            }

            // 4. Persist hostComponent
            for (String hostname : componentHostDTO.getHostnames()) {
                HostComponent hostComponent =
                        hostComponentRepository.findByComponentComponentNameAndHostHostname(componentName, hostname);
                if (hostComponent == null) {
                    Host host = hostRepository.findByHostname(hostname);

                    hostComponent = new HostComponent();
                    hostComponent.setHost(host);
                    hostComponent.setComponent(component);
                    hostComponent.setState(MaintainState.UNINSTALLED);
                    hostComponentRepository.save(hostComponent);
                }
            }
        }
    }
}
