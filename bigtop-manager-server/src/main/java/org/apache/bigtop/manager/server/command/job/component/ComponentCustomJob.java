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

import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.command.stage.ComponentCustomStage;
import org.apache.bigtop.manager.server.command.stage.StageContext;
import org.apache.bigtop.manager.server.exception.ServerException;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.utils.StackUtils;

import java.util.List;
import java.util.Map;

/**
 * Job for component custom command.
 */
public class ComponentCustomJob extends AbstractComponentJob {

    public ComponentCustomJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void createStages() {
        String customCommand = jobContext.getCommandDTO().getCustomCommand();
        if (customCommand == null || customCommand.isBlank()) {
            throw new ServerException("customCommand must not be blank for CUSTOM command");
        }

        Map<String, List<String>> componentHostsMap = getComponentHostsMap();
        for (Map.Entry<String, List<String>> e : componentHostsMap.entrySet()) {
            String componentName = e.getKey();
            if (componentName != null) {
                componentName = componentName.toLowerCase();
            }
            List<String> hostnames = e.getValue();
            if (hostnames == null || hostnames.isEmpty()) {
                continue;
            }

            StageContext stageContext = StageContext.fromCommandDTO(jobContext.getCommandDTO());
            stageContext.setHostnames(hostnames);
            stageContext.setComponentName(componentName);

            // AbstractComponentStage#createTaskContext() will call
            // StackUtils.getServiceDTO(stageContext.getServiceName()).
            // So serviceName must be set here.
            ServiceDTO serviceDTO = StackUtils.getServiceDTOByComponentName(componentName);
            stageContext.setServiceName(serviceDTO.getName());

            stages.add(new ComponentCustomStage(stageContext, customCommand));
        }
    }

    @Override
    public String getName() {
        return "Custom component command";
    }
}
