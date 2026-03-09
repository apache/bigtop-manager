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
package org.apache.bigtop.manager.stack.extra.v1_0_0.trino;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.enums.ConfigType;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxFileUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TrinoSetup {

    public static ShellResult config(TrinoParams params, String componentName) {

        String user = params.user();
        String group = params.group();

        Map<String, Object> globalParamsMap = params.getGlobalParamsMap();

        LinuxFileUtils.createDirectories(params.confDir(), user, group, Constants.PERMISSION_755, true);

        // create data dir
        Map<String, Object> nodeProperties = params.nodeProperties();
        Object dataDir = nodeProperties.get("node.data-dir");
        if (dataDir != null && StringUtils.isNotBlank(dataDir.toString())) {
            LinuxFileUtils.createDirectories(dataDir.toString(), user, group, Constants.PERMISSION_755, true);
        }

        Map<String, Object> nodeMap = new HashMap<>(nodeProperties);

        nodeMap.put("node.id", params.hostname());

        Map<String, Object> configProperties = new HashMap<>(params.configProperties());
        configProperties.put("coordinator", StringUtils.equalsIgnoreCase(componentName, "trino_coordinator"));

        LinuxFileUtils.toFile(
                ConfigType.PROPERTIES,
                MessageFormat.format("{0}/node.properties", params.confDir()),
                user,
                group,
                Constants.PERMISSION_644,
                nodeMap);

        LinuxFileUtils.toFileByTemplate(
                params.getJvmConfigContent(),
                MessageFormat.format("{0}/jvm.config", params.confDir()),
                user,
                group,
                Constants.PERMISSION_644,
                globalParamsMap);

        LinuxFileUtils.toFile(
                ConfigType.PROPERTIES,
                MessageFormat.format("{0}/config.properties", params.confDir()),
                user,
                group,
                Constants.PERMISSION_644,
                configProperties);

        // create catalog dir and catalog.properties
        LinuxFileUtils.createDirectories(
                MessageFormat.format("{0}/catalog", params.confDir()), user, group, Constants.PERMISSION_755, true);

        Map<String, Object> hiveCatalog = createHiveCatalog(params);
        if (!hiveCatalog.isEmpty()) {
            LinuxFileUtils.toFile(
                    ConfigType.PROPERTIES,
                    MessageFormat.format("{0}/catalog/hive.properties", params.confDir()),
                    user,
                    group,
                    Constants.PERMISSION_644,
                    hiveCatalog);
        }

        return ShellResult.success("Trino Configure success!");
    }

    private static Map<String, Object> createHiveCatalog(TrinoParams params) {
        Map<String, Object> hiveCatalog = params.hiveProperties();
        hiveCatalog.put("connector.name", "hive");

        Map<String, Object> hiveSite = LocalSettings.configurations("hive", "hive-site");
        String metastoreUris = hiveSite.get("hive.metastore.uris").toString();
        String[] split = metastoreUris.split(":");
        String metastorePort = split[split.length - 1];

        List<String> hiveMetastores = LocalSettings.componentHosts("hive_metastore");
        if (hiveMetastores != null && !hiveMetastores.isEmpty()) {
            String hiveMetastore = hiveMetastores.get(0);
            hiveCatalog.put(
                    "hive.metastore.uri", MessageFormat.format("thrift://{0}:{1}", hiveMetastore, metastorePort));
        } else {
            return new HashMap<>();
        }
        return hiveCatalog;
    }
}
