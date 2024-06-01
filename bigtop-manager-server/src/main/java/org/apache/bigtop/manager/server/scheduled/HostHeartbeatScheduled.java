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
package org.apache.bigtop.manager.server.scheduled;

import org.apache.bigtop.manager.common.message.entity.HeartbeatMessage;
import org.apache.bigtop.manager.common.message.entity.pojo.HostInfo;
import org.apache.bigtop.manager.dao.entity.Host;
import org.apache.bigtop.manager.dao.repository.HostRepository;
import org.apache.bigtop.manager.server.ws.ServerWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HostHeartbeatScheduled {

    @Resource
    private HostRepository hostRepository;

    @Async
    @Scheduled(cron = "0/30 * *  * * ? ")
    public void execute() {
        List<Host> hosts = hostRepository.findAll();
        Map<String, Host> hostMap = hosts.stream().collect(Collectors.toMap(Host::getHostname, host -> host));
        for (Map.Entry<String, HeartbeatMessage> entry : ServerWebSocketHandler.HEARTBEAT_MESSAGE_MAP.entrySet()) {
            String hostname = entry.getKey();
            HeartbeatMessage heartbeatMessage = entry.getValue();
            HostInfo hostInfo = heartbeatMessage.getHostInfo();
            if (hostMap.containsKey(hostname)) {
                Host host = hostMap.get(hostname);
                if (hostInfo != null) {
                    host.setArch(hostInfo.getArch());
                    host.setAvailableProcessors(hostInfo.getAvailableProcessors());
                    host.setIpv4(hostInfo.getIpv4());
                    host.setIpv6(hostInfo.getIpv6());
                    host.setOs(hostInfo.getOs());
                    host.setFreeMemorySize(hostInfo.getFreeMemorySize());
                    host.setTotalMemorySize(hostInfo.getTotalMemorySize());
                    host.setFreeDisk(hostInfo.getFreeDisk());
                    host.setTotalDisk(hostInfo.getTotalDisk());
                    // heartbeat HEALTHY
                }
                hostRepository.save(host);
                hostMap.remove(hostname);
            }
        }

        if (!hostMap.isEmpty()) {
            for (Map.Entry<String, Host> entry : hostMap.entrySet()) {
                Host host = entry.getValue();
                // heartbeat LOST
                hostRepository.save(host);
            }
        }

    }

}
