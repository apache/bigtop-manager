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
import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.dao.repository.ServiceDao;
import org.apache.bigtop.manager.server.command.CommandIdentifier;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.enums.CommandLevel;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.command.ServiceCommandDTO;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.apache.commons.collections4.CollectionUtils;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Component
public class RequiredServicesValidator implements CommandValidator {

    @Resource
    private ServiceDao serviceDao;

    @Override
    public List<CommandIdentifier> getCommandIdentifiers() {
        return List.of(new CommandIdentifier(CommandLevel.SERVICE, Command.ADD));
    }

    @Override
    public void validate(ValidatorContext context) {
        CommandDTO commandDTO = context.getCommandDTO();
        List<ServiceCommandDTO> serviceCommands = commandDTO.getServiceCommands();

        Long clusterId = commandDTO.getClusterId();

        List<String> serviceNames =
                serviceCommands.stream().map(ServiceCommandDTO::getServiceName).toList();
        for (ServiceCommandDTO serviceCommand : serviceCommands) {
            String serviceName = serviceCommand.getServiceName();
            ServiceDTO serviceDTO = StackUtils.getServiceDTO(serviceName);
            List<String> requiredServices = serviceDTO.getRequiredServices();
            if (CollectionUtils.isEmpty(requiredServices)) {
                continue;
            }

            List<ServicePO> services = serviceDao.findByClusterId(clusterId);
            List<ServicePO> infraServices = serviceDao.findByClusterId(0L);
            services.addAll(infraServices);

            List<String> allServices =
                    new ArrayList<>(services.stream().map(ServicePO::getName).toList());
            allServices.addAll(serviceNames);
            if (!new HashSet<>(allServices).containsAll(requiredServices)) {
                requiredServices.removeAll(allServices);
                throw new ApiException(ApiExceptionEnum.SERVICE_NOT_FOUND, String.join(",", requiredServices));
            }
        }
    }
}
