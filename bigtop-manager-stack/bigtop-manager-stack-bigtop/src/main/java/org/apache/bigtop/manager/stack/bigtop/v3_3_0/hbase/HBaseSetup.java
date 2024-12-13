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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.hbase;

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
public class HBaseSetup {

    public static ShellResult configure(Params params) {
        log.info("Configuring HBase");
        HBaseParams hbaseParams = (HBaseParams) params;

        String confDir = hbaseParams.confDir();
        String hbaseUser = hbaseParams.user();
        String hbaseGroup = hbaseParams.group();

        LinuxFileUtils.createDirectories(hbaseParams.getHbaseLogDir(), hbaseUser, hbaseGroup, PERMISSION_755, true);
        LinuxFileUtils.createDirectories(hbaseParams.getHbasePidDir(), hbaseUser, hbaseGroup, PERMISSION_755, true);
        LinuxFileUtils.createDirectories(hbaseParams.getHbaseRootDir(), hbaseUser, hbaseGroup, PERMISSION_755, true);

        LinuxFileUtils.toFileByTemplate(
                hbaseParams.hbaseLimits(),
                MessageFormat.format("{0}/hbase.conf", HadoopParams.LIMITS_CONF_DIR),
                Constants.ROOT_USER,
                Constants.ROOT_USER,
                Constants.PERMISSION_644,
                hbaseParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                hbaseParams.getHbaseEnvContent(),
                MessageFormat.format("{0}/hbase-env.sh", confDir),
                hbaseUser,
                hbaseGroup,
                Constants.PERMISSION_644,
                hbaseParams.getGlobalParamsMap());

        LinuxFileUtils.toFile(
                ConfigType.XML,
                MessageFormat.format("{0}/hbase-site.xml", confDir),
                hbaseUser,
                hbaseGroup,
                Constants.PERMISSION_644,
                hbaseParams.hbaseSite());

        LinuxFileUtils.toFile(
                ConfigType.XML,
                MessageFormat.format("{0}/hbase-policy.xml", confDir),
                hbaseUser,
                hbaseGroup,
                Constants.PERMISSION_644,
                hbaseParams.hbasePolicy());

        LinuxFileUtils.toFileByTemplate(
                hbaseParams.getHbaseLog4jContent(),
                MessageFormat.format("{0}/log4j.properties", confDir),
                hbaseUser,
                hbaseGroup,
                Constants.PERMISSION_644,
                hbaseParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                hbaseParams.regionservers(),
                MessageFormat.format("{0}/regionservers", confDir),
                hbaseUser,
                hbaseGroup,
                Constants.PERMISSION_644,
                hbaseParams.getGlobalParamsMap());

        log.info("Successfully configured HBase");
        return ShellResult.success();
    }
}
