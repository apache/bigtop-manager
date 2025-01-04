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
package org.apache.bigtop.manager.stack.infra.v1_0_0.prometheus;

import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.enums.ConfigType;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxFileUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Map;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PrometheusSetup {

    public static ShellResult config(Params params) {
        PrometheusParams prometheusParams = (PrometheusParams) params;
        String user = prometheusParams.user();
        String group = prometheusParams.group();

        LinuxFileUtils.createDirectories(prometheusParams.dataDir(), user, group, Constants.PERMISSION_755, true);
        LinuxFileUtils.createDirectories(prometheusParams.confDir(), user, group, Constants.PERMISSION_755, true);

        LinuxFileUtils.toFileByTemplate(
                prometheusParams.getPrometheusContent(),
                MessageFormat.format("{0}/prometheus.yml", prometheusParams.confDir()),
                user,
                group,
                Constants.PERMISSION_644,
                prometheusParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                prometheusParams.getPrometheusRulesFileContent(),
                MessageFormat.format(
                        "{0}/{1}", prometheusParams.confDir(), prometheusParams.getPrometheusRulesFilename()),
                user,
                group,
                Constants.PERMISSION_644,
                prometheusParams.getGlobalParamsMap());

        for (int i = 0; i < prometheusParams.getScrapeJobs().size(); i++) {
            Map<String, Object> job = prometheusParams.getScrapeJobs().get(i);
            LinuxFileUtils.toFile(
                    ConfigType.JSON,
                    (String) job.get("targets_file"),
                    user,
                    group,
                    Constants.PERMISSION_644,
                    job.get("targets_list"));
        }

        return ShellResult.success("Prometheus Configure success!");
    }
}
