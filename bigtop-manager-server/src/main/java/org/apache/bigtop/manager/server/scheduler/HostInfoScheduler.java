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
import org.apache.bigtop.manager.dao.entity.Host;
import org.apache.bigtop.manager.dao.repository.HostRepository;
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

@Slf4j
@Component
public class HostInfoScheduler {

    @Resource
    private HostRepository hostRepository;

    @Async
    @Scheduled(cron = "0/30 * *  * * ? ")
    public void execute() {
        List<Host> hosts = hostRepository.findAll();
        for (Host host : hosts) {
            getHostInfo(host);
        }
    }

    private void getHostInfo(Host host) {
        String hostname = host.getHostname();
        try {
            if (!GrpcClient.isChannelAlive(hostname)) {
                GrpcClient.createChannel(hostname);
            }

            HostInfoServiceGrpc.HostInfoServiceBlockingStub stub =
                    GrpcClient.getBlockingStub(hostname, HostInfoServiceGrpc.HostInfoServiceBlockingStub.class);
            HostInfoReply reply = stub.getHostInfo(HostInfoRequest.newBuilder().build());

            host.setArch(reply.getArch());
            host.setAvailableProcessors(reply.getAvailableProcessors());
            host.setIpv4(reply.getIpv4());
            host.setIpv6(reply.getIpv6());
            host.setOs(reply.getOs());
            host.setFreeMemorySize(reply.getFreeMemorySize());
            host.setTotalMemorySize(reply.getTotalMemorySize());
            host.setFreeDisk(reply.getFreeDisk());
            host.setTotalDisk(reply.getTotalDisk());

            host.setState(MaintainState.STARTED);
        } catch (Exception e) {
            log.error("Error getting host info", e);
            host.setState(MaintainState.STOPPED);
        }

        hostRepository.save(host);
    }
}
