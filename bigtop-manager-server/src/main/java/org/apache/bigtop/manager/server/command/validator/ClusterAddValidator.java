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
package org.apache.bigtop.manager.server.command.validator;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.repository.ClusterDao;
import org.apache.bigtop.manager.dao.repository.HostDao;
import org.apache.bigtop.manager.server.command.CommandIdentifier;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.enums.CommandLevel;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.model.dto.command.ClusterCommandDTO;

import org.apache.commons.collections4.CollectionUtils;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

@Component
public class ClusterAddValidator implements CommandValidator {

    @Resource
    private ClusterDao clusterDao;

    @Resource
    private HostDao hostDao;

    @Override
    public List<CommandIdentifier> getCommandIdentifiers() {
        return List.of(new CommandIdentifier(CommandLevel.CLUSTER, Command.ADD));
    }

    @Override
    public void validate(ValidatorContext context) {
        ClusterCommandDTO clusterCommand = context.getCommandDTO().getClusterCommand();
        String clusterName = clusterCommand.getName();

        ClusterPO clusterPO = clusterDao.findByName(clusterName);

        if (clusterPO != null) {
            throw new ApiException(ApiExceptionEnum.CLUSTER_EXISTS, clusterName);
        }

        List<String> hostnames = clusterCommand.getHosts().stream()
                .flatMap(hostCommandDTO -> hostCommandDTO.getHostnames().stream())
                .toList();
        List<HostPO> hostPOList = hostDao.findAllByHostnames(hostnames);
        if (CollectionUtils.isNotEmpty(hostPOList)) {
            List<String> existsHostnames =
                    hostPOList.stream().map(HostPO::getHostname).toList();
            throw new ApiException(ApiExceptionEnum.HOST_ASSIGNED, String.join(",", existsHostnames));
        }
    }
}
