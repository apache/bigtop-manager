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

import org.apache.bigtop.manager.common.utils.ProjectPathUtils;
import org.apache.bigtop.manager.grpc.generated.JobCacheReply;
import org.apache.bigtop.manager.grpc.generated.JobCacheRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import io.grpc.stub.StreamObserver;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class JobCacheServiceGrpcImplTest {

    private JobCacheServiceGrpcImpl jobCacheServiceGrpcImpl;

    @Mock
    private StreamObserver<JobCacheReply> responseObserver;

    private Path cacheDirPath;

    @BeforeEach
    public void setUp() throws Exception {
        // Create a real temporary directory for testing
        cacheDirPath = Files.createTempDirectory("test-cache-dir");
        jobCacheServiceGrpcImpl = new JobCacheServiceGrpcImpl();
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Delete the test directory after each test
        if (cacheDirPath != null) {
            Files.walk(cacheDirPath)
                    .sorted((p1, p2) -> -p1.compareTo(p2))
                    .map(Path::toFile)
                    .forEach(java.io.File::delete);
        }
    }

    @Test
    public void testSaveSuccess() {
        // Mock the static behavior of ProjectPathUtils.getAgentCachePath method
        try (MockedStatic<ProjectPathUtils> mockedStatic = mockStatic(ProjectPathUtils.class)) {
            mockedStatic.when(ProjectPathUtils::getAgentCachePath).thenReturn(cacheDirPath.toString());

            // Construct JobCacheRequest
            String payloadJson = "{\"configurations\": {\"configKey\": {\"subKey\": \"subValue\"}}}";
            JobCacheRequest request = JobCacheRequest.newBuilder()
                    .setJobId(123L)
                    .setPayload(payloadJson)
                    .build();

            // Execute the save method
            jobCacheServiceGrpcImpl.save(request, responseObserver);

            // Verify that JsonUtils.writeToFile method was called correctly
            verify(responseObserver).onNext(any(JobCacheReply.class));
            verify(responseObserver).onCompleted();
        }
    }

    @Test
    public void testSaveDirectoryCreationFailure() {
        // Mock the static behavior of ProjectPathUtils.getAgentCachePath method
        try (MockedStatic<ProjectPathUtils> mockedStatic = mockStatic(ProjectPathUtils.class)) {
            mockedStatic.when(ProjectPathUtils::getAgentCachePath).thenReturn(cacheDirPath.toString());

            // Mock Files.createDirectories to throw an exception
            try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
                mockedFiles
                        .when(() -> Files.createDirectories(any(Path.class)))
                        .thenThrow(new RuntimeException("Directory creation failed"));

                // Construct JobCacheRequest
                String payloadJson = "{\"configurations\": {\"configKey\": {\"subKey\": \"subValue\"}}}";
                JobCacheRequest request = JobCacheRequest.newBuilder()
                        .setJobId(123L)
                        .setPayload(payloadJson)
                        .build();

                // Execute the save method, expecting onError to be called
                jobCacheServiceGrpcImpl.save(request, responseObserver);

                // Verify that onError was called with the expected exception
                verify(responseObserver).onError(any(RuntimeException.class));
            }
        }
    }
}
