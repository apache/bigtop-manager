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

import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.repository.ClusterDao;
import org.apache.bigtop.manager.server.command.task.TaskContext;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.dto.ComponentDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractComponentStage extends AbstractStage {

    private ClusterDao clusterDao;

    private ClusterPO clusterPO;

    public AbstractComponentStage(StageContext stageContext) {
        super(stageContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();

        this.clusterDao = SpringContextHolder.getBean(ClusterDao.class);
    }

    @Override
    protected void beforeCreateTasks() {
        this.clusterPO = clusterDao.findById(stageContext.getClusterId());
    }

    @Override
    protected String getServiceName() {
        return stageContext.getServiceDTO().getServiceName();
    }

    @Override
    protected String getComponentName() {
        return stageContext.getComponentDTO().getComponentName();
    }

    protected TaskContext createTaskContext(TaskContext taskContext) {
        ServiceDTO serviceDTO = stageContext.getServiceDTO();
        ComponentDTO componentDTO = stageContext.getComponentDTO();

        taskContext.setClusterId(clusterPO.getId());
        taskContext.setClusterName(clusterPO.getClusterName());
        taskContext.setServiceName(serviceDTO.getServiceName());
        taskContext.setStackName(stageContext.getStackName());
        taskContext.setStackVersion(stageContext.getStackVersion());
        taskContext.setComponentName(componentDTO.getComponentName());
        taskContext.setComponentDisplayName(componentDTO.getDisplayName());
        taskContext.setServiceUser(serviceDTO.getServiceUser());
        taskContext.setRoot(clusterPO.getRoot());

        Map<String, Object> properties = new HashMap<>();
        properties.put("customCommands", componentDTO.getCustomCommands());
        properties.put("packageSpecifics", serviceDTO.getPackageSpecifics());
        properties.put("commandScript", componentDTO.getCommandScript());
        taskContext.setProperties(properties);
        return taskContext;
    }
}
