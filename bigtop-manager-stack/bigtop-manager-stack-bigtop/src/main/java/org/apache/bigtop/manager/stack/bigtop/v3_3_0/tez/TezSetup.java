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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.tez;

import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.enums.ConfigType;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxFileUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

import static org.apache.bigtop.manager.common.constants.Constants.PERMISSION_755;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TezSetup {

    public static ShellResult configure(Params params) {
        log.info("Configuring Tez");
        TezParams tezParams = (TezParams) params;

        String confDir = tezParams.confDir();
        String hadoopUser = LocalSettings.users().get("hadoop");
        String tezUser = tezParams.user();
        String tezGroup = tezParams.group();

        LinuxFileUtils.createDirectories(confDir, tezUser, tezGroup, Constants.PERMISSION_755, true);

        LinuxFileUtils.toFile(
                ConfigType.XML,
                MessageFormat.format("{0}/tez-site.xml", confDir),
                tezUser,
                tezGroup,
                Constants.PERMISSION_644,
                tezParams.tezSite(),
                tezParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                tezParams.getTezEnvContent(),
                MessageFormat.format("{0}/tez-env.sh", confDir),
                tezUser,
                tezGroup,
                PERMISSION_755,
                tezParams.getGlobalParamsMap());

        //        HdfsUtil.createDirectory(hadoopUser, "/apps");
        //        HdfsUtil.uploadFile(tezUser, tezParams.serviceHome() + "/lib/tez.tar.gz", "/apps/tez");

        log.info("Successfully configured Tez");
        return ShellResult.success();
    }
}
