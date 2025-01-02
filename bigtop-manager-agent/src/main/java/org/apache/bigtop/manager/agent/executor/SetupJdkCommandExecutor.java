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
package org.apache.bigtop.manager.agent.executor;

import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.message.entity.pojo.ClusterInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.PackageInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.RepoInfo;
import org.apache.bigtop.manager.common.utils.os.OSDetection;
import org.apache.bigtop.manager.grpc.generated.CommandType;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;
import org.apache.bigtop.manager.stack.core.utils.TarballUtils;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxFileUtils;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SetupJdkCommandExecutor extends AbstractCommandExecutor {

    @Override
    public CommandType getCommandType() {
        return CommandType.SETUP_JDK;
    }

    /**
     * TODO JDK info currently is hardcoded, need to improve and make it configurable.
     */
    @Override
    public void doExecute() {
        log.info("Setting up cluster jdk...");
        String arch = OSDetection.getArch();
        String pkgName = getPkgName(arch);
        String checksum = getChecksum(arch);
        PackageInfo packageInfo = new PackageInfo(pkgName, checksum);
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
    }

    private String getPkgName(String arch) {
        String replacedArch = arch.equals("x86_64") ? "x64" : arch;
        replacedArch = replacedArch.equals("arm64") ? "aarch64" : arch;
        return MessageFormat.format("jdk-8u431-linux-{0}.tar.gz", replacedArch);
    }

    private String getChecksum(String arch) {
        return switch (arch) {
            case "x64", "x86_64" -> "SHA-256:b396978a716b7d23ccccabfe5c47c3b75d2434d7f8f7af690bc648172382720d";
            case "arm64", "aarch64" -> "SHA-256:e68d3e31ffcf7f05a4de65d04974843073bdff238bb6524adb272de9e616be7c";
            default -> throw new RuntimeException("Unknown arch for jdk: " + arch);
        };
    }
}
