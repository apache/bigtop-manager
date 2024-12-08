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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.zookeeper;

import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.common.utils.NetUtils;
import org.apache.bigtop.manager.stack.core.enums.ConfigType;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxFileUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ZookeeperSetup {

    public static ShellResult configure(Params params) {
        log.info("Configuring ZooKeeper");
        ZookeeperParams zookeeperParams = (ZookeeperParams) params;

        String confDir = zookeeperParams.confDir();
        String zookeeperUser = zookeeperParams.user();
        String zookeeperGroup = zookeeperParams.group();
        Map<String, Object> zookeeperEnv = zookeeperParams.zookeeperEnv();
        Map<String, Object> zooCfg = zookeeperParams.zooCfg();
        List<String> zkHostList = LocalSettings.hosts("zookeeper_server");

        LinuxFileUtils.createDirectories(
                zookeeperParams.getZookeeperDataDir(), zookeeperUser, zookeeperGroup, Constants.PERMISSION_755, true);
        LinuxFileUtils.createDirectories(
                zookeeperParams.getZookeeperLogDir(), zookeeperUser, zookeeperGroup, Constants.PERMISSION_755, true);
        LinuxFileUtils.createDirectories(
                zookeeperParams.getZookeeperPidDir(), zookeeperUser, zookeeperGroup, Constants.PERMISSION_755, true);

        // server.${host?index+1}=${host}:2888:3888
        zkHostList.sort(String::compareToIgnoreCase);
        StringBuilder zkServerStr = new StringBuilder();
        for (String zkHost : zkHostList) {
            zkServerStr
                    .append(MessageFormat.format("server.{0}={1}:2888:3888", zkHostList.indexOf(zkHost) + 1, zkHost))
                    .append("\n");
        }

        LinuxFileUtils.toFile(
                ConfigType.CONTENT,
                MessageFormat.format("{0}/myid", zookeeperParams.getZookeeperDataDir()),
                zookeeperUser,
                zookeeperGroup,
                Constants.PERMISSION_644,
                zkHostList.indexOf(NetUtils.getHostname()) + 1 + "");

        HashMap<String, Object> map = new HashMap<>(zooCfg);
        map.remove("content");
        Map<String, Object> paramMap = Map.of("zk_server_str", zkServerStr.toString(), "security_enabled", false);
        LinuxFileUtils.toFileByTemplate(
                zooCfg.get("content").toString(),
                MessageFormat.format("{0}/zoo.cfg", confDir),
                zookeeperUser,
                zookeeperGroup,
                Constants.PERMISSION_644,
                Map.of("model", map),
                paramMap);

        LinuxFileUtils.toFileByTemplate(
                zookeeperEnv.get("content").toString(),
                MessageFormat.format("{0}/zookeeper-env.sh", confDir),
                zookeeperUser,
                zookeeperGroup,
                Constants.PERMISSION_644,
                zookeeperParams.getGlobalParamsMap());

        log.info("Successfully configured ZooKeeper");
        return ShellResult.success();
    }
}
