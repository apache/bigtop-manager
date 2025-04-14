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
package org.apache.bigtop.manager.server.timer;

import org.apache.bigtop.manager.common.constants.ComponentCategories;
import org.apache.bigtop.manager.common.utils.CaseUtils;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.dao.repository.ComponentDao;
import org.apache.bigtop.manager.dao.repository.HostDao;
import org.apache.bigtop.manager.dao.repository.ServiceDao;
import org.apache.bigtop.manager.grpc.generated.ComponentStatusReply;
import org.apache.bigtop.manager.grpc.generated.ComponentStatusRequest;
import org.apache.bigtop.manager.grpc.generated.ComponentStatusServiceGrpc;
import org.apache.bigtop.manager.server.enums.HealthyStatusEnum;
import org.apache.bigtop.manager.server.grpc.GrpcClient;
import org.apache.bigtop.manager.server.model.dto.ComponentDTO;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ComponentStatusTimer {

    @Resource
    private ServiceDao serviceDao;

    @Resource
    private ComponentDao componentDao;

    @Resource
    private HostDao hostDao;

    @Async
    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void execute() {
        List<ComponentPO> componentPOList = componentDao.findAll();
        for (ComponentPO componentPO : componentPOList) {
            ComponentDTO componentDTO = StackUtils.getComponentDTO(componentPO.getName());
            String category = componentDTO.getCategory();
            if (HealthyStatusEnum.fromCode(componentPO.getStatus()) == HealthyStatusEnum.UNKNOWN
                    || category.equals(ComponentCategories.CLIENT)) {
                continue;
            }

            ComponentPO componentDetailsPO = componentDao.findDetailsById(componentPO.getId());
            HostPO hostPO = hostDao.findById(componentPO.getHostId());
            ComponentStatusRequest request = ComponentStatusRequest.newBuilder()
                    .setStackName(
                            CaseUtils.toLowerCase(componentDetailsPO.getStack().split("-")[0]))
                    .setStackVersion(componentDetailsPO.getStack().split("-")[1])
                    .setServiceName(componentDetailsPO.getServiceName())
                    .setServiceUser(componentDetailsPO.getServiceUser())
                    .setComponentName(componentDetailsPO.getName())
                    .build();
            ComponentStatusServiceGrpc.ComponentStatusServiceBlockingStub blockingStub = GrpcClient.getBlockingStub(
                    hostPO.getHostname(),
                    hostPO.getGrpcPort(),
                    ComponentStatusServiceGrpc.ComponentStatusServiceBlockingStub.class);
            ComponentStatusReply reply = blockingStub.getComponentStatus(request);

            // Status 0 means the service is running
            if (reply.getStatus() == 0) {
                componentPO.setStatus(HealthyStatusEnum.HEALTHY.getCode());
            } else {
                componentPO.setStatus(HealthyStatusEnum.UNHEALTHY.getCode());
            }
        }

        componentDao.partialUpdateByIds(componentPOList);

        // Update services
        Map<Long, List<ComponentPO>> componentPOMap =
                componentPOList.stream().collect(Collectors.groupingBy(ComponentPO::getServiceId));
        for (Map.Entry<Long, List<ComponentPO>> entry : componentPOMap.entrySet()) {
            Long serviceId = entry.getKey();
            List<ComponentPO> components = entry.getValue();
            ServicePO servicePO = serviceDao.findById(serviceId);
            boolean hasUnknownComponent = components.stream()
                    .anyMatch(component -> Objects.equals(component.getStatus(), HealthyStatusEnum.UNKNOWN.getCode()));
            if (hasUnknownComponent) {
                continue;
            }

            List<ComponentPO> healthyComponents = components.stream()
                    .filter(component -> Objects.equals(component.getStatus(), HealthyStatusEnum.HEALTHY.getCode()))
                    .toList();

            if (healthyComponents.size() == components.size()) {
                servicePO.setStatus(HealthyStatusEnum.HEALTHY.getCode());
                servicePO.setRestartFlag(false);
            } else {
                servicePO.setStatus(HealthyStatusEnum.UNHEALTHY.getCode());
                servicePO.setRestartFlag(true);
            }

            serviceDao.partialUpdateById(servicePO);
        }
    }
}
