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

import org.apache.bigtop.manager.common.message.entity.payload.CommandPayload;
import org.apache.bigtop.manager.common.utils.Environments;
import org.apache.bigtop.manager.stack.bigtop.param.BigtopParams;
import org.apache.bigtop.manager.stack.core.annotations.GlobalParams;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;

import com.google.auto.service.AutoService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Getter
@Slf4j
@AutoService(Params.class)
@NoArgsConstructor
public class HBaseParams extends BigtopParams {

    private String hbaseLogDir = "/var/log/hbase";
    private String hbasePidDir = "/var/run/hbase";

    private String hbaseRootDir;
    private String hbaseMasterPidFile;
    private String hbaseRegionServerPidFile;

    private String hbaseEnvContent;
    private String hbaseLog4jContent;

    public HBaseParams(CommandPayload commandPayload) {
        super(commandPayload);
        globalParamsMap.put("java_home", Environments.getJavaHome());
        globalParamsMap.put("hbase_home", serviceHome());
        globalParamsMap.put("hbase_conf_dir", confDir());
        globalParamsMap.put("security_enabled", false);
        globalParamsMap.put("hbase_user", user());
        globalParamsMap.put("hbase_group", group());
        globalParamsMap.put("regionserver_hosts", LocalSettings.hosts("hbase_regionserver"));

        hbaseMasterPidFile = hbasePidDir + "/hbase-" + user() + "-master.pid";
        hbaseRegionServerPidFile = hbasePidDir + "/hbase-" + user() + "-regionserver.pid";
    }

    public String hbaseLimits() {
        Map<String, Object> hbaseConf = LocalSettings.configurations(getServiceName(), "hbase.conf");
        return (String) hbaseConf.get("content");
    }

    public String regionservers() {
        Map<String, Object> hdfsConf = LocalSettings.configurations(getServiceName(), "regionservers");
        return (String) hdfsConf.get("content");
    }

    @GlobalParams
    public Map<String, Object> hbaseSite() {
        Map<String, Object> configurations = LocalSettings.configurations(getServiceName(), "hbase-site");
        List<String> zookeeperQuorum = LocalSettings.hosts("zookeeper_server");
        Map<String, Object> zooCfg = LocalSettings.configurations("zookeeper", "zoo.cfg");

        // Auto generate properties for hbase-site.xml
        configurations.put("hbase.zookeeper.property.clientPort", zooCfg.get("clientPort"));
        configurations.put("hbase.zookeeper.quorum", String.join(",", zookeeperQuorum));

        hbaseRootDir = (String) configurations.get("hbase.rootdir");
        return configurations;
    }

    @GlobalParams
    public Map<String, Object> hbaseEnv() {
        Map<String, Object> hbaseEnv = LocalSettings.configurations(getServiceName(), "hbase-env");
        hbasePidDir = (String) hbaseEnv.get("hbase_pid_dir");
        hbaseLogDir = (String) hbaseEnv.get("hbase_log_dir");
        hbaseEnvContent = (String) hbaseEnv.get("content");
        return hbaseEnv;
    }

    @GlobalParams
    public Map<String, Object> hbaseLog4j() {
        Map<String, Object> configurations = LocalSettings.configurations(getServiceName(), "hbase-log4j");
        hbaseLog4jContent = (String) configurations.get("content");
        return configurations;
    }

    @GlobalParams
    public Map<String, Object> hbasePolicy() {
        return LocalSettings.configurations(getServiceName(), "hbase-policy");
    }

    @Override
    public String getServiceName() {
        return "hbase";
    }
}
