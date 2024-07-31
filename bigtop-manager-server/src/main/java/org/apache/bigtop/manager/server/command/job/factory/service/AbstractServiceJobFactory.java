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
package org.apache.bigtop.manager.server.command.job.factory.service;

import org.apache.bigtop.manager.common.constants.ComponentCategories;
import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.po.HostComponentPO;
import org.apache.bigtop.manager.dao.repository.ComponentRepository;
import org.apache.bigtop.manager.dao.repository.HostComponentRepository;
import org.apache.bigtop.manager.server.command.job.factory.AbstractJobFactory;
import org.apache.bigtop.manager.server.command.stage.factory.StageContext;
import org.apache.bigtop.manager.server.command.stage.factory.StageFactories;
import org.apache.bigtop.manager.server.command.stage.factory.StageType;
import org.apache.bigtop.manager.server.exception.ServerException;
import org.apache.bigtop.manager.server.model.dto.ComponentDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.command.ServiceCommandDTO;
import org.apache.bigtop.manager.server.stack.dag.ComponentCommandWrapper;
import org.apache.bigtop.manager.server.stack.dag.DAG;
import org.apache.bigtop.manager.server.stack.dag.DagGraphEdge;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.apache.commons.collections4.CollectionUtils;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * A Service Job can be seen as a collection of multiple Components and Hosts,
 * so it can directly inherit from AbstractComponentJobFactory.
 */
public abstract class AbstractServiceJobFactory extends AbstractJobFactory {

    @Resource
    protected ComponentRepository componentRepository;

    @Resource
    protected HostComponentRepository hostComponentRepository;

    protected String stackName;

    protected String stackVersion;

    protected DAG<String, ComponentCommandWrapper, DagGraphEdge> dag;

    protected void initAttrs() {
        stackName = clusterPO.getStackPO().getStackName();
        stackVersion = clusterPO.getStackPO().getStackVersion();
        dag = StackUtils.getStackDagMap().get(StackUtils.fullStackName(stackName, stackVersion));
    }

    protected StageContext createStageContext(String serviceName, String componentName, List<String> hostnames) {
        ServiceDTO serviceDTO = StackUtils.getServiceDTO(stackName, stackVersion, serviceName);
        ComponentDTO componentDTO = StackUtils.getComponentDTO(stackName, stackVersion, componentName);

        StageContext stageContext = StageContext.fromPayload(JsonUtils.writeAsString(jobContext.getCommandDTO()));
        stageContext.setServiceDTO(serviceDTO);
        stageContext.setComponentDTO(componentDTO);
        stageContext.setStackName(stackName);
        stageContext.setStackVersion(stackVersion);
        stageContext.setHostnames(hostnames);

        return stageContext;
    }

    protected List<String> getTodoListForCommand(Command command) {
        try {
            List<String> orderedList = dag.topologicalSort();
            List<String> componentNames = getComponentNames();
            List<String> componentCommandNames = new ArrayList<>(componentNames.stream()
                    .map(x -> x + "-" + command.name().toUpperCase())
                    .toList());

            orderedList.retainAll(componentCommandNames);
            componentCommandNames.removeAll(orderedList);
            orderedList.addAll(componentCommandNames);

            return orderedList;
        } catch (Exception e) {
            throw new ServerException(e);
        }
    }

    protected List<String> getComponentNames() {
        List<String> serviceNames = getServiceNames();
        List<ComponentPO> componentPOList =
                componentRepository.findAllByClusterPOIdAndServicePOServiceNameIn(clusterPO.getId(), serviceNames);

        return componentPOList.stream().map(ComponentPO::getComponentName).toList();
    }

    protected String findServiceNameByComponentName(String componentName) {
        for (ServiceDTO serviceDTO : StackUtils.getServiceDTOList(stackName, stackVersion)) {
            for (ComponentDTO componentDTO : serviceDTO.getComponents()) {
                if (componentDTO.getComponentName().equals(componentName)) {
                    return serviceDTO.getServiceName();
                }
            }
        }

        return null;
    }

    protected Boolean isMasterComponent(String componentName) {
        ComponentDTO componentDTO = StackUtils.getComponentDTO(stackName, stackVersion, componentName);
        return componentDTO.getCategory().equalsIgnoreCase(ComponentCategories.MASTER);
    }

