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

import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.constants.MessageConstants;
import org.apache.bigtop.manager.common.utils.Environments;
import org.apache.bigtop.manager.grpc.generated.SetupJdkReply;
import org.apache.bigtop.manager.grpc.generated.SetupJdkRequest;
import org.apache.bigtop.manager.grpc.generated.SetupJdkServiceGrpc;
import org.apache.bigtop.manager.grpc.pojo.ClusterInfo;
import org.apache.bigtop.manager.grpc.pojo.PackageInfo;
import org.apache.bigtop.manager.grpc.pojo.RepoInfo;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;
import org.apache.bigtop.manager.stack.core.utils.TarballUtils;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxFileUtils;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
public class SetupJdkServiceGrpcImpl extends SetupJdkServiceGrpc.SetupJdkServiceImplBase {

    @Override
    public void setup(SetupJdkRequest request, StreamObserver<SetupJdkReply> responseObserver) {
        try {
            if (Environments.isDevMode()) {
                SetupJdkReply reply = SetupJdkReply.newBuilder()
                        .setCode(MessageConstants.SUCCESS_CODE)
                        .build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
                return;
            }

            log.info("Setting up cluster jdk...");
            ClusterInfo clusterInfo = LocalSettings.cluster();
            String dependenciesHome = clusterInfo.getRootDir() + "/dependencies";
            String user = System.getProperty("user.name");
            LinuxFileUtils.createDirectories(dependenciesHome, user, user, Constants.PERMISSION_755, true);

            String jdkHome = dependenciesHome + "/jdk";
            RepoInfo repoInfo = LocalSettings.repo("jdk8");
            PackageInfo packageInfo = new PackageInfo();
            packageInfo.setName(repoInfo.getPkgName());
            packageInfo.setChecksum(repoInfo.getChecksum());
            TarballUtils.installPackage(repoInfo.getBaseUrl(), dependenciesHome, jdkHome, packageInfo, 1);
            LinuxFileUtils.createDirectories(jdkHome, user, user, Constants.PERMISSION_755, true);

            SetupJdkReply reply = SetupJdkReply.newBuilder()
                    .setCode(MessageConstants.SUCCESS_CODE)
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error setting up jdk", e);
            responseObserver.onError(e);
        }
    }
}
