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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.hive;

import org.apache.bigtop.manager.grpc.payload.ComponentCommandPayload;
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
public class HiveParams extends BigtopParams {

    private String hiveLogDir = "/var/log/hive";
    private String hivePidDir = "/var/run/hive";

    private String hiveserver2PidFile;
    private String hiveMetastorePidFile;

    private Integer metastorePort;
    private String hiveEnvContent;
    private String hiveLog4j2Content;
    private String beelineLog4j2Content;
    private String hiveExecLog4j2Content;
    private String llapCliLog4j2Content;
    private String llapDaemonLog4j2Content;

    private final String hiveShellContent =
            "dir=$(dirname $0)\n$dir/hive --service $1 > /dev/null 2>&1 &\necho $! > $2";

    public HiveParams(ComponentCommandPayload componentCommandPayload) {
        super(componentCommandPayload);
        globalParamsMap.put("java_home", javaHome());
        globalParamsMap.put("hadoop_home", hadoopHome());
        globalParamsMap.put("hive_home", serviceHome());
        globalParamsMap.put("hive_conf_dir", confDir());
        globalParamsMap.put("security_enabled", false);
        globalParamsMap.put("hive_user", user());
        globalParamsMap.put("hive_group", group());

        hiveserver2PidFile = hivePidDir + "/hiveserver2.pid";
        hiveMetastorePidFile = hivePidDir + "/hive-metastore.pid";
    }

    public String hiveLimits() {
        Map<String, Object> hiveConf = LocalSettings.configurations(getServiceName(), "hive.conf");
        return (String) hiveConf.get("content");
    }

    @GlobalParams
    public Map<String, Object> hiveSite() {
        Map<String, Object> configurations = LocalSettings.configurations(getServiceName(), "hive-site");
        String metastoreUris = configurations.get("hive.metastore.uris").toString();

        String[] split = metastoreUris.split(":");
        metastorePort = Integer.parseInt(split[split.length - 1]);
        globalParamsMap.put("hive_metastore_port", metastorePort);

        // Auto generate zookeeper properties for hive-site.xml
        Map<String, Object> zooCfg = LocalSettings.configurations("zookeeper", "zoo.cfg");
        List<String> zookeeperQuorum = LocalSettings.hosts("zookeeper_server");

        configurations.put("hive.zookeeper.client.port", zooCfg.get("clientPort"));
        configurations.put("hive.zookeeper.quorum", String.join(",", zookeeperQuorum));

        // Auto generate database properties for hive-site.xml
        String mysqlHost = LocalSettings.hosts("mysql_server").get(0);
        String mysqlPassword = LocalSettings.configurations("mysql", "common")
                .get("root_password")
                .toString();
        String clusterName = LocalSettings.cluster().getName();
        configurations.put("hive.metastore.db.type", "mysql");
        configurations.put(
                "javax.jdo.option.ConnectionURL",
                "jdbc:mysql://" + mysqlHost + ":3306/" + clusterName
                        + "_hive?createDatabaseIfNotExist=true&amp;useSSL=false&amp;allowPublicKeyRetrieval=true");
        configurations.put("javax.jdo.option.ConnectionDriverName", "com.mysql.cj.jdbc.Driver");
        configurations.put("javax.jdo.option.ConnectionUserName", "root");
        configurations.put("javax.jdo.option.ConnectionPassword", mysqlPassword);
        return configurations;
    }

    @GlobalParams
    public Map<String, Object> hiveEnv() {
        Map<String, Object> hiveEnv = LocalSettings.configurations(getServiceName(), "hive-env");
        hivePidDir = (String) hiveEnv.get("hive_pid_dir");
        hiveLogDir = (String) hiveEnv.get("hive_log_dir");
        hiveEnvContent = (String) hiveEnv.get("content");
        return hiveEnv;
    }

    @GlobalParams
    public Map<String, Object> hiveLog4j2() {
        Map<String, Object> configurations = LocalSettings.configurations(getServiceName(), "hive-log4j2");
        hiveLog4j2Content = (String) configurations.get("content");
        return configurations;
    }

    @GlobalParams
    public Map<String, Object> beelineLog4j2() {
        Map<String, Object> configurations = LocalSettings.configurations(getServiceName(), "beeline-log4j2");
        beelineLog4j2Content = (String) configurations.get("content");
        return configurations;
    }

    @GlobalParams
    public Map<String, Object> hiveExecLog4j2() {
        Map<String, Object> configurations = LocalSettings.configurations(getServiceName(), "hive-exec-log4j2");
        hiveExecLog4j2Content = (String) configurations.get("content");
        return configurations;
    }

    @GlobalParams
    public Map<String, Object> llapCliLog4j2() {
        Map<String, Object> configurations = LocalSettings.configurations(getServiceName(), "llap-cli-log4j2");
        llapCliLog4j2Content = (String) configurations.get("content");
        return configurations;
    }

    @GlobalParams
    public Map<String, Object> llapDaemonLog4j2() {
        Map<String, Object> configurations = LocalSettings.configurations(getServiceName(), "llap-daemon-log4j2");
        llapDaemonLog4j2Content = (String) configurations.get("content");
        return configurations;
    }

    public String hadoopHome() {
        return stackHome() + "/hadoop";
    }

    @Override
    public String getServiceName() {
        return "hive";
    }
}
