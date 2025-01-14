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
import org.apache.bigtop.manager.server.enums.HealthyStatusEnum;

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
        ComponentPO componentPO = componentDao.findByQuery(componentQuery).get(0);
        componentPO.setStatus(HealthyStatusEnum.HEALTHY.getCode());
        componentDao.partialUpdateById(componentPO);
    }

    @Override
    public String getName() {
        return "Start " + taskContext.getComponentDisplayName() + " on " + taskContext.getHostname();
    }
}
