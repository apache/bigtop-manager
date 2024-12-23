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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.spark;

import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.shell.ShellResult;
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
public class SparkSetup {

    public static ShellResult configure(Params params) {
        log.info("Configuring Spark");
        SparkParams sparkParams = (SparkParams) params;

        String confDir = sparkParams.confDir();
        String sparkUser = sparkParams.user();
        String sparkGroup = sparkParams.group();

        LinuxFileUtils.createDirectories(sparkParams.getSparkLogDir(), sparkUser, sparkGroup, PERMISSION_755, true);
        LinuxFileUtils.createDirectories(sparkParams.getSparkPidDir(), sparkUser, sparkGroup, PERMISSION_755, true);

        String sparkHistoryLogDir = sparkParams.getSparkHistoryLogDir();
        if (sparkHistoryLogDir.startsWith("file:")) {
            String dir = sparkHistoryLogDir.split(":")[1];
            LinuxFileUtils.createDirectories(dir, sparkUser, sparkGroup, PERMISSION_755, true);
        }

        LinuxFileUtils.toFileByTemplate(
                sparkParams.getSparkEnvContent(),
                MessageFormat.format("{0}/spark-env.sh", confDir),
                sparkUser,
                sparkGroup,
                Constants.PERMISSION_755,
                sparkParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                sparkParams.getSparkDefaultsContent(),
                MessageFormat.format("{0}/spark-defaults.conf", confDir),
                sparkUser,
                sparkGroup,
                Constants.PERMISSION_755,
                sparkParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                sparkParams.getSparkFairSchedulerContent(),
                MessageFormat.format("{0}/fairscheduler.xml", confDir),
                sparkUser,
                sparkGroup,
                Constants.PERMISSION_755,
                sparkParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                sparkParams.getSparkLog4j2Content(),
                MessageFormat.format("{0}/log4j2.properties", confDir),
                sparkUser,
                sparkGroup,
                Constants.PERMISSION_755,
                sparkParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                sparkParams.getSparkMetricsContent(),
                MessageFormat.format("{0}/metrics.properties", confDir),
                sparkUser,
                sparkGroup,
                Constants.PERMISSION_755,
                sparkParams.getGlobalParamsMap());

        LinuxFileUtils.toFile(
                ConfigType.XML,
                MessageFormat.format("{0}/hive-site.xml", confDir),
                sparkUser,
                sparkGroup,
                Constants.PERMISSION_644,
                sparkParams.sparkHiveSite());

        log.info("Successfully configured Spark");
        return ShellResult.success();
    }
}
