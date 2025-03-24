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
package org.apache.bigtop.manager.server.command.job.service;

import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.query.ComponentQuery;
import org.apache.bigtop.manager.dao.repository.ComponentDao;
import org.apache.bigtop.manager.dao.repository.HostDao;
import org.apache.bigtop.manager.dao.repository.ServiceConfigDao;
import org.apache.bigtop.manager.dao.repository.ServiceConfigSnapshotDao;
import org.apache.bigtop.manager.dao.repository.ServiceDao;
import org.apache.bigtop.manager.server.command.job.AbstractJob;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.dto.command.ServiceCommandDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractServiceJob extends AbstractJob {

    protected ServiceDao serviceDao;
    protected ServiceConfigDao serviceConfigDao;
    protected ServiceConfigSnapshotDao serviceConfigSnapshotDao;
    protected ComponentDao componentDao;
    protected HostDao hostDao;

    public AbstractServiceJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();

        this.serviceDao = SpringContextHolder.getBean(ServiceDao.class);
        this.serviceConfigDao = SpringContextHolder.getBean(ServiceConfigDao.class);
        this.serviceConfigSnapshotDao = SpringContextHolder.getBean(ServiceConfigSnapshotDao.class);
        this.componentDao = SpringContextHolder.getBean(ComponentDao.class);
        this.hostDao = SpringContextHolder.getBean(HostDao.class);
    }

    @Override
    protected void beforeCreateStages() {
        super.beforeCreateStages();
    }

    protected Map<String, List<String>> getComponentHostsMap() {
        List<String> serviceNames = getServiceNames();
        ComponentQuery componentQuery = ComponentQuery.builder()
                .clusterId(clusterPO.getId())
                .serviceNames(serviceNames)
                .build();
        List<ComponentPO> componentPOList = componentDao.findByQuery(componentQuery);
        Map<String, List<String>> componentHostsMap = new HashMap<>();
        for (ComponentPO componentPO : componentPOList) {
            List<String> hosts = componentHostsMap.computeIfAbsent(componentPO.getName(), k -> new ArrayList<>());
            hosts.add(componentPO.getHostname());
        }

        return componentHostsMap;
    }

    private List<String> getServiceNames() {
        return jobContext.getCommandDTO().getServiceCommands().stream()
                .map(ServiceCommandDTO::getServiceName)
                .toList();
    }
}
