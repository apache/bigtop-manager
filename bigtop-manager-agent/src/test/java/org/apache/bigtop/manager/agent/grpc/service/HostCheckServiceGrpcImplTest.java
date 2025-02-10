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

import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.common.utils.Environments;
import org.apache.bigtop.manager.common.utils.os.TimeSyncDetection;
import org.apache.bigtop.manager.grpc.generated.HostCheckReply;
import org.apache.bigtop.manager.grpc.generated.HostCheckRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.grpc.stub.StreamObserver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class HostCheckServiceGrpcImplTest {

    @Mock
    private StreamObserver<HostCheckReply> responseObserver;

    private HostCheckServiceGrpcImpl hostCheckServiceGrpcImpl;

    @BeforeEach
    public void setUp() {
        hostCheckServiceGrpcImpl = new HostCheckServiceGrpcImpl();
    }

    @Test
    public void testCheckInDevMode() {
        try (var environmentsMockedStatic = mockStatic(Environments.class)) {
            // Mock environment in development mode
            environmentsMockedStatic.when(Environments::isDevMode).thenReturn(true);

            // Call check method
            hostCheckServiceGrpcImpl.check(mock(HostCheckRequest.class), responseObserver);

            // Verify responseObserver is correctly called
            then(responseObserver).should(times(1)).onNext(any());
            then(responseObserver).should(times(1)).onCompleted();
            then(responseObserver).should(never()).onError(any());
        }
    }

    @Test
    public void testCheckInNonDevModeWithSuccess() {
        try (var environmentsMockedStatic = mockStatic(Environments.class);
                var timeSyncDetectionMockedStatic = mockStatic(TimeSyncDetection.class)) {
            // Mock environment not in development mode
            environmentsMockedStatic.when(Environments::isDevMode).thenReturn(false);
            // Mock successful time synchronization check
            timeSyncDetectionMockedStatic
                    .when(TimeSyncDetection::checkTimeSync)
                    .thenReturn(ShellResult.success("test"));

            // Call check method
            hostCheckServiceGrpcImpl.check(mock(HostCheckRequest.class), responseObserver);

            // Verify responseObserver is correctly called
            then(responseObserver).should(times(1)).onNext(any());
            then(responseObserver).should(times(1)).onCompleted();
            then(responseObserver).should(never()).onError(any());
        }
    }

    @Test
    public void testCheckInNonDevModeWithError() {
        try (var environmentsMockedStatic = mockStatic(Environments.class);
                var timeSyncDetectionMockedStatic = mockStatic(TimeSyncDetection.class)) {
            // Mock environment not in development mode
            environmentsMockedStatic.when(Environments::isDevMode).thenReturn(false);
            // Mock failed time synchronization check
            timeSyncDetectionMockedStatic.when(TimeSyncDetection::checkTimeSync).thenReturn(ShellResult.fail("test"));

            // Call check method
            hostCheckServiceGrpcImpl.check(mock(HostCheckRequest.class), responseObserver);

            // Verify responseObserver is correctly called
            then(responseObserver).should(times(1)).onNext(any());
            then(responseObserver).should(times(1)).onCompleted();
            then(responseObserver).should(never()).onError(any());
        }
    }

    @Test
    public void testCheckWithError() {
        try (var environmentsMockedStatic = mockStatic(Environments.class);
                var timeSyncDetectionMockedStatic = mockStatic(TimeSyncDetection.class)) {
            // Mock environment not in development mode
            environmentsMockedStatic.when(Environments::isDevMode).thenReturn(false);
            // Mock a runtime exception during check execution
            timeSyncDetectionMockedStatic
                    .when(TimeSyncDetection::checkTimeSync)
                    .thenThrow(new RuntimeException("Test Simulated Error"));

            // Call check method
            hostCheckServiceGrpcImpl.check(mock(HostCheckRequest.class), responseObserver);

            // Verify responseObserver is correctly called
            then(responseObserver).should(never()).onNext(any());
            then(responseObserver).should(never()).onCompleted();
            then(responseObserver).should(times(1)).onError(any());
        }
    }
}
