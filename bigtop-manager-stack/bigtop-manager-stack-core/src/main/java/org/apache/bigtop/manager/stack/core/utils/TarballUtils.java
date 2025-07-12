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
package org.apache.bigtop.manager.stack.core.utils;

import org.apache.bigtop.manager.grpc.pojo.PackageInfo;
import org.apache.bigtop.manager.stack.core.tarball.FileDownloader;
import org.apache.bigtop.manager.stack.core.tarball.TarballExtractor;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class TarballUtils {

    public static void installPackage(
            String repoUrl, String stackHome, String serviceHome, PackageInfo packageInfo, Integer skipLevels) {
        if (Files.exists(Path.of(serviceHome))) {
            log.info("Service home [{}] exists, skip downloading...", serviceHome);
            return;
        }

        String remoteUrl = repoUrl + File.separator + packageInfo.getName();
        File localFile = new File(stackHome + File.separator + packageInfo.getName());
        FileDownloader.download(remoteUrl, stackHome, packageInfo);

        log.info("Extracting [{}] to [{}]", localFile.getAbsolutePath(), serviceHome);
        TarballExtractor.extractTarball(localFile.getAbsolutePath(), serviceHome, skipLevels);
        log.info("File [{}] successfully extracted to [{}]", localFile.getAbsolutePath(), serviceHome);
    }
}
