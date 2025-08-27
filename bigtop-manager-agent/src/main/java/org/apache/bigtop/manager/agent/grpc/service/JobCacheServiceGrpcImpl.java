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

import org.apache.bigtop.manager.common.constants.MessageConstants;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.common.utils.ProjectPathUtils;
import org.apache.bigtop.manager.grpc.generated.JobCacheReply;
import org.apache.bigtop.manager.grpc.generated.JobCacheRequest;
import org.apache.bigtop.manager.grpc.generated.JobCacheServiceGrpc;
import org.apache.bigtop.manager.grpc.payload.JobCachePayload;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.bigtop.manager.common.constants.CacheFiles.CLUSTER_INFO;
import static org.apache.bigtop.manager.common.constants.CacheFiles.COMPONENTS_INFO;
import static org.apache.bigtop.manager.common.constants.CacheFiles.CONFIGURATIONS_INFO;
import static org.apache.bigtop.manager.common.constants.CacheFiles.HOSTS_INFO;
import static org.apache.bigtop.manager.common.constants.CacheFiles.REPOS_INFO;
import static org.apache.bigtop.manager.common.constants.CacheFiles.USERS_INFO;

@Slf4j
@GrpcService
public class JobCacheServiceGrpcImpl extends JobCacheServiceGrpc.JobCacheServiceImplBase {

    @Override
    public void save(JobCacheRequest request, StreamObserver<JobCacheReply> responseObserver) {
        try {
            JobCachePayload payload = JsonUtils.readFromString(request.getPayload(), JobCachePayload.class);
            String cacheDir = ProjectPathUtils.getAgentCachePath() + File.separator + payload.getClusterId();
            Path p = Paths.get(cacheDir);
            if (!Files.exists(p)) {
                Files.createDirectories(p);
            }

            String dir = p.getParent().toFile().getAbsolutePath();
            JsonUtils.writeToFile(dir + "/current", payload.getCurrentClusterId());

            JsonUtils.writeToFile(cacheDir + CONFIGURATIONS_INFO, payload.getConfigurations());
            JsonUtils.writeToFile(cacheDir + COMPONENTS_INFO, payload.getComponentHosts());
            JsonUtils.writeToFile(cacheDir + USERS_INFO, payload.getUserInfo());
            JsonUtils.writeToFile(cacheDir + REPOS_INFO, payload.getRepoInfo());
            JsonUtils.writeToFile(cacheDir + CLUSTER_INFO, payload.getClusterInfo());
            JsonUtils.writeToFile(cacheDir + HOSTS_INFO, payload.getHosts());

            JobCacheReply reply = JobCacheReply.newBuilder()
                    .setCode(MessageConstants.SUCCESS_CODE)
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
