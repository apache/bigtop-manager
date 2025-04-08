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
import org.apache.bigtop.manager.common.utils.os.OSDetection;
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

import java.text.MessageFormat;

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
            String arch = OSDetection.getArch();
            String pkgName = getPkgName(arch);
            String checksum = getChecksum(arch);

            PackageInfo packageInfo = new PackageInfo();
            packageInfo.setName(pkgName);
            packageInfo.setChecksum(checksum);

            ClusterInfo clusterInfo = LocalSettings.cluster();
            RepoInfo repoInfo = LocalSettings.repos().stream()
                    .filter(r -> arch.equals(r.getArch()) && r.getType() == 2)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "Cannot find repo for os: [" + OSDetection.getOS() + "] and arch: [" + arch + "]"));
            String toolsHome = clusterInfo.getRootDir() + "/tools";
            String user = System.getProperty("user.name");
            LinuxFileUtils.createDirectories(toolsHome, user, user, Constants.PERMISSION_755, true);

            String jdkHome = toolsHome + "/jdk";
            TarballUtils.installPackage(repoInfo.getBaseUrl(), toolsHome, jdkHome, packageInfo, 1);
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

    private String getPkgName(String arch) {
        String replacedArch =
                switch (arch) {
                    case "x86_64" -> "x64";
                    case "arm64", "aarch64" -> "aarch64";
                    default -> {
                        log.error("Unsupported architecture: {}", arch);
                        throw new IllegalArgumentException("Unsupported architecture: " + arch);
                    }
                };
        String pkgName = MessageFormat.format("jdk-8u431-linux-{0}.tar.gz", replacedArch);
        log.debug("Generated package name: {}", pkgName);
        return pkgName;
    }

    private String getChecksum(String arch) {
        return switch (arch) {
            case "x64", "x86_64" -> "SHA-256:b396978a716b7d23ccccabfe5c47c3b75d2434d7f8f7af690bc648172382720d";
            case "arm64", "aarch64" -> "SHA-256:e68d3e31ffcf7f05a4de65d04974843073bdff238bb6524adb272de9e616be7c";
            default -> {
                log.error("Unknown arch for jdk: {}", arch);
                throw new IllegalArgumentException("Unknown arch for jdk: " + arch);
            }
        };
    }
}
