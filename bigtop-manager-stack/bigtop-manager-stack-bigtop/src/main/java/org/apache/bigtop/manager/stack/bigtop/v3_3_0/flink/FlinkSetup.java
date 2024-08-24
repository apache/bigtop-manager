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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.flink;

import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.bigtop.utils.HdfsUtil;
import org.apache.bigtop.manager.stack.core.param.Params;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxFileUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FlinkSetup {

    public static ShellResult config(Params params) {
        FlinkParams flinkParams = (FlinkParams) params;
        String flinkUser = params.user();
        String flinkGroup = params.group();
        String confDir = flinkParams.confDir();

        LinuxFileUtils.createDirectories(
                flinkParams.getFlinkLogDir(), flinkUser, flinkGroup, Constants.PERMISSION_755, true);
        LinuxFileUtils.createDirectories(
                flinkParams.getFlinkPidDir(), flinkUser, flinkGroup, Constants.PERMISSION_755, true);

        // log4j.properties
        log.info("Generating [{}/log4j.properties] file", confDir);
        LinuxFileUtils.toFileByTemplate(
                flinkParams.getFlinkLog4jPropertiesContent(),
                MessageFormat.format("{0}/log4j.properties", confDir),
                flinkUser,
                flinkGroup,
                Constants.PERMISSION_644,
                flinkParams.getGlobalParamsMap());

        // log4j-cli.properties
        log.info("Generating [{}/log4j-cli.properties] file", confDir);
        LinuxFileUtils.toFileByTemplate(
                flinkParams.getFlinkLog4jCLiPropertiesContent(),
                MessageFormat.format("{0}/log4j-cli.properties", confDir),
                flinkUser,
                flinkGroup,
                Constants.PERMISSION_644,
                flinkParams.getGlobalParamsMap());

        // log4j-console.properties
        log.info("Generating [{}/log4j-console.properties] file", confDir);
        LinuxFileUtils.toFileByTemplate(
                flinkParams.getFlinkLog4jConsolePropertiesContent(),
                MessageFormat.format("{0}/log4j-console.properties", confDir),
                flinkUser,
                flinkGroup,
                Constants.PERMISSION_644,
                flinkParams.getGlobalParamsMap());

        // log4j-session.properties
        log.info("Generating [{}/log4j-session.properties] file", confDir);
        LinuxFileUtils.toFileByTemplate(
                flinkParams.getFlinkLog4jSessionPropertiesContent(),
                MessageFormat.format("{0}/log4j-session.properties", confDir),
                flinkUser,
                flinkGroup,
                Constants.PERMISSION_644,
                flinkParams.getGlobalParamsMap());

        // flink-conf.yaml
        log.info("Generating [{}/flink-conf.yaml] file", confDir);
        LinuxFileUtils.toFileByTemplate(
                flinkParams.getFlinkConfContent(),
                MessageFormat.format("{0}/flink-conf.yaml", confDir),
                flinkUser,
                flinkGroup,
                Constants.PERMISSION_644,
                flinkParams.getGlobalParamsMap());

        HdfsUtil.createDirectory(flinkUser, "/completed-jobs");

        return ShellResult.success("Flink Configure success!");
    }
}
