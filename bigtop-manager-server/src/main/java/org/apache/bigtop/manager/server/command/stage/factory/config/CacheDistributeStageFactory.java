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
package org.apache.bigtop.manager.server.command.stage.factory.config;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.po.TaskPO;
import org.apache.bigtop.manager.dao.repository.HostRepository;
import org.apache.bigtop.manager.server.command.stage.factory.AbstractStageFactory;
import org.apache.bigtop.manager.server.command.stage.factory.StageType;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CacheDistributeStageFactory extends AbstractStageFactory {

    @Resource
    private HostRepository hostRepository;

    @Override
    public StageType getStageType() {
        return StageType.CACHE_DISTRIBUTE;
    }

    @Override
    public void doCreateStage() {
        List<String> hostnames = new ArrayList<>();
        if (context.getClusterId() == null) {
            hostnames.addAll(context.getHostnames());
        } else {
            hostnames.addAll(context.getHostnames() == null ? List.of() : context.getHostnames());
            hostnames.addAll(hostRepository.findAllByClusterPOId(context.getClusterId()).stream()
                    .map(HostPO::getHostname)
                    .toList());
        }

        stagePO.setName("Distribute Caches");

        List<TaskPO> taskPOList = new ArrayList<>();
        hostnames = hostnames.stream().distinct().toList();
        for (String hostname : hostnames) {
            TaskPO taskPO = new TaskPO();
            taskPO.setName(stagePO.getName() + " on " + hostname);
            taskPO.setStackName(context.getStackName());
            taskPO.setStackVersion(context.getStackVersion());
            taskPO.setHostname(hostname);
            taskPO.setServiceName("cluster");
            taskPO.setServiceUser("root");
            taskPO.setServiceGroup("root");
            taskPO.setComponentName("bigtop-manager-agent");
            taskPO.setCommand(Command.CUSTOM);
            taskPO.setCustomCommand("cache_host");
            taskPOList.add(taskPO);
        }

        stagePO.setTaskPOList(taskPOList);
    }
}
