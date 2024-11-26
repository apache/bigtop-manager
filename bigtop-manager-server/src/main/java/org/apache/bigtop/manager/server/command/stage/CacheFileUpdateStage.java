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
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.server.command.task.CacheFileUpdateTask;
import org.apache.bigtop.manager.server.command.task.Task;
import org.apache.bigtop.manager.server.command.task.TaskContext;
import org.apache.bigtop.manager.server.model.dto.HostDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheFileUpdateStage extends AbstractStage {

    public CacheFileUpdateStage(StageContext stageContext) {
        super(stageContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();
    }

    @Override
    protected void beforeCreateTasks() {
        List<Long> hostIds = new ArrayList<>();

        if (stageContext.getClusterId() == null) {
            hostIds.addAll(stageContext.getHostIds() == null ? List.of() : stageContext.getHostIds());
        } else {
            hostIds.addAll(stageContext.getHostIds() == null ? List.of() : stageContext.getHostIds());
            hostIds.addAll(hostDao.findAllByClusterId(stageContext.getClusterId()).stream()
                    .map(HostPO::getId)
                    .toList());
        }

        stageContext.setHostIds(hostIds);
    }

    @Override
    protected Task createTask(HostDTO hostDTO) {
        TaskContext taskContext = new TaskContext();
        taskContext.setHostDTO(hostDTO);
        taskContext.setClusterId(stageContext.getClusterId());
        taskContext.setClusterName(stageContext.getClusterName());
        taskContext.setUserGroup(stageContext.getUserGroup());
        taskContext.setRootDir(stageContext.getRootDir());
        taskContext.setServiceName("cluster");
        taskContext.setServiceUser("root");
        taskContext.setComponentName("agent");
        taskContext.setComponentDisplayName("Agent");

        Map<String, Object> properties = new HashMap<>();
        properties.put("hostIds", stageContext.getHostIds());
        taskContext.setProperties(properties);

        return new CacheFileUpdateTask(taskContext);
    }

    @Override
    public String getName() {
        return "Update cache files";
    }
}
