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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.hadoop;

import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.enums.ConfigType;
import org.apache.bigtop.manager.stack.core.exception.StackException;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
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
public class HadoopSetup {

    public static ShellResult configure(Params params) {
        return configure(params, null);
    }

    public static ShellResult configure(Params params, String componentName) {
        log.info("Configuring Hadoop");
        HadoopParams hadoopParams = (HadoopParams) params;

        String confDir = hadoopParams.confDir();
        String hadoopUser = hadoopParams.user();
        String hadoopGroup = hadoopParams.group();
        Map<String, Object> hadoopEnv = hadoopParams.hadoopEnv();
        Map<String, Object> yarnEnv = hadoopParams.yarnEnv();
        Map<String, Object> mapredEnv = hadoopParams.mapredEnv();

        if (StringUtils.isNotBlank(componentName)) {
            switch (componentName) {
                case "namenode": {
                    LinuxFileUtils.createDirectories(
                            hadoopParams.getDfsNameNodeDir(), hadoopUser, hadoopGroup, Constants.PERMISSION_755, true);
                    LinuxFileUtils.createDirectories(
                            hadoopParams.getDfsNameNodeCheckPointDir(),
                            hadoopUser,
                            hadoopGroup,
                            Constants.PERMISSION_755,
                            true);
                }
                case "secondarynamenode": {
                    LinuxFileUtils.createDirectories(
                            hadoopParams.getDfsNameNodeCheckPointDir(),
                            hadoopUser,
                            hadoopGroup,
                            Constants.PERMISSION_755,
                            true);
                }
                case "datanode": {
                    LinuxFileUtils.createDirectories(
                            hadoopParams.getDfsDomainSocketPathPrefix(),
                            hadoopUser,
                            hadoopGroup,
                            Constants.PERMISSION_755,
                            true);
                    if (StringUtils.isNotBlank(hadoopParams.getDfsDataDir())) {
                        String[] dfsDataDirs = hadoopParams.getDfsDataDir().split("\\s*,\\s*");
                        for (String dir : dfsDataDirs) {
                            LinuxFileUtils.createDirectories(
                                    dir, hadoopUser, hadoopGroup, Constants.PERMISSION_755, true);
                        }
                    }
                }
                case "nodemanager": {
                    if (StringUtils.isNotBlank(hadoopParams.getNodeManagerLogDir())) {
                        String[] nmLogDirs = hadoopParams.getNodeManagerLogDir().split("\\s*,\\s*");
                        for (String dir : nmLogDirs) {
                            LinuxFileUtils.createDirectories(
                                    dir, hadoopUser, hadoopGroup, Constants.PERMISSION_755, true);
                        }
                    }
                    if (StringUtils.isNotBlank(hadoopParams.getNodeManagerLocalDir())) {
                        String[] nmLocalDirs =
                                hadoopParams.getNodeManagerLocalDir().split("\\s*,\\s*");
                        for (String dir : nmLocalDirs) {
                            LinuxFileUtils.createDirectories(
                                    dir, hadoopUser, hadoopGroup, Constants.PERMISSION_755, true);
                        }
                    }
                }
            }
        }

        // mkdir directories
        LinuxFileUtils.createDirectories(
                hadoopParams.getHadoopLogDir(), hadoopUser, hadoopGroup, Constants.PERMISSION_755, true);
        LinuxFileUtils.createDirectories(
                hadoopParams.getHadoopPidDir(), hadoopUser, hadoopGroup, Constants.PERMISSION_755, true);

        LinuxFileUtils.toFileByTemplate(
                hadoopParams.getHadoopConfContent(),
                MessageFormat.format("{0}/hadoop.conf", HadoopParams.LIMITS_CONF_DIR),
                Constants.ROOT_USER,
                Constants.ROOT_USER,
                Constants.PERMISSION_644,
                hadoopParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                hadoopEnv.get("content").toString(),
                MessageFormat.format("{0}/hadoop-env.sh", confDir),
                hadoopUser,
                hadoopGroup,
                Constants.PERMISSION_644,
                hadoopParams.getGlobalParamsMap());

        LinuxFileUtils.toFile(
                ConfigType.XML,
                MessageFormat.format("{0}/core-site.xml", confDir),
                hadoopUser,
                hadoopGroup,
                Constants.PERMISSION_644,
                hadoopParams.coreSite());

        LinuxFileUtils.toFile(
                ConfigType.XML,
                MessageFormat.format("{0}/hdfs-site.xml", confDir),
                hadoopUser,
                hadoopGroup,
                Constants.PERMISSION_644,
                hadoopParams.hdfsSite());

        LinuxFileUtils.toFile(
                ConfigType.XML,
                MessageFormat.format("{0}/hadoop-policy.xml", confDir),
                hadoopUser,
                hadoopGroup,
                Constants.PERMISSION_644,
                hadoopParams.hadoopPolicy());

        LinuxFileUtils.toFileByTemplate(
                hadoopParams.workers(),
                MessageFormat.format("{0}/workers", confDir),
                hadoopUser,
                hadoopGroup,
                Constants.PERMISSION_644,
                hadoopParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                hadoopParams.hdfsLog4j().get("content").toString(),
                MessageFormat.format("{0}/log4j.properties", confDir),
                hadoopUser,
                hadoopGroup,
                Constants.PERMISSION_644,
                hadoopParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                yarnEnv.get("content").toString(),
                MessageFormat.format("{0}/yarn-env.sh", confDir),
                hadoopUser,
                hadoopGroup,
                Constants.PERMISSION_644,
                hadoopParams.getGlobalParamsMap());

        LinuxFileUtils.toFile(
                ConfigType.XML,
                MessageFormat.format("{0}/yarn-site.xml", confDir),
                hadoopUser,
                hadoopGroup,
                Constants.PERMISSION_644,
                hadoopParams.yarnSite(),
                hadoopParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                hadoopParams.yarnLog4j().get("content").toString(),
                MessageFormat.format("{0}/yarnservice-log4j.properties", confDir),
                hadoopUser,
                hadoopGroup,
                Constants.PERMISSION_644,
                hadoopParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                mapredEnv.get("content").toString(),
                MessageFormat.format("{0}/mapred-env.sh", confDir),
                hadoopUser,
                hadoopGroup,
                Constants.PERMISSION_644,
                hadoopParams.getGlobalParamsMap());

        LinuxFileUtils.toFile(
                ConfigType.XML,
                MessageFormat.format("{0}/mapred-site.xml", confDir),
                hadoopUser,
                hadoopGroup,
                Constants.PERMISSION_644,
                hadoopParams.mapredSite(),
                hadoopParams.getGlobalParamsMap());

        //        HdfsUtil.createDirectory(hadoopUser, "/apps");
        //        HdfsUtil.createDirectory(hadoopUser, "/app-logs");
        //        HdfsUtil.createDirectory(hadoopUser, "/apps/mapred");
        //        HdfsUtil.createDirectory(hadoopUser, "/apps/mapred/staging");
        //        HdfsUtil.createDirectory(hadoopUser, "/apps/mapred/history");
        //        HdfsUtil.createDirectory(hadoopUser, "/apps/mapred/history/tmp");
        //        HdfsUtil.createDirectory(hadoopUser, "/apps/mapred/history/done");

        log.info("Successfully configured Hadoop");
        return ShellResult.success();
    }

