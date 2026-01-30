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

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.query.ComponentQuery;
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.server.enums.HealthyStatusEnum;
import org.apache.bigtop.manager.server.exception.ServerException;
import org.apache.bigtop.manager.server.model.dto.StackDTO;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.apache.commons.collections4.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ComponentStartTask extends AbstractComponentTask {

    public ComponentStartTask(TaskContext taskContext) {
        super(taskContext);
    }

    @Override
    protected Command getCommand() {
        return Command.START;
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
        boolean isNew = CollectionUtils.isEmpty(componentPOList);
        if (isNew) {
            log.warn("Component [{}] on host [{}] not found in DB during START, creating new entry. This may indicate an issue in the ADD task.", componentName, hostname);
            componentPO = new ComponentPO();
        } else {
            componentPO = componentPOList.get(0);
        }

        // If new or existing but incomplete, fill in the details
        if (isNew || componentPO.getHostId() == null) {
            log.info("Populating full component details for component [{}] on host [{}]. New entry: {}", componentName, hostname, isNew);
            HostPO hostPO = hostDao.findByHostname(hostname);
            if (hostPO == null) {
                throw new ServerException("Host not found in database: " + hostname);
            }
            StackDTO stackDTO = StackUtils.getServiceStack(taskContext.getServiceName());

            componentPO.setName(componentName);
            componentPO.setHostname(hostname);
            componentPO.setClusterId(taskContext.getClusterId());
            componentPO.setHostId(hostPO.getId());
            componentPO.setServiceId(taskContext.getServiceId());
            componentPO.setServiceName(taskContext.getServiceName());
            componentPO.setServiceUser(taskContext.getServiceUser());
            componentPO.setStack(stackDTO.getStackName() + "-" + stackDTO.getStackVersion());
        }

        componentPO.setStatus(HealthyStatusEnum.HEALTHY.getCode());

        if (componentPO.getId() == null) {
            componentDao.save(componentPO);
        } else {
            componentDao.partialUpdateById(componentPO);
        }
    }

    @Override
    public String getName() {
        return "Start " + taskContext.getComponentDisplayName() + " on " + taskContext.getHostname();
    }
}
