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
import org.apache.bigtop.manager.dao.po.HostComponentPO;
import org.apache.bigtop.manager.dao.repository.HostComponentDao;
import org.apache.bigtop.manager.grpc.generated.ComponentStatusReply;
import org.apache.bigtop.manager.grpc.generated.ComponentStatusRequest;
import org.apache.bigtop.manager.grpc.generated.ComponentStatusServiceGrpc;
import org.apache.bigtop.manager.server.grpc.GrpcClient;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ComponentStatusScheduler {

    @Resource
    private HostComponentDao hostComponentDao;

    @Async
    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void execute() {
        List<HostComponentPO> hostComponentPOList = hostComponentDao.findAllJoin();
        for (HostComponentPO hostComponentPO : hostComponentPOList) {
            // Only check services which should be in running
            if (!List.of(MaintainState.STARTED, MaintainState.STOPPED)
                    .contains(MaintainState.fromString(hostComponentPO.getState()))) {
                continue;
            }

            ComponentStatusRequest request = ComponentStatusRequest.newBuilder()
                    .setServiceName(hostComponentPO.getServiceName())
                    .setServiceUser(hostComponentPO.getServiceUser())
                    .setComponentName(hostComponentPO.getComponentName())
                    .setCommandScript(hostComponentPO.getCommandScript())
                    .setRoot(hostComponentPO.getRoot())
                    .setStackName(hostComponentPO.getStackName())
                    .setStackVersion(hostComponentPO.getStackVersion())
                    .build();
            ComponentStatusServiceGrpc.ComponentStatusServiceBlockingStub blockingStub = GrpcClient.getBlockingStub(
                    hostComponentPO.getHostname(), ComponentStatusServiceGrpc.ComponentStatusServiceBlockingStub.class);
            ComponentStatusReply reply = blockingStub.getComponentStatus(request);

            // Status 0 means the service is running
            if (reply.getStatus() == 0
                    && MaintainState.fromString(hostComponentPO.getState()) == MaintainState.STOPPED) {
                hostComponentPO.setState(MaintainState.STARTED.getName());
                hostComponentDao.partialUpdateById(hostComponentPO);
            }

            if (reply.getStatus() != 0
                    && MaintainState.fromString(hostComponentPO.getState()) == MaintainState.STARTED) {
                hostComponentPO.setState(MaintainState.STOPPED.getName());
                hostComponentDao.partialUpdateById(hostComponentPO);
            }
        }
    }
}
