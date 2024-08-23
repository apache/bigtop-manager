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

import org.apache.bigtop.manager.common.message.entity.pojo.PackageInfo;
import org.apache.bigtop.manager.stack.core.exception.StackException;
import org.apache.bigtop.manager.stack.core.tarball.ChecksumValidator;
import org.apache.bigtop.manager.stack.core.tarball.TarballDownloader;
import org.apache.bigtop.manager.stack.core.tarball.TarballExtractor;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class TarballUtils {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void installPackage(
            String repoUrl, String stackHome, String serviceHome, PackageInfo packageInfo, Integer skipLevels) {
        String remoteUrl = repoUrl + File.separator + packageInfo.getName();
        File localFile = new File(stackHome + File.separator + packageInfo.getName());
        String algorithm = packageInfo.getChecksum().split(":")[0];
        String checksum = packageInfo.getChecksum().split(":")[1];

        if (localFile.exists()) {
            log.info("File [{}] exists, validating checksum", localFile.getAbsolutePath());
        } else {
            log.info("Downloading [{}] to [{}]", remoteUrl, stackHome);
            downloadPackage(remoteUrl, stackHome);
        }

        boolean validateChecksum = ChecksumValidator.validateChecksum(algorithm, checksum, localFile);
        if (!validateChecksum) {
            log.warn("Invalid checksum for [{}], re-downloading...", localFile.getAbsolutePath());
            localFile.delete();
            downloadPackage(remoteUrl, stackHome);
        }

        validateChecksum = ChecksumValidator.validateChecksum(algorithm, checksum, localFile);
        if (!validateChecksum) {
            log.error("Invalid checksum for [{}], exiting...", localFile.getAbsolutePath());
            throw new StackException("Invalid checksum for " + localFile.getAbsolutePath());
        }

        log.info("Checksum validate successfully for [{}]", localFile.getAbsolutePath());
        log.info("Extracting [{}] to [{}]", localFile.getAbsolutePath(), serviceHome);
        TarballExtractor.extractTarball(localFile.getAbsolutePath(), serviceHome, skipLevels);
        log.info("File [{}] successfully extracted to [{}]", localFile.getAbsolutePath(), serviceHome);
    }

    private static void downloadPackage(String remoteUrl, String saveDir) {
        int i = 1;
        while (true) {
            Boolean downloaded = TarballDownloader.downloadFile(remoteUrl, saveDir);
            if (downloaded) {
                break;
            } else {
                if (i == 3) {
                    log.error("Failed to download [{}], exiting...", remoteUrl);
                    throw new StackException("Failed to download " + remoteUrl);
                } else {
                    log.error("Failed to download [{}], retrying...: {}", remoteUrl, i);
                }
            }

            i++;
        }
    }
}
