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

import org.apache.bigtop.manager.agent.executor.CommandExecutor;
import org.apache.bigtop.manager.agent.executor.CommandExecutors;
import org.apache.bigtop.manager.agent.holder.SpringContextHolder;
import org.apache.bigtop.manager.grpc.generated.CommandReply;
import org.apache.bigtop.manager.grpc.generated.CommandRequest;
import org.apache.bigtop.manager.grpc.generated.CommandType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommandServiceGrpcImplTest {

    private CommandServiceGrpcImpl commandServiceGrpc;

    @BeforeEach
    public void setUp() {
        SpringContextHolder springContextHolder = new SpringContextHolder();
        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);

        CommandExecutor mockExecutor = mock(CommandExecutor.class);
        lenient()
                .when(mockApplicationContext.getBeansOfType(CommandExecutor.class))
                .thenReturn(Map.of("mockExecutor", mockExecutor));

        springContextHolder.setApplicationContext(mockApplicationContext);

        // Initialize CommandServiceGrpcImpl
        commandServiceGrpc = new CommandServiceGrpcImpl();
    }

    @Test
    public void testExecCommand() {
        // Arrange
        CommandRequest request = CommandRequest.newBuilder()
                .setPayload("Test Payload")
                .setTaskId(1L)
                .setType(CommandType.COMPONENT)
                .build();

        CommandReply expectedReply = CommandReply.newBuilder()
                .setCode(0)
                .setResult("Success")
                .setTaskId(1L)
                .build();

        CommandExecutor mockExecutor = mock(CommandExecutor.class);
        when(CommandExecutors.getCommandExecutor(CommandType.COMPONENT)).thenReturn(mockExecutor);
        when(mockExecutor.execute(request)).thenReturn(expectedReply);

        StreamObserver<CommandReply> responseObserver = mock(StreamObserver.class);
        ArgumentCaptor<CommandReply> captor = ArgumentCaptor.forClass(CommandReply.class);

        // Act
        commandServiceGrpc.exec(request, responseObserver);

        // Assert
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();

        CommandReply actualReply = captor.getValue();
        assertEquals(expectedReply.getCode(), actualReply.getCode());
        assertEquals(expectedReply.getResult(), actualReply.getResult());
        assertEquals(expectedReply.getTaskId(), actualReply.getTaskId());
    }

    @Test
    public void testExecCommandExecutorThrowsException() {
        // Arrange
        CommandRequest request = CommandRequest.newBuilder()
                .setPayload("Test Payload")
                .setTaskId(1L)
                .setType(CommandType.COMPONENT)
                .build();

        when(CommandExecutors.getCommandExecutor(CommandType.COMPONENT))
                .thenThrow(new RuntimeException("Executor not found"));

        StreamObserver<CommandReply> responseObserver = mock(StreamObserver.class);

        // Act
        commandServiceGrpc.exec(request, responseObserver);

        // Assert
        verify(responseObserver).onError(any(RuntimeException.class));
    }

    @Test
    public void testExecCommandExecutionThrowsException() {
        // Arrange
        CommandRequest request = CommandRequest.newBuilder()
                .setPayload("Test Payload")
                .setTaskId(1L)
                .setType(CommandType.COMPONENT)
                .build();

        CommandExecutor mockExecutor = mock(CommandExecutor.class);
        when(CommandExecutors.getCommandExecutor(CommandType.COMPONENT)).thenReturn(mockExecutor);
        when(mockExecutor.execute(request)).thenThrow(new RuntimeException("Execution failed"));

        StreamObserver<CommandReply> responseObserver = mock(StreamObserver.class);

        // Act
        commandServiceGrpc.exec(request, responseObserver);

        // Assert
        verify(responseObserver).onError(any(RuntimeException.class));
    }

    @Test
    public void testExecCommandLogFileOperationFails() {
        // Arrange
        CommandRequest request = CommandRequest.newBuilder()
                .setPayload("Test Payload")
                .setTaskId(1L)
                .setType(CommandType.COMPONENT)
                .build();

        CommandReply expectedReply = CommandReply.newBuilder()
                .setCode(1)
                .setResult("File operation failed")
                .setTaskId(1L)
                .build();

        CommandExecutor mockExecutor = mock(CommandExecutor.class);
        lenient()
                .when(CommandExecutors.getCommandExecutor(CommandType.COMPONENT))
                .thenReturn(mockExecutor);
        lenient().when(mockExecutor.execute(request)).thenReturn(expectedReply);

        StreamObserver<CommandReply> responseObserver = mock(StreamObserver.class);

        // Mock truncateLogFile to throw an exception
        CommandServiceGrpcImpl commandServiceGrpcSpy = spy(commandServiceGrpc);
        doThrow(new RuntimeException("File operation failed"))
                .when(commandServiceGrpcSpy)
                .truncateLogFile(anyLong());

        // Act
        commandServiceGrpcSpy.exec(request, responseObserver);

        // Assert
        verify(responseObserver, never()).onCompleted();
        verify(responseObserver).onError(any(StatusRuntimeException.class));
    }

    @Test
    public void testExecCommandResponseObserverThrowsException() {
        // Arrange
        CommandRequest request = CommandRequest.newBuilder()
                .setPayload("Test Payload")
                .setTaskId(1L)
                .setType(CommandType.COMPONENT)
                .build();

        CommandReply expectedReply = CommandReply.newBuilder()
                .setCode(0)
                .setResult("Success")
                .setTaskId(1L)
                .build();

        CommandExecutor mockExecutor = mock(CommandExecutor.class);
        lenient()
                .when(CommandExecutors.getCommandExecutor(CommandType.COMPONENT))
                .thenReturn(mockExecutor);
        lenient().when(mockExecutor.execute(request)).thenReturn(expectedReply);

        StreamObserver<CommandReply> responseObserver = mock(StreamObserver.class);
        doThrow(new RuntimeException("Observer failed")).when(responseObserver).onNext(any());

        // Act
        commandServiceGrpc.exec(request, responseObserver);

        // Assert
        verify(responseObserver, never()).onCompleted(); // Ensure onCompleted is not called
        verify(responseObserver).onError(any(StatusRuntimeException.class));
    }
}
