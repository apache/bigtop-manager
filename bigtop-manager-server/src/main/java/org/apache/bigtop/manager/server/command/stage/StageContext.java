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
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
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

    private String serviceName;

    private String componentName;

    public static StageContext fromCommandDTO(CommandDTO commandDTO) {
        StageContext context = new StageContext();

        if (commandDTO.getClusterId() != null) {
            ClusterDao clusterDao = SpringContextHolder.getBean(ClusterDao.class);
            ClusterPO clusterPO = clusterDao.findById(commandDTO.getClusterId());
            context.setClusterId(clusterPO.getId());
            context.setClusterName(clusterPO.getName());
            context.setUserGroup(clusterPO.getUserGroup());
            context.setRootDir(clusterPO.getRootDir());
        }

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

        if (context.getClusterId() == null) {
            List<String> hostnames = clusterCommand.getHosts().stream()
                    .flatMap(hostDTO -> hostDTO.getHostnames().stream())
                    .toList();
            context.setHostnames(hostnames);
            context.setClusterName(clusterCommand.getName());
            context.setUserGroup(clusterCommand.getUserGroup());
            context.setRootDir(clusterCommand.getRootDir());
        }
    }

    private static void fromHostCommandPayload(StageContext context, CommandDTO commandDTO) {
        List<String> hostnames = commandDTO.getHostCommands().stream()
                .flatMap(hostDTO -> hostDTO.getHostnames().stream())
                .toList();
        context.setHostnames(hostnames);
    }

    private static void fromServiceCommandPayload(StageContext context, CommandDTO commandDTO) {
        // No need to set anything here, we should deal with this in the service job factory
    }

    private static void fromComponentCommandPayload(StageContext context, CommandDTO commandDTO) {
        // No need to set anything here, we should deal with this in the component job factory
    }
}
