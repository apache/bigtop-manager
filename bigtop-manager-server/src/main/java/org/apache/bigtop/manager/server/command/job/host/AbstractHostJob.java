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

import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.query.ComponentQuery;
import org.apache.bigtop.manager.dao.repository.ComponentDao;
import org.apache.bigtop.manager.server.command.job.AbstractJob;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.command.stage.Stage;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    protected Map<String, List<String>> getComponentHostsMap() {
        List<String> hostnames = getHostnames();
        ComponentQuery componentQuery = ComponentQuery.builder()
                .clusterId(clusterPO.getId())
                .hostnames(hostnames)
                .build();
        List<ComponentPO> componentPOList = componentDao.findByQuery(componentQuery);
        Map<String, List<String>> componentHostsMap = new HashMap<>();
        for (ComponentPO componentPO : componentPOList) {
            List<String> hosts = componentHostsMap.computeIfAbsent(componentPO.getName(), k -> new ArrayList<>());
            hosts.add(componentPO.getHostname());
        }

        return componentHostsMap;
    }

    private List<String> getHostnames() {
        return jobContext.getCommandDTO().getHostCommands().stream()
                .flatMap(hostCommandDTO -> hostCommandDTO.getHostnames().stream())
                .toList();
    }

    protected void setJobContextAndStagesForTest(JobContext jobContext, List<Stage> stages) {
        this.jobContext = jobContext;
        this.stages = stages;
    }
}
