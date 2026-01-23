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
package org.apache.bigtop.manager.server.command.task;

import org.apache.bigtop.manager.common.constants.ComponentCategories;
import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.query.ComponentQuery;
import org.apache.bigtop.manager.server.enums.HealthyStatusEnum;
import org.apache.bigtop.manager.server.model.dto.ComponentDTO;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.apache.commons.collections4.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ComponentAddTask extends AbstractComponentTask {

    public ComponentAddTask(TaskContext taskContext) {
        super(taskContext);
    }

    @Override
    protected Command getCommand() {
        return Command.ADD;
    }

    @Override
    public void onSuccess() {
        super.onSuccess();

        String componentName = taskContext.getComponentName();
        String hostname = taskContext.getHostname();
        ComponentQuery componentQuery = ComponentQuery.builder()
                .clusterId(taskContext.getClusterId())
                .hostname(hostname)
                .name(componentName)
                .build();
        List<ComponentPO> componentPOList = componentDao.findByQuery(componentQuery);
        ComponentPO componentPO;
        if (CollectionUtils.isEmpty(componentPOList)) {
            log.info("Component [{}] on host [{}] not found in DB, creating new entry.", componentName, hostname);
            componentPO = new ComponentPO();
            componentPO.setName(componentName);
            componentPO.setHostname(hostname);
            componentPO.setClusterId(taskContext.getClusterId());
            componentPO.setServiceId(taskContext.getServiceId());
        } else {
            componentPO = componentPOList.get(0);
        }

        ComponentDTO componentDTO = StackUtils.getComponentDTO(componentName);
        if (componentDTO.getCategory().equalsIgnoreCase(ComponentCategories.CLIENT)) {
            // Client components should always be healthy after added
            componentPO.setStatus(HealthyStatusEnum.HEALTHY.getCode());
        } else {
            // Master/Slave components need to start before being healthy
            componentPO.setStatus(HealthyStatusEnum.UNHEALTHY.getCode());
        }

        if (componentPO.getId() == null) {
            componentDao.save(componentPO);
        } else {
            componentDao.partialUpdateById(componentPO);
        }
    }

    @Override
    public String getName() {
        return "Add " + taskContext.getComponentDisplayName() + " on " + taskContext.getHostname();
    }
}
