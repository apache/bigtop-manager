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

import org.apache.bigtop.manager.common.message.entity.payload.CommandPayload;
import org.apache.bigtop.manager.common.utils.Environments;
import org.apache.bigtop.manager.stack.core.annotations.GlobalParams;
import org.apache.bigtop.manager.stack.core.param.BaseParams;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;

import lombok.Getter;

import java.util.Map;

@Getter
public class ZookeeperParams extends BaseParams {

    private String zookeeperLogDir = "/var/log/zookeeper";
    private String zookeeperPidDir = "/var/run/zookeeper";
    private String zookeeperDataDir = "/hadoop/zookeeper";
    private String zookeeperPidFile = zookeeperPidDir + "/zookeeper_server.pid";

    public ZookeeperParams(CommandPayload commandPayload) {
        super(commandPayload);
        globalParamsMap.put("java_home", Environments.getJavaHome());
        globalParamsMap.put("zookeeper_home", serviceHome());
        globalParamsMap.put("security_enabled", false);
        globalParamsMap.put("zookeeper_pid_file", zookeeperPidFile);
    }

    @GlobalParams
    public Map<String, Object> zooCfg() {
        Map<String, Object> zooCfg = LocalSettings.configurations(serviceName(), "zoo.cfg");
        zookeeperDataDir = (String) zooCfg.get("dataDir");
        return zooCfg;
    }

    @GlobalParams
    public Map<String, Object> zookeeperEnv() {
        Map<String, Object> zookeeperEnv = LocalSettings.configurations(serviceName(), "zookeeper-env");
        zookeeperLogDir = (String) zookeeperEnv.get("zookeeper_log_dir");
        zookeeperPidDir = (String) zookeeperEnv.get("zookeeper_pid_dir");
        zookeeperPidFile = zookeeperPidDir + "/zookeeper_server.pid";
        return zookeeperEnv;
    }
}
