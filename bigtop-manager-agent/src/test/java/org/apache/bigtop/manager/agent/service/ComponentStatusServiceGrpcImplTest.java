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
package org.apache.bigtop.manager.agent.service;

import org.apache.bigtop.manager.common.message.entity.payload.CommandPayload;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.grpc.generated.ComponentStatusReply;
import org.apache.bigtop.manager.grpc.generated.ComponentStatusRequest;
import org.apache.bigtop.manager.stack.core.executor.StackExecutor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ComponentStatusServiceGrpcImplTest {

    private final ComponentStatusServiceGrpcImpl service = new ComponentStatusServiceGrpcImpl();

    @Test
    public void testGetComponentStatusSuccess() {
        try (MockedStatic<StackExecutor> mockedStatic = mockStatic(StackExecutor.class)) {
            // Arrange
            ComponentStatusRequest request = ComponentStatusRequest.newBuilder()
                    .setStackName("TestStack")
                    .setStackVersion("1.0")
                    .setServiceName("TestService")
                    .setServiceUser("TestUser")
                    .setComponentName("TestComponent")
                    .build();

            ShellResult shellResult = new ShellResult();
            shellResult.setExitCode(0);

            // Mock StackExecutor
            mockedStatic
                    .when(() -> StackExecutor.execute(any(CommandPayload.class)))
                    .thenReturn(shellResult);

            StreamObserver<ComponentStatusReply> responseObserver = mock(StreamObserver.class);
            ArgumentCaptor<ComponentStatusReply> captor = ArgumentCaptor.forClass(ComponentStatusReply.class);

            // Act
            service.getComponentStatus(request, responseObserver);

            // Assert
            verify(responseObserver).onNext(captor.capture());
            verify(responseObserver).onCompleted();

            ComponentStatusReply reply = captor.getValue();
            assertEquals(0, reply.getStatus());
        }
    }

    @Test
    public void testGetComponentStatusExecutionFailure() {
        try (MockedStatic<StackExecutor> mockedStatic = mockStatic(StackExecutor.class)) {
            // Arrange
            ComponentStatusRequest request = ComponentStatusRequest.newBuilder()
                    .setStackName("TestStack")
                    .setStackVersion("1.0")
                    .setServiceName("TestService")
                    .setServiceUser("TestUser")
                    .setComponentName("TestComponent")
                    .build();

            // Mock StackExecutor to throw an exception
            mockedStatic
                    .when(() -> StackExecutor.execute(any(CommandPayload.class)))
                    .thenThrow(new RuntimeException("Execution failed"));

            StreamObserver<ComponentStatusReply> responseObserver = mock(StreamObserver.class);

            // Act
            service.getComponentStatus(request, responseObserver);

            // Assert
            verify(responseObserver).onError(any(StatusRuntimeException.class));

            // Capture the exception and verify its message
            ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
            verify(responseObserver).onError(captor.capture());

            // Get the captured exception
            Throwable actualException = captor.getValue();

            // Check if it is an instance of StatusRuntimeException
            assertInstanceOf(StatusRuntimeException.class, actualException, "Expected StatusRuntimeException");

            StatusRuntimeException statusRuntimeException = (StatusRuntimeException) actualException;

            // Check that the status code is UNKNOWN and the message contains the expected error message
            assertEquals(
                    Status.UNKNOWN.getCode(), statusRuntimeException.getStatus().getCode());
            assertTrue(statusRuntimeException.getMessage().contains("Execution failed"));
        }
    }
}
