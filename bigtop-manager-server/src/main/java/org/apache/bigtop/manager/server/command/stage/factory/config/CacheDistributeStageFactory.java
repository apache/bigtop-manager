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
import org.apache.bigtop.manager.dao.entity.Host;
import org.apache.bigtop.manager.dao.entity.Task;
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
            hostnames.addAll(hostRepository.findAllByClusterId(context.getClusterId()).stream()
                    .map(Host::getHostname)
                    .toList());
        }

        stage.setName("Distribute Caches");

        List<Task> tasks = new ArrayList<>();
        hostnames = hostnames.stream().distinct().toList();
        for (String hostname : hostnames) {
            Task task = new Task();
            task.setName(stage.getName() + " on " + hostname);
            task.setStackName(context.getStackName());
            task.setStackVersion(context.getStackVersion());
            task.setHostname(hostname);
            task.setServiceName("cluster");
            task.setServiceUser("root");
            task.setServiceGroup("root");
            task.setComponentName("bigtop-manager-agent");
            task.setCommand(Command.CUSTOM);
            task.setCustomCommand("cache_host");
            tasks.add(task);
        }

        stage.setTasks(tasks);
    }
}
