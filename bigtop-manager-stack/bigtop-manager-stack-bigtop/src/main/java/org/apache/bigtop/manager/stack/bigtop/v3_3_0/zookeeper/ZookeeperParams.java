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

import org.apache.bigtop.manager.grpc.payload.ComponentCommandPayload;
import org.apache.bigtop.manager.stack.bigtop.param.BigtopParams;
import org.apache.bigtop.manager.stack.core.annotations.GlobalParams;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;

import com.google.auto.service.AutoService;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@AutoService(Params.class)
@NoArgsConstructor
public class ZookeeperParams extends BigtopParams {

    private final String zookeeperLogDir = "/var/log/zookeeper";
    private final String zookeeperPidDir = "/var/run/zookeeper";
    private final String zookeeperPidFile = zookeeperPidDir + "/zookeeper_server.pid";

    private String zookeeperDataDir;

    public ZookeeperParams(ComponentCommandPayload componentCommandPayload) {
        super(componentCommandPayload);
        globalParamsMap.put("java_home", javaHome());
        globalParamsMap.put("zookeeper_home", serviceHome());
        globalParamsMap.put("security_enabled", false);
        globalParamsMap.put("zookeeper_pid_file", zookeeperPidFile);
    }

    @GlobalParams
    public Map<String, Object> zooCfg() {
        Map<String, Object> zooCfg = LocalSettings.configurations(getServiceName(), "zoo.cfg");
        zookeeperDataDir = (String) zooCfg.get("dataDir");
        return zooCfg;
    }

    @GlobalParams
    public Map<String, Object> zookeeperEnv() {
        return LocalSettings.configurations(getServiceName(), "zookeeper-env");
    }

    @Override
    public String getServiceName() {
        return "zookeeper";
    }
}
