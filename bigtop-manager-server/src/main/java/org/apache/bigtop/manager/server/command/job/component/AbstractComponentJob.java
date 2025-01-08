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
package org.apache.bigtop.manager.server.command.job.component;

import org.apache.bigtop.manager.dao.repository.ComponentDao;
import org.apache.bigtop.manager.dao.repository.HostDao;
import org.apache.bigtop.manager.dao.repository.ServiceDao;
import org.apache.bigtop.manager.server.command.job.AbstractJob;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractComponentJob extends AbstractJob {

    protected HostDao hostDao;
    protected ServiceDao serviceDao;
    protected ComponentDao componentDao;

    public AbstractComponentJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();

        this.hostDao = SpringContextHolder.getBean(HostDao.class);
        this.serviceDao = SpringContextHolder.getBean(ServiceDao.class);
        this.componentDao = SpringContextHolder.getBean(ComponentDao.class);
    }

    @Override
    protected void beforeCreateStages() {
        super.beforeCreateStages();
    }

    protected Map<String, List<String>> getComponentHostsMap() {
        Map<String, List<String>> componentHostsMap = new HashMap<>();

        jobContext.getCommandDTO().getComponentCommands().forEach(componentCommand -> {
            String componentName = componentCommand.getComponentName();
            List<String> hostnames = componentCommand.getHostnames();
            componentHostsMap.put(componentName, hostnames);
        });

        return componentHostsMap;
    }
}
