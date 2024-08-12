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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.hdfs;

import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.bigtop.v3_3_0.kafka.KafkaParams;
import org.apache.bigtop.manager.stack.core.enums.ConfigType;
import org.apache.bigtop.manager.stack.core.exception.StackException;
import org.apache.bigtop.manager.stack.core.param.Params;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxFileUtils;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxOSUtils;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.text.MessageFormat;
import java.util.Map;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HdfsSetup {

    public static ShellResult config(Params params) {
        return config(params, null);
    }

    public static ShellResult config(Params params, String componentName) {
        log.info("Setting hdfs config");
        HdfsParams hdfsParams = (HdfsParams) params;

        String confDir = hdfsParams.confDir();
        String hdfsUser = hdfsParams.user();
        String hdfsGroup = hdfsParams.group();
        Map<String, Object> hadoopEnv = hdfsParams.hadoopEnv();

        if (StringUtils.isNotBlank(componentName)) {
            switch (componentName) {
                case "namenode": {
                    LinuxFileUtils.createDirectories(
                            hdfsParams.getDfsNameNodeDir(), hdfsUser, hdfsGroup, Constants.PERMISSION_755, true);
                }
                case "secondarynamenode": {
                    LinuxFileUtils.createDirectories(
                            hdfsParams.getDfsNameNodeCheckPointDir(),
                            hdfsUser,
                            hdfsGroup,
                            Constants.PERMISSION_755,
                            true);
                }
                case "datanode": {
                    LinuxFileUtils.createDirectories(
                            hdfsParams.getDfsDomainSocketPathPrefix(),
                            hdfsUser,
                            hdfsGroup,
                            Constants.PERMISSION_755,
                            true);
                }
            }
        }

        // mkdir directories
        LinuxFileUtils.createDirectories(
                hdfsParams.getDfsDataDir(), hdfsUser, hdfsGroup, Constants.PERMISSION_755, true);
        LinuxFileUtils.createDirectories(
                hdfsParams.getHadoopLogDir(), hdfsUser, hdfsGroup, Constants.PERMISSION_775, true);
        LinuxFileUtils.createDirectories(
                hdfsParams.getHadoopPidDir(), hdfsUser, hdfsGroup, Constants.PERMISSION_755, true);

        // hdfs.limits
        LinuxFileUtils.toFileByTemplate(
                hdfsParams.hdfsLimits(),
                MessageFormat.format("{0}/hdfs.conf", KafkaParams.LIMITS_CONF_DIR),
                Constants.ROOT_USER,
                Constants.ROOT_USER,
                Constants.PERMISSION_644,
                hdfsParams.getGlobalParamsMap());

        // hadoop-env.sh
        LinuxFileUtils.toFileByTemplate(
                hadoopEnv.get("content").toString(),
                MessageFormat.format("{0}/hadoop-env.sh", confDir),
                hdfsUser,
                hdfsGroup,
                Constants.PERMISSION_644,
                hdfsParams.getGlobalParamsMap());

        // core-site.xml
        LinuxFileUtils.toFile(
                ConfigType.XML,
                MessageFormat.format("{0}/core-site.xml", confDir),
                hdfsUser,
                hdfsGroup,
                Constants.PERMISSION_644,
                hdfsParams.coreSite());

        // hdfs-site.xml
        LinuxFileUtils.toFile(
                ConfigType.XML,
                MessageFormat.format("{0}/hdfs-site.xml", confDir),
                hdfsUser,
                hdfsGroup,
                Constants.PERMISSION_644,
                hdfsParams.hdfsSite());

        // hdfs-policy.xml
        LinuxFileUtils.toFile(
                ConfigType.XML,
                MessageFormat.format("{0}/hadoop-policy.xml", confDir),
                hdfsUser,
                hdfsGroup,
                Constants.PERMISSION_644,
                hdfsParams.hadoopPolicy());

        // hdfs-policy.xml
        LinuxFileUtils.toFileByTemplate(
                hdfsParams.workers(),
                MessageFormat.format("{0}/workers", confDir),
                hdfsUser,
                hdfsGroup,
                Constants.PERMISSION_644,
                hdfsParams.getGlobalParamsMap());

        // log4j
        LinuxFileUtils.toFileByTemplate(
                hdfsParams.hdfsLog4j().get("content").toString(),
                MessageFormat.format("{0}/log4j.properties", confDir),
                hdfsUser,
                hdfsGroup,
                Constants.PERMISSION_644,
                hdfsParams.getGlobalParamsMap());

        // log.info("Creating /apps on hdfs");
        // HdfsUtil.createDirectory(hdfsUser, "/apps");

        log.info("Successfully configured HDFS");
        return ShellResult.success("HDFS Configure success!");
    }

    public static void formatNameNode(HdfsParams hdfsParams) {
        if (!isNameNodeFormatted(hdfsParams)) {
            String formatCmd = MessageFormat.format(
                    "{0} --config {1} namenode -format -nonInteractive", hdfsParams.hdfsExec(), hdfsParams.confDir());
            try {
                LinuxOSUtils.sudoExecCmd(formatCmd, hdfsParams.user());
            } catch (Exception e) {
                throw new StackException(e);
            }

            for (String nameNodeFormattedDir : hdfsParams.getNameNodeFormattedDirs()) {
                LinuxFileUtils.createDirectories(
                        nameNodeFormattedDir, hdfsParams.user(), hdfsParams.group(), Constants.PERMISSION_755, true);
            }
        }
    }

    public static boolean isNameNodeFormatted(HdfsParams hdfsParams) {

        boolean isFormatted = false;
        for (String nameNodeFormattedDir : hdfsParams.getNameNodeFormattedDirs()) {
            File file = new File(nameNodeFormattedDir);
            if (file.exists() && file.isDirectory()) {
                log.info("{} exists. Namenode DFS already formatted", nameNodeFormattedDir);
                isFormatted = true;
            }
        }

        if (isFormatted) {
            for (String nameNodeFormattedDir : hdfsParams.getNameNodeFormattedDirs()) {
                LinuxFileUtils.createDirectories(
                        nameNodeFormattedDir, hdfsParams.user(), hdfsParams.group(), Constants.PERMISSION_755, true);
            }
            return true;
        }

        // Check if name dirs are not empty
        String[] nameNodeDirs = hdfsParams.getDfsNameNodeDir().split(",");

        for (String nameNodeDir : nameNodeDirs) {
            File file = new File(nameNodeDir);
            if (!file.exists()) {
                log.info(
                        "NameNode will not be formatted because the directory {} is missing or cannot be checked for content.",
                        nameNodeDir);
                return true;
            } else {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    log.info("NameNode will not be formatted since {} exists and contains content", nameNodeDir);
                    return true;
                }
            }
        }

        return false;
    }
}