    public static void formatNameNode(HadoopParams hadoopParams) {
        if (!isNameNodeFormatted(hadoopParams)) {
            String formatCmd = MessageFormat.format(
                    "{0}/hdfs --config {1} namenode -format -nonInteractive",
                    hadoopParams.binDir(), hadoopParams.confDir());
            try {
                LinuxOSUtils.sudoExecCmd(formatCmd, hadoopParams.user());
            } catch (Exception e) {
                throw new StackException(e);
            }

            for (String nameNodeFormattedDir : hadoopParams.getNameNodeFormattedDirs()) {
                LinuxFileUtils.createDirectories(
                        nameNodeFormattedDir,
                        hadoopParams.user(),
                        hadoopParams.group(),
                        Constants.PERMISSION_755,
                        true);
            }
        }
    }

    public static boolean isNameNodeFormatted(HadoopParams hadoopParams) {

        boolean isFormatted = false;
        for (String nameNodeFormattedDir : hadoopParams.getNameNodeFormattedDirs()) {
            File file = new File(nameNodeFormattedDir);
            if (file.exists() && file.isDirectory()) {
                log.info("{} exists. Namenode DFS already formatted", nameNodeFormattedDir);
                isFormatted = true;
            }
        }

        if (isFormatted) {
            for (String nameNodeFormattedDir : hadoopParams.getNameNodeFormattedDirs()) {
                LinuxFileUtils.createDirectories(
                        nameNodeFormattedDir,
                        hadoopParams.user(),
                        hadoopParams.group(),
                        Constants.PERMISSION_755,
                        true);
            }
            return true;
        }

        // Check if name dirs are not empty
        String[] nameNodeDirs = hadoopParams.getDfsNameNodeDir().split(",");

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