    protected Boolean isSlaveComponent(String componentName) {
        ComponentDTO componentDTO = StackUtils.getComponentDTO(stackName, stackVersion, componentName);
        return componentDTO.getCategory().equalsIgnoreCase(ComponentCategories.SLAVE);
    }

    protected List<String> findHostnamesByComponentName(String componentName) {
        List<HostComponentPO> hostComponentPOList =
                hostComponentRepository.findAllByComponentPOClusterIdAndComponentPOComponentName(
                        clusterPO.getId(), componentName);
        if (hostComponentPOList == null) {
            return new ArrayList<>();
        } else {
            return hostComponentPOList.stream()
                    .map(HostComponentPO::getHostPO)
                    .map(HostPO::getHostname)
                    .toList();
        }
    }

    protected void createCacheStage() {
        StageContext stageContext = StageContext.fromPayload(JsonUtils.writeAsString(jobContext.getCommandDTO()));
        stageContext.setStackName(stackName);
        stageContext.setStackVersion(stackVersion);
        stagePOList.add(StageFactories.getStageFactory(StageType.CACHE_DISTRIBUTE).createStage(stageContext));
    }

    protected void createInstallStages() {
        List<String> todoList = getTodoListForCommand(Command.INSTALL);

        for (String componentCommand : todoList) {
            String[] split = componentCommand.split("-");
            String componentName = split[0];
            String serviceName = findServiceNameByComponentName(componentName);
            List<String> hostnames = findHostnamesByComponentName(componentName);
            if (CollectionUtils.isEmpty(hostnames)) {
                continue;
            }

            StageContext stageContext = createStageContext(serviceName, componentName, hostnames);
            stagePOList.add(
                    StageFactories.getStageFactory(StageType.COMPONENT_INSTALL).createStage(stageContext));
        }
    }

    protected void createStartStages() {
        List<String> todoList = getTodoListForCommand(Command.START);

        for (String componentCommand : todoList) {
            String[] split = componentCommand.split("-");
            String componentName = split[0];
            String serviceName = findServiceNameByComponentName(componentName);

            if (!(isMasterComponent(componentName) || isSlaveComponent(componentName))) {
                continue;
            }

            List<String> hostnames = findHostnamesByComponentName(componentName);
            if (CollectionUtils.isEmpty(hostnames)) {
                continue;
            }

            StageContext stageContext = createStageContext(serviceName, componentName, hostnames);
            stagePOList.add(StageFactories.getStageFactory(StageType.COMPONENT_START).createStage(stageContext));
        }
    }

    protected void createStopStages() {
        List<String> todoList = getTodoListForCommand(Command.STOP);

        for (String componentCommand : todoList) {
            String[] split = componentCommand.split("-");
            String componentName = split[0];
            String serviceName = findServiceNameByComponentName(componentName);

            if (!(isMasterComponent(componentName) || isSlaveComponent(componentName))) {
                continue;
            }

            List<String> hostnames = findHostnamesByComponentName(componentName);
            if (CollectionUtils.isEmpty(hostnames)) {
                continue;
            }

            StageContext stageContext = createStageContext(serviceName, componentName, hostnames);
            stagePOList.add(StageFactories.getStageFactory(StageType.COMPONENT_STOP).createStage(stageContext));
        }
    }

    protected void createCheckStages() {
        List<String> todoList = getTodoListForCommand(Command.CHECK);

        for (String componentCommand : todoList) {
            String[] split = componentCommand.split("-");
            String componentName = split[0];
            String serviceName = findServiceNameByComponentName(componentName);

            if (!isMasterComponent(componentName)) {
                continue;
            }

            List<String> hostnames = findHostnamesByComponentName(componentName);
            if (CollectionUtils.isEmpty(hostnames)) {
                continue;
            }

            StageContext stageContext = createStageContext(serviceName, componentName, List.of(hostnames.get(0)));
            stagePOList.add(StageFactories.getStageFactory(StageType.COMPONENT_CHECK).createStage(stageContext));
        }
    }

    private List<String> getServiceNames() {
        return jobContext.getCommandDTO().getServiceCommands().stream()
                .map(ServiceCommandDTO::getServiceName)
                .toList();
    }
}
