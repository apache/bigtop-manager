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
package org.apache.bigtop.manager.stack.core.tarball;

import org.apache.bigtop.manager.grpc.pojo.PackageInfo;
import org.apache.bigtop.manager.grpc.pojo.RepoInfo;
import org.apache.bigtop.manager.stack.core.exception.StackException;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
public class FileDownloader {

    public static void download(String saveDir, RepoInfo repoInfo) {
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.setName(repoInfo.getPkgName());
        packageInfo.setChecksum(repoInfo.getChecksum());

        String remoteUrl = repoInfo.getBaseUrl() + File.separator + repoInfo.getPkgName();
        download(remoteUrl, saveDir, packageInfo);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void download(String remoteUrl, String saveDir, PackageInfo packageInfo) {
        File localFile = new File(saveDir + File.separator + packageInfo.getName());
        String algorithm = packageInfo.getChecksum().split(":")[0];
        String checksum = packageInfo.getChecksum().split(":")[1];

        if (localFile.exists()) {
            log.info("File [{}] exists, validating checksum", localFile.getAbsolutePath());
        } else {
            log.info("Downloading [{}] to [{}]", remoteUrl, saveDir);
            download(remoteUrl, saveDir);
        }

        boolean validateChecksum = ChecksumValidator.validateChecksum(algorithm, checksum, localFile);
        if (!validateChecksum) {
            log.warn("Invalid checksum for [{}], re-downloading...", localFile.getAbsolutePath());
            localFile.delete();
            download(remoteUrl, saveDir);
        }

        validateChecksum = ChecksumValidator.validateChecksum(algorithm, checksum, localFile);
        if (!validateChecksum) {
            log.error("Invalid checksum for [{}], exiting...", localFile.getAbsolutePath());
            throw new StackException("Invalid checksum for " + localFile.getAbsolutePath());
        }

        log.info("Checksum validate successfully for [{}]", localFile.getAbsolutePath());
    }

    public static void download(String remoteUrl, String saveDir) {
        int i = 1;
        while (true) {
            Boolean downloaded = downloadFile(remoteUrl, saveDir);
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static Boolean downloadFile(String fileUrl, String saveDir) {
        HttpURLConnection httpConn = null;
        try {
            URL url = new URL(fileUrl);
            httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                InputStream inputStream = httpConn.getInputStream();
                String saveFilePath = saveDir + File.separator + fileName;

                if (!new File(saveDir).exists()) {
                    new File(saveDir).mkdirs();
                }

                FileOutputStream outputStream = new FileOutputStream(saveFilePath);

                int bytesRead = -1;
                byte[] buffer = new byte[4096];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();

                log.info("File downloaded: [{}]", saveFilePath);
                return true;
            } else {
                log.info("No file to download. Server replied HTTP code: [{}]", responseCode);
                return false;
            }
        } catch (Exception e) {
            log.error("Error downloading file: {}", e.getMessage());
            return false;
        } finally {
            if (httpConn != null) {
                httpConn.disconnect();
            }
        }
    }
}
