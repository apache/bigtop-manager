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

import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.repository.ComponentDao;
import org.apache.bigtop.manager.dao.repository.HostDao;
import org.apache.bigtop.manager.grpc.generated.ComponentStatusReply;
import org.apache.bigtop.manager.grpc.generated.ComponentStatusRequest;
import org.apache.bigtop.manager.grpc.generated.ComponentStatusServiceGrpc;
import org.apache.bigtop.manager.server.enums.HealthyStatusEnum;
import org.apache.bigtop.manager.server.grpc.GrpcClient;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
// @Component
public class ComponentStatusTimer {

    //    @Resource
    private ComponentDao componentDao;

    //    @Resource
    private HostDao hostDao;

    //    @Async
    //    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void execute() {
        List<ComponentPO> componentPOList = componentDao.findAll();
        for (ComponentPO componentPO : componentPOList) {
            HostPO hostPO = hostDao.findById(componentPO.getHostId());
            // TODO need to build the correct request
            ComponentStatusRequest request = ComponentStatusRequest.newBuilder()
                    //                    .setServiceName(hostComponentPO.getServiceName())
                    //                    .setServiceUser(hostComponentPO.getServiceUser())
                    //                    .setComponentName(hostComponentPO.getComponentName())
                    //                    .setCommandScript(hostComponentPO.getCommandScript())
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
    }
}
