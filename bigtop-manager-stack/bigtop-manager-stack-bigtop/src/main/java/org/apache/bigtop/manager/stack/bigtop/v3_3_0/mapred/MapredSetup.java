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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.mapred;

import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.bigtop.utils.HdfsUtil;
import org.apache.bigtop.manager.stack.core.enums.ConfigType;
import org.apache.bigtop.manager.stack.core.param.BaseParams;
import org.apache.bigtop.manager.stack.core.param.Params;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxFileUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MapredSetup {

    public static ShellResult config(Params params) {
        Mapreduce2Params mapreduce2Params = (Mapreduce2Params) params;
        String hdfsUser = LocalSettings.users().get("hdfs");
        String mapredUser = params.user();
        String mapredGroup = params.group();
        String confDir = mapreduce2Params.confDir();

        LinuxFileUtils.createDirectories(
                mapreduce2Params.getMapredLogDir(), mapredUser, mapredGroup, Constants.PERMISSION_755, true);
        LinuxFileUtils.createDirectories(
                mapreduce2Params.getMapredPidDir(), mapredUser, mapredGroup, Constants.PERMISSION_755, true);

        // mapreduce.conf
        log.info("Generating [{}/mapreduce.conf] file", BaseParams.LIMITS_CONF_DIR);
        LinuxFileUtils.toFileByTemplate(
                mapreduce2Params.mapredLimits(),
                MessageFormat.format("{0}/mapreduce.conf", BaseParams.LIMITS_CONF_DIR),
                Constants.ROOT_USER,
                Constants.ROOT_USER,
                Constants.PERMISSION_644,
                mapreduce2Params.getGlobalParamsMap());

        // mapred-env.sh
        log.info("Generating [{}/mapred-env.sh] file", confDir);
        LinuxFileUtils.toFileByTemplate(
                mapreduce2Params.mapredEnv().get("content").toString(),
                MessageFormat.format("{0}/mapred-env.sh", confDir),
                mapredUser,
                mapredGroup,
                Constants.PERMISSION_644,
                mapreduce2Params.getGlobalParamsMap());

        // mapred-site.xml
        log.info("Generating [{}/mapred-site.xml] file", confDir);
        LinuxFileUtils.toFile(
                ConfigType.XML,
                MessageFormat.format("{0}/mapred-site.xml", confDir),
                mapredUser,
                mapredGroup,
                Constants.PERMISSION_644,
                mapreduce2Params.mapredSite(),
                mapreduce2Params.getGlobalParamsMap());

        HdfsUtil.createDirectory(hdfsUser, "/apps");
        HdfsUtil.createDirectory(hdfsUser, "/app-logs");
        HdfsUtil.createDirectory(mapredUser, "/apps/mapred");
        HdfsUtil.createDirectory(mapredUser, "/apps/mapred/staging");
        HdfsUtil.createDirectory(mapredUser, "/apps/mapred/history");
        HdfsUtil.createDirectory(mapredUser, "/apps/mapred/history/tmp");
        HdfsUtil.createDirectory(mapredUser, "/apps/mapred/history/done");

        log.info("Successfully configured MapReduce2");
        return ShellResult.success("MapReduce2 Configure success!");
    }
}
