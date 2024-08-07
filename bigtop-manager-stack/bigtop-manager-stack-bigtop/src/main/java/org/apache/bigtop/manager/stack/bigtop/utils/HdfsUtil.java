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
package org.apache.bigtop.manager.stack.bigtop.utils;

import org.apache.bigtop.manager.stack.common.exception.StackException;
import org.apache.bigtop.manager.stack.common.utils.LocalSettings;

import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.List;

@Data
@Slf4j
public class HdfsUtil {

    public static void uploadFile(String user, String localFilePath, String destDir) {
        uploadFile(user, localFilePath, destDir, null);
    }

    public static void uploadFile(String user, String localFilePath, String destDir, String destFilename) {
        Configuration conf = new Configuration();
        conf.addResource(new Path("/etc/hadoop/conf/core-site.xml"));
        conf.addResource(new Path("/etc/hadoop/conf/hdfs-site.xml"));

        List<String> namenodeList = LocalSettings.hosts("namenode");
        if (CollectionUtils.isEmpty(namenodeList)) {
            String msg = "No namenode found in the cluster";
            log.error(msg);
            throw new StackException(msg);
        }

        String hdfsUri = MessageFormat.format("hdfs://{0}:8020", namenodeList.get(0));
        UserGroupInformation ugi = UserGroupInformation.createRemoteUser(user);
        try {
            ugi.doAs((PrivilegedAction<Void>) () -> {
                try (FileSystem fs = FileSystem.get(new URI(hdfsUri), conf)) {
                    // Create dest dir if not exist
                    Path destDirPath = new Path(destDir);
                    if (!fs.exists(destDirPath)) {
                        fs.mkdirs(destDirPath);
                    }

                    // upload file
                    Path destFilePath = destFilename == null ? new Path(destDir) : new Path(destDir, destFilename);
                    fs.copyFromLocalFile(new Path(localFilePath), destFilePath);
                } catch (Exception e) {
                    log.error("Error while uploading file to hdfs", e);
                    throw new StackException(e);
                }

                return null;
            });
        } catch (Exception e) {
            log.error("Error while uploading file to hdfs", e);
            throw new StackException(e);
        }
    }
}
