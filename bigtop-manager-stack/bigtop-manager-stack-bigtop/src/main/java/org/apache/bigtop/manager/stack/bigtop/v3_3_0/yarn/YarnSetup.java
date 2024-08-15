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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.yarn;

import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.enums.ConfigType;
import org.apache.bigtop.manager.stack.core.param.BaseParams;
import org.apache.bigtop.manager.stack.core.param.Params;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxFileUtils;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Map;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class YarnSetup {

    public static ShellResult config(Params params) {
        return config(params, null);
    }

    public static ShellResult config(Params params, String componentName) {
        log.info("Setting yarn config");
        YarnParams yarnParams = (YarnParams) params;

        String confDir = yarnParams.confDir();
        String yarnUser = yarnParams.user();
        String yarnGroup = yarnParams.group();
        Map<String, Object> yarnEnv = yarnParams.yarnEnv();

        if (StringUtils.isNotBlank(componentName)) {
            switch (componentName) {
                case "resourcemanager": {
                    LinuxFileUtils.toFileByTemplate(
                            yarnParams.excludeNodesContent(),
                            yarnParams.getRmNodesExcludeDir(),
                            yarnUser,
                            yarnGroup,
                            Constants.PERMISSION_644,
                            yarnParams.getGlobalParamsMap());
                }
                case "nodemanager": {
                    LinuxFileUtils.createDirectories(
                            yarnParams.getNodemanagerLogDir(), yarnUser, yarnGroup, Constants.PERMISSION_755, true);
                    LinuxFileUtils.createDirectories(
                            yarnParams.getNodemanagerLocalDir(), yarnUser, yarnGroup, Constants.PERMISSION_755, true);
                }
            }
        }

        // mkdir directories
        LinuxFileUtils.createDirectories(
                yarnParams.getYarnLogDir(), yarnUser, yarnGroup, Constants.PERMISSION_755, true);
        LinuxFileUtils.createDirectories(
                yarnParams.getYarnPidDir(), yarnUser, yarnGroup, Constants.PERMISSION_755, true);
        LinuxFileUtils.createDirectories(yarnParams.getTmpDir(), yarnUser, yarnGroup, Constants.PERMISSION_755, true);

        // yarn.conf
        LinuxFileUtils.toFileByTemplate(
                yarnParams.yarnLimits(),
                MessageFormat.format("{0}/yarn.conf", BaseParams.LIMITS_CONF_DIR),
                Constants.ROOT_USER,
                Constants.ROOT_USER,
                Constants.PERMISSION_644,
                yarnParams.getGlobalParamsMap());

        // yarn-env.sh
        LinuxFileUtils.toFileByTemplate(
                yarnEnv.get("content").toString(),
                MessageFormat.format("{0}/yarn-env.sh", confDir),
                yarnUser,
                yarnGroup,
                Constants.PERMISSION_644,
                yarnParams.getGlobalParamsMap());

        // yarn-site.xml
        LinuxFileUtils.toFile(
                ConfigType.XML,
                MessageFormat.format("{0}/yarn-site.xml", confDir),
                yarnUser,
                yarnGroup,
                Constants.PERMISSION_644,
                yarnParams.yarnSite(),
                yarnParams.getGlobalParamsMap());

        // log4j
        LinuxFileUtils.toFileByTemplate(
                yarnParams.yarnLog4j().get("content").toString(),
                MessageFormat.format("{0}/yarnservice-log4j.properties", confDir),
                yarnUser,
                yarnGroup,
                Constants.PERMISSION_644,
                yarnParams.getGlobalParamsMap());

        return ShellResult.success("YARN Configure success!");
    }
}
