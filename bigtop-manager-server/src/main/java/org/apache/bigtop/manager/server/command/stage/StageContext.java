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

import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.dto.ComponentDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.command.ClusterCommandDTO;

import lombok.Data;

import java.util.List;

@Data
public class StageContext {

    private Long clusterId;

    private String clusterName;

    private String userGroup;

    private String rootDir;

    private List<String> hostnames;

    private ServiceDTO serviceDTO;

    private ComponentDTO componentDTO;

    public static StageContext fromPayload(String payload) {
        CommandDTO commandDTO = JsonUtils.readFromString(payload, CommandDTO.class);
        return fromCommandDTO(commandDTO);
    }

    public static StageContext fromCommandDTO(CommandDTO commandDTO) {
        StageContext context = new StageContext();
        context.setClusterId(commandDTO.getClusterId());

        switch (commandDTO.getCommandLevel()) {
            case CLUSTER -> fromClusterCommandPayload(context, commandDTO);
            case HOST -> fromHostCommandPayload(context, commandDTO);
            case SERVICE -> fromServiceCommandPayload(context, commandDTO);
            case COMPONENT -> fromComponentCommandPayload(context, commandDTO);
        }

        return context;
    }

    private static void fromClusterCommandPayload(StageContext context, CommandDTO commandDTO) {
        ClusterCommandDTO clusterCommand = commandDTO.getClusterCommand();

        context.setClusterName(clusterCommand.getName());
        context.setHostnames(clusterCommand.getHosts().getHostnames());
        context.setUserGroup(clusterCommand.getUserGroup());
        context.setRootDir(clusterCommand.getRootDir());
    }

    private static void fromHostCommandPayload(StageContext context, CommandDTO commandDTO) {
        // No need to set anything here, we should deal with this in the host job factory
    }

    private static void fromServiceCommandPayload(StageContext context, CommandDTO commandDTO) {
        // No need to set anything here, we should deal with this in the service job factory
    }

    private static void fromComponentCommandPayload(StageContext context, CommandDTO commandDTO) {
        // No need to set anything here, we should deal with this in the component job factory
    }
}
