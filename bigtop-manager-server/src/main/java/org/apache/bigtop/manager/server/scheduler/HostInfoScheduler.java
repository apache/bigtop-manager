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

import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.repository.HostDao;
import org.apache.bigtop.manager.grpc.generated.HostInfoReply;
import org.apache.bigtop.manager.grpc.generated.HostInfoRequest;
import org.apache.bigtop.manager.grpc.generated.HostInfoServiceGrpc;
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
public class HostInfoScheduler {

    @Resource
    private HostDao hostDao;

    @Async
    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void execute() {
        List<HostPO> hostPOList = hostDao.findAll();
        for (HostPO hostPO : hostPOList) {
            getHostInfo(hostPO);
        }
    }

    private void getHostInfo(HostPO hostPO) {
        String hostname = hostPO.getHostname();
        Integer grpcPort = hostPO.getGrpcPort();
        try {
            HostInfoServiceGrpc.HostInfoServiceBlockingStub stub = GrpcClient.getBlockingStub(
                    hostname, grpcPort, HostInfoServiceGrpc.HostInfoServiceBlockingStub.class);
            HostInfoReply reply = stub.getHostInfo(HostInfoRequest.newBuilder().build());

            hostPO.setArch(reply.getArch());
            hostPO.setAvailableProcessors(reply.getAvailableProcessors());
            hostPO.setIpv4(reply.getIpv4());
            hostPO.setIpv6(reply.getIpv6());
            hostPO.setOs(reply.getOs());
            hostPO.setFreeMemorySize(reply.getFreeMemorySize());
            hostPO.setTotalMemorySize(reply.getTotalMemorySize());
            hostPO.setFreeDisk(reply.getFreeDisk());
            hostPO.setTotalDisk(reply.getTotalDisk());
        } catch (Exception e) {
            log.error("Error getting host info for {}", hostname, e);
        }

        hostDao.partialUpdateById(hostPO);
    }
}
