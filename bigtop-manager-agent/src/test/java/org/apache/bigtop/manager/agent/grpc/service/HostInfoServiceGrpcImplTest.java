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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sun.management.OperatingSystemMXBean;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HostInfoServiceGrpcImplTest {

    @InjectMocks
    private HostInfoServiceGrpcImpl service;

    @Test
    public void testGetHostInfoSuccess() {
        // Arrange
        HostInfoRequest request = HostInfoRequest.newBuilder().build();

        // Mock InetAddress
        InetAddress mockInetAddress = mock(InetAddress.class);
        lenient().when(mockInetAddress.getHostName()).thenReturn("localhost");
        lenient().when(mockInetAddress.getHostAddress()).thenReturn("192.168.0.100");

        // Mock static methods of OSDetection using MockedStatic
        try (MockedStatic<OSDetection> mockedStatic = mockStatic(OSDetection.class);
                MockedStatic<ManagementFactory> mockManagementFactory = mockStatic(ManagementFactory.class)) {
            long oneGBInBytes = 1024L * 1024L * 1024L;
            long freeDiskInBytes = 100L * oneGBInBytes;
            long totalDiskInBytes = 500L * oneGBInBytes;
            // Mock static methods for disk size
            mockedStatic.when(OSDetection::freeDisk).thenReturn(freeDiskInBytes);
            mockedStatic.when(OSDetection::totalDisk).thenReturn(totalDiskInBytes);

            // Mock OSDetection static methods for OS, version, and arch
            mockedStatic.when(OSDetection::getOS).thenReturn("Linux");
            mockedStatic.when(OSDetection::getVersion).thenReturn("1.0");
            mockedStatic.when(OSDetection::getArch).thenReturn("x86_64");

            // Mock OperatingSystemMXBean methods
            OperatingSystemMXBean mockOsBean = mock(OperatingSystemMXBean.class);
            mockManagementFactory
                    .when(ManagementFactory::getOperatingSystemMXBean)
                    .thenReturn(mockOsBean);
            when(mockOsBean.getAvailableProcessors()).thenReturn(4);
            when(mockOsBean.getProcessCpuTime()).thenReturn(100L);
            when(mockOsBean.getTotalMemorySize()).thenReturn(1024L * 1024 * 1024);
            when(mockOsBean.getFreeMemorySize()).thenReturn(512L * 1024 * 1024);
            when(mockOsBean.getTotalSwapSpaceSize()).thenReturn(2048L * 1024 * 1024);
            when(mockOsBean.getFreeSwapSpaceSize()).thenReturn(1024L * 1024 * 1024);
            when(mockOsBean.getCommittedVirtualMemorySize()).thenReturn(4096L * 1024 * 1024);
            when(mockOsBean.getCpuLoad()).thenReturn(0.75);
            when(mockOsBean.getProcessCpuLoad()).thenReturn(0.50);
            when(mockOsBean.getSystemLoadAverage()).thenReturn(1.5);

            StreamObserver<HostInfoReply> mockResponseObserver = mock(StreamObserver.class);

            // Act
            service.getHostInfo(request, mockResponseObserver);

            // Assert
            verify(mockResponseObserver).onNext(any(HostInfoReply.class));
            verify(mockResponseObserver).onCompleted();
        }
    }

    @Test
    public void testGetHostInfoFailure() {
        // Arrange
        HostInfoRequest request = HostInfoRequest.newBuilder().build();

        try (MockedStatic<InetAddress> mockInetAddress = mockStatic(InetAddress.class)) {
            // Simulate an exception during execution
            mockInetAddress.when(InetAddress::getLocalHost).thenThrow(new RuntimeException("Network error"));

            StreamObserver<HostInfoReply> mockResponseObserver = mock(StreamObserver.class);

            // Act
            service.getHostInfo(request, mockResponseObserver);

            // Assert
            verify(mockResponseObserver).onError(any(StatusRuntimeException.class));
        }
    }
}
