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
package org.apache.bigtop.manager.server.scheduler;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.bigtop.manager.common.enums.MaintainState;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.po.HostComponentPO;
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.dao.po.StackPO;
import org.apache.bigtop.manager.dao.repository.HostComponentRepository;
import org.apache.bigtop.manager.grpc.generated.ComponentStatusReply;
import org.apache.bigtop.manager.grpc.generated.ComponentStatusRequest;
import org.apache.bigtop.manager.grpc.generated.ComponentStatusServiceGrpc;
import org.apache.bigtop.manager.server.grpc.GrpcClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ComponentStatusScheduler {

    @Resource
    private HostComponentRepository hostComponentRepository;

    @Async
    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void execute() {
        List<HostComponentPO> hostComponentPOList = hostComponentRepository.findAll();
        for (HostComponentPO hostComponentPO : hostComponentPOList) {
            // Only check services which should be in running
            if (!List.of(MaintainState.STARTED, MaintainState.STOPPED).contains(hostComponentPO.getState())) {
                continue;
            }

            HostPO hostPO = hostComponentPO.getHostPO();
            ComponentPO componentPO = hostComponentPO.getComponentPO();
            ServicePO servicePO = componentPO.getServicePO();
            ClusterPO clusterPO = hostPO.getClusterPO();
            StackPO stackPO = clusterPO.getStackPO();

            ComponentStatusRequest request = ComponentStatusRequest.newBuilder()
                    .setServiceName(servicePO.getServiceName())
                    .setComponentName(componentPO.getComponentName())
                    .setCommandScript(componentPO.getCommandScript())
                    .setRoot(clusterPO.getRoot())
                    .setStackName(stackPO.getStackName())
                    .setStackVersion(stackPO.getStackVersion())
                    .build();
            ComponentStatusServiceGrpc.ComponentStatusServiceBlockingStub blockingStub = GrpcClient.getBlockingStub(
                    hostPO.getHostname(), ComponentStatusServiceGrpc.ComponentStatusServiceBlockingStub.class);
            ComponentStatusReply reply = blockingStub.getComponentStatus(request);

            // Status 0 means the service is running
            if (reply.getStatus() == 0 && hostComponentPO.getState() == MaintainState.STOPPED) {
                hostComponentPO.setState(MaintainState.STARTED);
                hostComponentRepository.save(hostComponentPO);
            }

            if (reply.getStatus() != 0 && hostComponentPO.getState() == MaintainState.STARTED) {
                hostComponentPO.setState(MaintainState.STOPPED);
                hostComponentRepository.save(hostComponentPO);
            }
        }
    }
}
