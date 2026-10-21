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

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.server.command.helper.ComponentStageHelper;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;

import java.util.List;
import java.util.Map;

public class ServiceConfigureJob extends AbstractServiceJob {

    public ServiceConfigureJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void createStages() {
        CommandDTO commandDTO = jobContext.getCommandDTO();
        Map<String, List<String>> componentHostsMap = getComponentHostsMap();

        // Order services for CONFIGURE and restart
        List<String> orderedServices =
                getOrderedServiceNamesForCommand(org.apache.bigtop.manager.common.enums.Command.CONFIGURE);
        for (String serviceName : orderedServices) {
            Map<String, List<String>> perServiceHosts = filterComponentHostsByService(componentHostsMap, serviceName);
            stages.addAll(ComponentStageHelper.createComponentStages(perServiceHosts, Command.CONFIGURE, commandDTO));
        }

        // Restart services stop then start respecting ordering
        List<String> stopServices =
                getOrderedServiceNamesForCommand(org.apache.bigtop.manager.common.enums.Command.STOP);
        for (String serviceName : stopServices) {
            Map<String, List<String>> perServiceHosts = filterComponentHostsByService(componentHostsMap, serviceName);
            stages.addAll(ComponentStageHelper.createComponentStages(perServiceHosts, Command.STOP, commandDTO));
        }
        List<String> startServices =
                getOrderedServiceNamesForCommand(org.apache.bigtop.manager.common.enums.Command.START);
        for (String serviceName : startServices) {
            Map<String, List<String>> perServiceHosts = filterComponentHostsByService(componentHostsMap, serviceName);
            stages.addAll(ComponentStageHelper.createComponentStages(perServiceHosts, Command.START, commandDTO));
        }
    }

    @Override
    public String getName() {
        return "Configure services";
    }
}
