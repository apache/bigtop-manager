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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.hive;

import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.bigtop.v3_3_0.hadoop.HadoopParams;
import org.apache.bigtop.manager.stack.core.enums.ConfigType;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxFileUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

import static org.apache.bigtop.manager.common.constants.Constants.PERMISSION_755;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HiveSetup {

    public static ShellResult configure(Params params) {
        log.info("Configuring Hive");
        HiveParams hiveParams = (HiveParams) params;

        String confDir = hiveParams.confDir();
        String hiveUser = hiveParams.user();
        String hiveGroup = hiveParams.group();

        LinuxFileUtils.createDirectories(hiveParams.getHiveLogDir(), hiveUser, hiveGroup, PERMISSION_755, true);
        LinuxFileUtils.createDirectories(hiveParams.getHivePidDir(), hiveUser, hiveGroup, PERMISSION_755, true);

        LinuxFileUtils.toFile(
                ConfigType.CONTENT,
                MessageFormat.format("{0}/hive-service.sh", hiveParams.serviceHome() + "/bin"),
                hiveUser,
                hiveGroup,
                Constants.PERMISSION_755,
                hiveParams.getHiveShellContent());

        LinuxFileUtils.toFileByTemplate(
                hiveParams.hiveLimits(),
                MessageFormat.format("{0}/hive.conf", HadoopParams.LIMITS_CONF_DIR),
                Constants.ROOT_USER,
                Constants.ROOT_USER,
                Constants.PERMISSION_644,
                hiveParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                hiveParams.getHiveEnvContent(),
                MessageFormat.format("{0}/hive-env.sh", confDir),
                hiveUser,
                hiveGroup,
                Constants.PERMISSION_644,
                hiveParams.getGlobalParamsMap());

        LinuxFileUtils.toFile(
                ConfigType.XML,
                MessageFormat.format("{0}/hive-site.xml", confDir),
                hiveUser,
                hiveGroup,
                Constants.PERMISSION_644,
                hiveParams.hiveSite());

        LinuxFileUtils.toFileByTemplate(
                hiveParams.getHiveLog4j2Content(),
                MessageFormat.format("{0}/hive-log4j2.properties", confDir),
                hiveUser,
                hiveGroup,
                Constants.PERMISSION_644,
                hiveParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                hiveParams.getBeelineLog4j2Content(),
                MessageFormat.format("{0}/beeline-log4j2.properties", confDir),
                hiveUser,
                hiveGroup,
                Constants.PERMISSION_644,
                hiveParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                hiveParams.getHiveExecLog4j2Content(),
                MessageFormat.format("{0}/hive-exec-log4j2.properties", confDir),
                hiveUser,
                hiveGroup,
                Constants.PERMISSION_644,
                hiveParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                hiveParams.getLlapCliLog4j2Content(),
                MessageFormat.format("{0}/llap-cli-log4j2.properties", confDir),
                hiveUser,
                hiveGroup,
                Constants.PERMISSION_644,
                hiveParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                hiveParams.getLlapDaemonLog4j2Content(),
                MessageFormat.format("{0}/llap-daemon-log4j2.properties", confDir),
                hiveUser,
                hiveGroup,
                Constants.PERMISSION_644,
                hiveParams.getGlobalParamsMap());

        log.info("Successfully configured Hive");
        return ShellResult.success();
    }
}
