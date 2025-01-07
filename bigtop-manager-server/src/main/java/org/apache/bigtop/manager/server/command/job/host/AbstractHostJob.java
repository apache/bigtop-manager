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
package org.apache.bigtop.manager.server.command.job.host;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.query.ComponentQuery;
import org.apache.bigtop.manager.dao.repository.ComponentDao;
import org.apache.bigtop.manager.server.command.job.AbstractJob;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.command.stage.ComponentStartStage;
import org.apache.bigtop.manager.server.command.stage.ComponentStopStage;
import org.apache.bigtop.manager.server.command.stage.StageContext;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.dto.ComponentDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.utils.StackDAGUtils;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHostJob extends AbstractJob {

    protected ComponentDao componentDao;

    public AbstractHostJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();

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

    protected void createStartStages() {
        List<ComponentPO> componentPOList = getComponentPOList();
        List<String> todoList = StackDAGUtils.getTodoList(getComponentNames(componentPOList), Command.START);

        for (String componentCommand : todoList) {
            String[] split = componentCommand.split("-");
            String componentName = split[0];

            if (StackUtils.isClientComponent(componentName)) {
                continue;
            }

            List<String> hostnames = getHostnames();
            if (CollectionUtils.isEmpty(hostnames)) {
                continue;
            }

            StageContext stageContext = createStageContext(componentName, hostnames);
            stages.add(new ComponentStartStage(stageContext));
        }
    }

    protected void createStopStages() {
        List<ComponentPO> componentPOList = getComponentPOList();
        List<String> todoList = StackDAGUtils.getTodoList(getComponentNames(componentPOList), Command.STOP);

        for (String componentCommand : todoList) {
            String[] split = componentCommand.split("-");
            String componentName = split[0];

            if (StackUtils.isClientComponent(componentName)) {
                continue;
            }

            List<String> hostnames = getHostnames();
            if (CollectionUtils.isEmpty(hostnames)) {
                continue;
            }

            StageContext stageContext = createStageContext(componentName, hostnames);
            stages.add(new ComponentStopStage(stageContext));
        }
    }

    private List<ComponentPO> getComponentPOList() {
        ComponentQuery query = ComponentQuery.builder()
                .clusterId(clusterPO.getId())
                .hostnames(getHostnames())
                .build();
        return componentDao.findByQuery(query);
    }

    private List<String> getComponentNames(List<ComponentPO> componentPOList) {
        if (componentPOList == null) {
            return new ArrayList<>();
        } else {
            return componentPOList.stream().map(ComponentPO::getName).distinct().toList();
        }
    }

    private List<String> getHostnames() {
        return jobContext.getCommandDTO().getHostCommands().stream()
                .flatMap(hostCommandDTO -> hostCommandDTO.getHostnames().stream())
                .toList();
    }
}
