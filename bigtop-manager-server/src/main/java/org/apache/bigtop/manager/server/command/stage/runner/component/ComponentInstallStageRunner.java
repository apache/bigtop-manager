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
package org.apache.bigtop.manager.server.command.stage.runner.component;

import org.apache.bigtop.manager.common.enums.MaintainState;
import org.apache.bigtop.manager.dao.po.HostComponentPO;
import org.apache.bigtop.manager.dao.po.Task;
import org.apache.bigtop.manager.dao.repository.HostComponentRepository;
import org.apache.bigtop.manager.server.command.stage.factory.StageType;
import org.apache.bigtop.manager.server.command.stage.runner.AbstractStageRunner;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;

@Slf4j
@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ComponentInstallStageRunner extends AbstractStageRunner {

    @Resource
    private HostComponentRepository hostComponentRepository;

    @Override
    public StageType getStageType() {
        return StageType.COMPONENT_INSTALL;
    }

    @Override
    public void onTaskSuccess(Task task) {
        super.onTaskSuccess(task);

        Long clusterId = task.getClusterPO().getId();
        String componentName = task.getComponentName();
        String hostname = task.getHostname();
        HostComponentPO hostComponentPO =
                hostComponentRepository.findByComponentClusterIdAndComponentComponentNameAndHostHostname(
                        clusterId, componentName, hostname);
        hostComponentPO.setState(MaintainState.INSTALLED);
        hostComponentRepository.save(hostComponentPO);
    }
}
