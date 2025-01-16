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
package org.apache.bigtop.manager.agent.grpc.service;

import org.apache.bigtop.manager.common.utils.os.OSDetection;
import org.apache.bigtop.manager.grpc.generated.HostInfoReply;
import org.apache.bigtop.manager.grpc.generated.HostInfoRequest;
import org.apache.bigtop.manager.grpc.generated.HostInfoServiceGrpc;

import com.sun.management.OperatingSystemMXBean;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;

@Slf4j
@GrpcService
public class HostInfoServiceGrpcImpl extends HostInfoServiceGrpc.HostInfoServiceImplBase {

    @Override
    public void getHostInfo(HostInfoRequest request, StreamObserver<HostInfoReply> responseObserver) {
        HostInfoReply.Builder builder = HostInfoReply.newBuilder();

        try {
            InetAddress addr = InetAddress.getLocalHost();
            builder.setHostname(addr.getHostName());
            builder.setIpv4(addr.getHostAddress());

            OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            builder.setOs(OSDetection.getOS());
            builder.setVersion(OSDetection.getVersion());
            builder.setArch(OSDetection.getArch());
            builder.setAvailableProcessors(osmxb.getAvailableProcessors());
            builder.setProcessCpuTime(osmxb.getProcessCpuTime());
            builder.setTotalMemorySize(osmxb.getTotalMemorySize());
            builder.setFreeMemorySize(osmxb.getFreeMemorySize());
            builder.setTotalSwapSpaceSize(osmxb.getTotalSwapSpaceSize());
            builder.setFreeSwapSpaceSize(osmxb.getFreeSwapSpaceSize());
            builder.setCommittedVirtualMemorySize(osmxb.getCommittedVirtualMemorySize());

            builder.setCpuLoad(String.valueOf(osmxb.getCpuLoad()));
            builder.setProcessCpuLoad(String.valueOf(osmxb.getProcessCpuLoad()));
            builder.setSystemLoadAverage(String.valueOf(osmxb.getSystemLoadAverage()));

            builder.setFreeDisk(OSDetection.freeDisk());
            builder.setTotalDisk(OSDetection.totalDisk());
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting host info", e);
            Status status = Status.UNKNOWN.withDescription(e.getMessage());
            responseObserver.onError(status.asRuntimeException());
        }
    }
}
