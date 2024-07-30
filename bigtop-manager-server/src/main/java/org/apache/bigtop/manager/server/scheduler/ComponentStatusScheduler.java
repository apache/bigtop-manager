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

import org.apache.bigtop.manager.common.enums.MaintainState;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.po.Host;
import org.apache.bigtop.manager.dao.po.HostComponent;
import org.apache.bigtop.manager.dao.po.Service;
import org.apache.bigtop.manager.dao.po.Stack;
import org.apache.bigtop.manager.dao.repository.HostComponentRepository;
import org.apache.bigtop.manager.grpc.generated.ComponentStatusReply;
import org.apache.bigtop.manager.grpc.generated.ComponentStatusRequest;
import org.apache.bigtop.manager.grpc.generated.ComponentStatusServiceGrpc;
import org.apache.bigtop.manager.server.grpc.GrpcClient;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@org.springframework.stereotype.Component
public class ComponentStatusScheduler {

    @Resource
    private HostComponentRepository hostComponentRepository;

    @Async
    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void execute() {
        List<HostComponent> hostComponentList = hostComponentRepository.findAll();
        for (HostComponent hostComponent : hostComponentList) {
            // Only check services which should be in running
            if (!List.of(MaintainState.STARTED, MaintainState.STOPPED).contains(hostComponent.getState())) {
                continue;
            }

            Host host = hostComponent.getHost();
            ComponentPO componentPO = hostComponent.getComponentPO();
            Service service = componentPO.getService();
            ClusterPO clusterPO = host.getClusterPO();
            Stack stack = clusterPO.getStack();

            ComponentStatusRequest request = ComponentStatusRequest.newBuilder()
                    .setServiceName(service.getServiceName())
                    .setComponentName(componentPO.getComponentName())
                    .setCommandScript(componentPO.getCommandScript())
                    .setRoot(clusterPO.getRoot())
                    .setStackName(stack.getStackName())
                    .setStackVersion(stack.getStackVersion())
                    .build();
            ComponentStatusServiceGrpc.ComponentStatusServiceBlockingStub blockingStub = GrpcClient.getBlockingStub(
                    host.getHostname(), ComponentStatusServiceGrpc.ComponentStatusServiceBlockingStub.class);
            ComponentStatusReply reply = blockingStub.getComponentStatus(request);

            // Status 0 means the service is running
            if (reply.getStatus() == 0 && hostComponent.getState() == MaintainState.STOPPED) {
                hostComponent.setState(MaintainState.STARTED);
                hostComponentRepository.save(hostComponent);
            }

            if (reply.getStatus() != 0 && hostComponent.getState() == MaintainState.STARTED) {
                hostComponent.setState(MaintainState.STOPPED);
                hostComponentRepository.save(hostComponent);
            }
        }
    }
}
