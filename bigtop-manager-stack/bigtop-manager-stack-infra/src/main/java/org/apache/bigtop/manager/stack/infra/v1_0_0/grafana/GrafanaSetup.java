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
package org.apache.bigtop.manager.stack.infra.v1_0_0.grafana;

import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxFileUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GrafanaSetup {

    public static ShellResult config(Params params) {
        GrafanaParams grafanaParams = (GrafanaParams) params;
        String user = grafanaParams.user();
        String group = grafanaParams.group();

        LinuxFileUtils.createDirectories(grafanaParams.dataDir(), user, group, Constants.PERMISSION_755, true);
        LinuxFileUtils.createDirectories(grafanaParams.dataSourceDir(), user, group, Constants.PERMISSION_755, true);

        LinuxFileUtils.toFileByTemplate(
                grafanaParams.getGrafanaContent(),
                MessageFormat.format("{0}/grafana.ini", grafanaParams.confDir()),
                user,
                group,
                Constants.PERMISSION_644,
                grafanaParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                grafanaParams.getDataSourceContent(),
                MessageFormat.format("{0}/prometheus.yaml", grafanaParams.dataSourceDir()),
                user,
                group,
                Constants.PERMISSION_644,
                grafanaParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                grafanaParams.getGrafanaDashboardContent(),
                MessageFormat.format("{0}/bm-dashboards.yaml", grafanaParams.dashboardsDir()),
                user,
                group,
                Constants.PERMISSION_644,
                grafanaParams.getGlobalParamsMap());

        return ShellResult.success("Grafana Configure success!");
    }
}
