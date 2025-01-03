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

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.query.ComponentQuery;
import org.apache.bigtop.manager.dao.repository.ComponentDao;
import org.apache.bigtop.manager.dao.repository.HostDao;
import org.apache.bigtop.manager.dao.repository.ServiceConfigDao;
import org.apache.bigtop.manager.dao.repository.ServiceConfigSnapshotDao;
import org.apache.bigtop.manager.dao.repository.ServiceDao;
import org.apache.bigtop.manager.server.command.stage.CacheFileUpdateStage;
import org.apache.bigtop.manager.server.command.stage.ComponentAddStage;
import org.apache.bigtop.manager.server.command.stage.ComponentCheckStage;
import org.apache.bigtop.manager.server.command.stage.ComponentConfigureStage;
import org.apache.bigtop.manager.server.command.stage.ComponentStartStage;
import org.apache.bigtop.manager.server.command.stage.ComponentStopStage;
import org.apache.bigtop.manager.server.command.stage.StageContext;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.dto.ComponentDTO;
import org.apache.bigtop.manager.server.model.dto.ComponentHostDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.command.ServiceCommandDTO;
import org.apache.bigtop.manager.server.utils.StackDAGUtils;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractServiceJob extends AbstractJob {

    protected ServiceDao serviceDao;
    protected ServiceConfigDao serviceConfigDao;
    protected ServiceConfigSnapshotDao serviceConfigSnapshotDao;
    protected ComponentDao componentDao;
    protected HostDao hostDao;

    public AbstractServiceJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();

        this.serviceDao = SpringContextHolder.getBean(ServiceDao.class);
        this.serviceConfigDao = SpringContextHolder.getBean(ServiceConfigDao.class);
        this.serviceConfigSnapshotDao = SpringContextHolder.getBean(ServiceConfigSnapshotDao.class);
        this.componentDao = SpringContextHolder.getBean(ComponentDao.class);
        this.hostDao = SpringContextHolder.getBean(HostDao.class);
    }

    @Override
    protected void beforeCreateStages() {
        super.beforeCreateStages();
    }

    protected StageContext createStageContext(String serviceName, String componentName, List<String> hostnames) {
        StageContext stageContext = StageContext.fromCommandDTO(jobContext.getCommandDTO());

        ServiceDTO serviceDTO = StackUtils.getServiceDTO(serviceName);
        ComponentDTO componentDTO = StackUtils.getComponentDTO(componentName);

        stageContext.setHostnames(hostnames);
        stageContext.setServiceDTO(serviceDTO);
        stageContext.setComponentDTO(componentDTO);

        return stageContext;
    }

    protected List<String> getComponentNames() {
        List<String> serviceNames = getServiceNames();
        ComponentQuery componentQuery = ComponentQuery.builder()
                .clusterId(clusterPO.getId())
                .serviceNames(serviceNames)
                .build();
        List<ComponentPO> componentPOList = componentDao.findByQuery(componentQuery);

        return componentPOList.stream().map(ComponentPO::getName).distinct().toList();
    }

    protected String findServiceNameByComponentName(String componentName) {
        return StackUtils.getServiceDTOByComponentName(componentName).getName();
    }

    protected List<String> findHostnamesByComponentName(String componentName) {
        ComponentQuery componentQuery = ComponentQuery.builder()
                .clusterId(clusterPO.getId())
                .name(componentName)
                .build();
        List<ComponentPO> componentPOList = componentDao.findByQuery(componentQuery);
        if (componentPOList == null) {
            return new ArrayList<>();
        } else {
            return componentPOList.stream().map(ComponentPO::getHostname).toList();
        }
    }

    protected void createCacheStage() {
        StageContext stageContext = StageContext.fromCommandDTO(jobContext.getCommandDTO());
        stages.add(new CacheFileUpdateStage(stageContext));
    }

    protected void createAddStages() {
        List<String> todoList = StackDAGUtils.getTodoList(getComponentNames(), Command.ADD);

        for (String componentCommand : todoList) {
            String[] split = componentCommand.split("-");
            String componentName = split[0];
            String serviceName = findServiceNameByComponentName(componentName);
            List<String> hostnames = findHostnamesByComponentName(componentName);
            if (CollectionUtils.isEmpty(hostnames)) {
                continue;
            }

            StageContext stageContext = createStageContext(serviceName, componentName, hostnames);
            stages.add(new ComponentAddStage(stageContext));
        }
    }

    protected void createConfigureStages() {
        for (ServiceCommandDTO serviceCommand : jobContext.getCommandDTO().getServiceCommands()) {
            for (ComponentHostDTO componentHost : serviceCommand.getComponentHosts()) {
                String serviceName = serviceCommand.getServiceName();
                String componentName = componentHost.getComponentName();
                List<String> hostnames = componentHost.getHostnames();

                StageContext stageContext = createStageContext(serviceName, componentName, hostnames);
                stages.add(new ComponentConfigureStage(stageContext));
            }
        }
    }

    protected void createStartStages() {
        List<String> todoList = StackDAGUtils.getTodoList(getComponentNames(), Command.START);

        for (String componentCommand : todoList) {
            String[] split = componentCommand.split("-");
            String componentName = split[0];
            String serviceName = findServiceNameByComponentName(componentName);

            if (StackUtils.isClientComponent(componentName)) {
                continue;
            }

            List<String> hostnames = findHostnamesByComponentName(componentName);
            if (CollectionUtils.isEmpty(hostnames)) {
                continue;
            }

            StageContext stageContext = createStageContext(serviceName, componentName, hostnames);
            stages.add(new ComponentStartStage(stageContext));
        }
    }

    protected void createStopStages() {
        List<String> todoList = StackDAGUtils.getTodoList(getComponentNames(), Command.STOP);

        for (String componentCommand : todoList) {
            String[] split = componentCommand.split("-");
            String componentName = split[0];
            String serviceName = findServiceNameByComponentName(componentName);

            if (StackUtils.isClientComponent(componentName)) {
                continue;
            }

            List<String> hostnames = findHostnamesByComponentName(componentName);
            if (CollectionUtils.isEmpty(hostnames)) {
                continue;
            }

            StageContext stageContext = createStageContext(serviceName, componentName, hostnames);
            stages.add(new ComponentStopStage(stageContext));
        }
    }

    protected void createCheckStages() {
        List<String> todoList = StackDAGUtils.getTodoList(getComponentNames(), Command.CHECK);

        for (String componentCommand : todoList) {
            String[] split = componentCommand.split("-");
            String componentName = split[0];
            String serviceName = findServiceNameByComponentName(componentName);

            if (StackUtils.isClientComponent(componentName)) {
                continue;
            }

            List<String> hostnames = findHostnamesByComponentName(componentName);
            if (CollectionUtils.isEmpty(hostnames)) {
                continue;
            }

            StageContext stageContext = createStageContext(serviceName, componentName, List.of(hostnames.get(0)));
            stages.add(new ComponentCheckStage(stageContext));
        }
    }

    private List<String> getServiceNames() {
        return jobContext.getCommandDTO().getServiceCommands().stream()
                .map(ServiceCommandDTO::getServiceName)
                .toList();
    }
}
