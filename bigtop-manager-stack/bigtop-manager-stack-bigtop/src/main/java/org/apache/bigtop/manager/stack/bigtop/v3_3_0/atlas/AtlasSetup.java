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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.atlas;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxFileUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AtlasSetup {
    public static ShellResult configure(Params params) {
        log.info("Configuring Atlas");
        AtlasParams atlasParams = (AtlasParams) params;

        String confDir = atlasParams.confDir();
        String user = atlasParams.user();
        String group = atlasParams.group();
        
        // Call atlasEnv() to initialize all dependencies first
        Map<String, Object> atlasEnv = atlasParams.atlasEnv();
        // Call applicationProperties() to populate global params
        atlasParams.applicationProperties();

        // Create log and pid directories
        LinuxFileUtils.createDirectories(atlasParams.getAtlasLogDir(), user, group, Constants.PERMISSION_755, true);
        LinuxFileUtils.createDirectories(atlasParams.getAtlasPidDir(), user, group, Constants.PERMISSION_755, true);

        // Render atlas-env.sh
        Map<String, Object> additional = new HashMap<>();
        additional.put("zk_quorum", atlasEnv.get("zk_quorum"));
        additional.put("host", atlasParams.hostname());

        LinuxFileUtils.toFileByTemplate(
                atlasEnv.get("content").toString(),
                MessageFormat.format("{0}/atlas-env.sh", atlasParams.serviceHome() + "/bin"),
                user,
                group,
                Constants.PERMISSION_755,
                atlasParams.getGlobalParamsMap(),
                additional);

        // Render atlas-application.properties
        Map<String, Object> appProps = LocalSettings.configurations(atlasParams.getServiceName(), "application-properties");
        LinuxFileUtils.toFileByTemplate(
                appProps.get("content").toString(),
                MessageFormat.format("{0}/atlas-application.properties", confDir),
                user,
                group,
                Constants.PERMISSION_755,
                atlasParams.getGlobalParamsMap(),
                additional);

        log.info("Successfully configured Atlas");
        return ShellResult.success();
    }
}
