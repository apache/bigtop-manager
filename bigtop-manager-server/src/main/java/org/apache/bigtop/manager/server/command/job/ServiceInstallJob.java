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

import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.dao.query.ComponentQuery;
import org.apache.bigtop.manager.dao.repository.HostDao;
import org.apache.bigtop.manager.dao.repository.ServiceDao;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.converter.ComponentConverter;
import org.apache.bigtop.manager.server.model.converter.ServiceConverter;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.dto.ComponentDTO;
import org.apache.bigtop.manager.server.model.dto.ComponentHostDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.command.ServiceCommandDTO;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class ServiceInstallJob extends AbstractServiceJob {

    private ServiceDao serviceDao;
    private HostDao hostDao;

    public ServiceInstallJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();

        this.serviceDao = SpringContextHolder.getBean(ServiceDao.class);
        this.hostDao = SpringContextHolder.getBean(HostDao.class);
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
                        ComponentQuery componentQuery = ComponentQuery.builder()
                                .clusterId(clusterPO.getId())
                                .name(componentName)
                                .build();
                        List<String> existHostnames = componentDao.findByQuery(componentQuery).stream()
                                .map(ComponentPO::getHostname)
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
            ServicePO servicePO = serviceDao.findByClusterIdAndServiceName(clusterId, serviceName);
            upsertService(servicePO, serviceCommand);
        }
    }

    private void upsertService(ServicePO servicePO, ServiceCommandDTO serviceCommand) {
        CommandDTO commandDTO = jobContext.getCommandDTO();
        Long clusterId = commandDTO.getClusterId();
        String serviceName = serviceCommand.getServiceName();
        ClusterPO clusterPO = clusterDao.findByIdJoin(clusterId);

        // 1. Persist service and components
        if (servicePO == null) {
            ServiceDTO serviceDTO = StackUtils.getServiceDTO(serviceName);
            servicePO = ServiceConverter.INSTANCE.fromDTO2PO(serviceDTO);
            servicePO.setClusterId(clusterId);
            serviceDao.save(servicePO);
        }

        // TODO 2. Update configs

        for (ComponentHostDTO componentHostDTO : serviceCommand.getComponentHosts()) {
            String componentName = componentHostDTO.getComponentName();
            List<String> hostnames = componentHostDTO.getHostnames();
            List<HostPO> hostPOList = hostDao.findAllByHostnameIn(hostnames);

            // 3. Persist components
            for (HostPO hostPO : hostPOList) {
                ComponentQuery componentQuery = ComponentQuery.builder()
                        .clusterId(clusterPO.getId())
                        .hostId(hostPO.getId())
                        .name(componentName)
                        .build();
                List<ComponentPO> componentPOList = componentDao.findByQuery(componentQuery);
                if (CollectionUtils.isEmpty(componentPOList)) {
                    ComponentDTO componentDTO = StackUtils.getComponentDTO(componentName);
                    ComponentPO componentPO = ComponentConverter.INSTANCE.fromDTO2PO(componentDTO);
                    componentPO.setClusterId(clusterPO.getId());
                    componentPO.setHostId(hostPO.getId());
                    componentPO.setServiceId(servicePO.getId());
                    componentDao.save(componentPO);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Install services";
    }
}
