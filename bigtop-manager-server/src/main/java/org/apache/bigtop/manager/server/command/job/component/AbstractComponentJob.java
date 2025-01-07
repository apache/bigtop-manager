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

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.dao.repository.ComponentDao;
import org.apache.bigtop.manager.dao.repository.HostDao;
import org.apache.bigtop.manager.dao.repository.ServiceDao;
import org.apache.bigtop.manager.server.command.job.AbstractJob;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.command.stage.CacheFileUpdateStage;
import org.apache.bigtop.manager.server.command.stage.ComponentAddStage;
import org.apache.bigtop.manager.server.command.stage.ComponentConfigureStage;
import org.apache.bigtop.manager.server.command.stage.ComponentStartStage;
import org.apache.bigtop.manager.server.command.stage.ComponentStopStage;
import org.apache.bigtop.manager.server.command.stage.StageContext;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.dto.ComponentDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.command.ComponentCommandDTO;
import org.apache.bigtop.manager.server.utils.StackDAGUtils;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractComponentJob extends AbstractJob {

    protected HostDao hostDao;
    protected ServiceDao serviceDao;
    protected ComponentDao componentDao;

    public AbstractComponentJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();

        this.hostDao = SpringContextHolder.getBean(HostDao.class);
        this.serviceDao = SpringContextHolder.getBean(ServiceDao.class);
        this.componentDao = SpringContextHolder.getBean(ComponentDao.class);
    }

    @Override
    protected void beforeCreateStages() {
        super.beforeCreateStages();
    }

    protected StageContext createStageContext(String componentName, List<String> hostnames) {
        StageContext stageContext = StageContext.fromCommandDTO(jobContext.getCommandDTO());

        ServiceDTO serviceDTO = StackUtils.getServiceDTOByComponentName(componentName);
        ComponentDTO componentDTO = StackUtils.getComponentDTO(componentName);

        stageContext.setHostnames(hostnames);
        stageContext.setServiceDTO(serviceDTO);
        stageContext.setComponentDTO(componentDTO);

        return stageContext;
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
            List<String> hostnames = getHostnames(componentName);
            if (CollectionUtils.isEmpty(hostnames)) {
                continue;
            }

            StageContext stageContext = createStageContext(componentName, hostnames);
            stages.add(new ComponentAddStage(stageContext));
        }
    }

    protected void createConfigureStages() {
        for (ComponentCommandDTO componentCommand : jobContext.getCommandDTO().getComponentCommands()) {
            String componentName = componentCommand.getComponentName();
            List<String> hostnames = componentCommand.getHostnames();

            StageContext stageContext = createStageContext(componentName, hostnames);
            stages.add(new ComponentConfigureStage(stageContext));
        }
    }

    protected void createStartStages() {
        List<String> todoList = StackDAGUtils.getTodoList(getComponentNames(), Command.START);

        for (String componentCommand : todoList) {
            String[] split = componentCommand.split("-");
            String componentName = split[0];

            if (StackUtils.isClientComponent(componentName)) {
                continue;
            }

            List<String> hostnames = getHostnames(componentName);
            if (CollectionUtils.isEmpty(hostnames)) {
                continue;
            }

            StageContext stageContext = createStageContext(componentName, hostnames);
            stages.add(new ComponentStartStage(stageContext));
        }
    }

    protected void createStopStages() {
        List<String> todoList = StackDAGUtils.getTodoList(getComponentNames(), Command.STOP);

        for (String componentCommand : todoList) {
            String[] split = componentCommand.split("-");
            String componentName = split[0];

            if (StackUtils.isClientComponent(componentName)) {
                continue;
            }

            List<String> hostnames = getHostnames(componentName);
            if (CollectionUtils.isEmpty(hostnames)) {
                continue;
            }

            StageContext stageContext = createStageContext(componentName, hostnames);
            stages.add(new ComponentStopStage(stageContext));
        }
    }

    private List<String> getComponentNames() {
        return jobContext.getCommandDTO().getComponentCommands().stream()
                .map(ComponentCommandDTO::getComponentName)
                .toList();
    }

    private List<String> getHostnames(String componentName) {
        for (ComponentCommandDTO componentCommand : jobContext.getCommandDTO().getComponentCommands()) {
            if (componentCommand.getComponentName().equals(componentName)) {
                return componentCommand.getHostnames();
            }
        }

        return new ArrayList<>();
    }
}
