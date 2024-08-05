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
package org.apache.bigtop.manager.server.command.stage;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.repository.ClusterRepository;
import org.apache.bigtop.manager.server.command.task.HostCheckTask;
import org.apache.bigtop.manager.server.command.task.Task;
import org.apache.bigtop.manager.server.command.task.TaskContext;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;

public class HostCheckStage extends AbstractStage {

    private ClusterRepository clusterRepository;

    public HostCheckStage(StageContext stageContext) {
        super(stageContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();

        this.clusterRepository = SpringContextHolder.getBean(ClusterRepository.class);
    }

    @Override
    protected void beforeCreateTasks() {
        if (stageContext.getClusterId() != null) {
            ClusterPO clusterPO = clusterRepository.getReferenceById(stageContext.getClusterId());

            stageContext.setStackName(clusterPO.getStackPO().getStackName());
            stageContext.setStackVersion(clusterPO.getStackPO().getStackVersion());
        }
    }

    @Override
    protected Task createTask(String hostname) {
        TaskContext taskContext = new TaskContext();
        taskContext.setHostname(hostname);
        taskContext.setClusterId(stageContext.getClusterId());
        taskContext.setClusterName(stageContext.getClusterName());
        taskContext.setStackName(stageContext.getStackName());
        taskContext.setStackVersion(stageContext.getStackVersion());
        taskContext.setServiceName("cluster");
        taskContext.setServiceUser("root");
        taskContext.setServiceGroup("root");
        taskContext.setComponentName("agent");
        taskContext.setComponentDisplayName("Agent");
        taskContext.setCommand(Command.CUSTOM);
        taskContext.setCustomCommand("check_host");

        return new HostCheckTask(taskContext);
    }

    @Override
    public String getName() {
        return "Check hosts";
    }
}
