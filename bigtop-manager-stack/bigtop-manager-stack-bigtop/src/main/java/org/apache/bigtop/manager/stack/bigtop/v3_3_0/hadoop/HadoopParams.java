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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.hadoop;

import org.apache.bigtop.manager.common.message.entity.payload.CommandPayload;
import org.apache.bigtop.manager.common.utils.Environments;
import org.apache.bigtop.manager.stack.bigtop.param.BigtopParams;
import org.apache.bigtop.manager.stack.core.annotations.GlobalParams;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;

import org.apache.commons.lang3.StringUtils;

import com.google.auto.service.AutoService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
@Slf4j
@AutoService(Params.class)
@NoArgsConstructor
public class HadoopParams extends BigtopParams {

    private final String hadoopLogDir = "/var/log/hadoop";
    private final String hadoopPidDir = "/var/run/hadoop";

    // hadoop-${user}-${component}.pid
    private final String nameNodePidFile = hadoopPidDir + "/hadoop-hadoop-namenode.pid";
    private final String dataNodePidFile = hadoopPidDir + "/hadoop-hadoop-datanode.pid";
    private final String sNameNodePidFile = hadoopPidDir + "/hadoop-hadoop-secondarynamenode.pid";
    private final String journalNodePidFile = hadoopPidDir + "/hadoop-hadoop-journalnode.pid";
    private final String zkfcPidFile = hadoopPidDir + "/hadoop-hadoop-zkfc.pid";
    private final String resourceManagerPidFile = hadoopPidDir + "/hadoop-hadoop-resourcemanager.pid";
    private final String nodeManagerPidFile = hadoopPidDir + "/hadoop-hadoop-nodemanager.pid";
    private final String historyServerPidFile = hadoopPidDir + "/hadoop-hadoop-historyserver.pid";

    private String dfsDataDir;
    private String dfsNameNodeDir;
    private String dfsNameNodeCheckPointDir;
    private String dfsDomainSocketPathPrefix;

    private String nodeManagerLogDir = "/hadoop/yarn/log";
    private String nodeManagerLocalDir = "/hadoop/yarn/local";

    private List<String> nameNodeFormattedDirs;

    public HadoopParams(CommandPayload commandPayload) {
        super(commandPayload);
        globalParamsMap.put("hdfs_user", user());
        globalParamsMap.put("hdfs_group", group());
        globalParamsMap.put("datanode_hosts", LocalSettings.hosts("datanode"));
        globalParamsMap.put("java_home", Environments.getJavaHome());
        globalParamsMap.put("hadoop_home", serviceHome());
        globalParamsMap.put("hadoop_conf_dir", confDir());
        globalParamsMap.put("hadoop_libexec_dir", serviceHome() + "/libexec");
        globalParamsMap.put("exclude_hosts", new ArrayList<>());
    }

    public String hadoopLimits() {
        Map<String, Object> hadoopConf = LocalSettings.configurations(getServiceName(), "hadoop.conf");
        return (String) hadoopConf.get("content");
    }

    public String workers() {
        Map<String, Object> hdfsConf = LocalSettings.configurations(getServiceName(), "workers");
        return (String) hdfsConf.get("content");
    }

    @GlobalParams
    public Map<String, Object> hdfsLog4j() {
        return LocalSettings.configurations(getServiceName(), "hdfs-log4j");
    }

    @GlobalParams
    public Map<String, Object> coreSite() {
        Map<String, Object> coreSite = LocalSettings.configurations(getServiceName(), "core-site");
        List<String> namenodeList = LocalSettings.hosts("namenode");
        if (!namenodeList.isEmpty()) {
            coreSite.put(
                    "fs.defaultFS", ((String) coreSite.get("fs.defaultFS")).replace("localhost", namenodeList.get(0)));
        }
        return coreSite;
    }

    @GlobalParams
    public Map<String, Object> hadoopPolicy() {
        return LocalSettings.configurations(getServiceName(), "hadoop-policy");
    }

    @GlobalParams
    public Map<String, Object> hdfsSite() {
        Map<String, Object> hdfsSite = LocalSettings.configurations(getServiceName(), "hdfs-site");
        List<String> namenodeList = LocalSettings.hosts("namenode");
        if (!namenodeList.isEmpty()) {
            hdfsSite.put(
                    "dfs.namenode.rpc-address",
                    ((String) hdfsSite.get("dfs.namenode.rpc-address")).replace("0.0.0.0", namenodeList.get(0)));
            hdfsSite.put(
                    "dfs.datanode.https.address",
                    ((String) hdfsSite.get("dfs.datanode.https.address")).replace("0.0.0.0", namenodeList.get(0)));
            hdfsSite.put(
                    "dfs.namenode.https-address",
                    ((String) hdfsSite.get("dfs.namenode.https-address")).replace("0.0.0.0", namenodeList.get(0)));
        }

        dfsDataDir = (String) hdfsSite.get("dfs.datanode.data.dir");
        dfsNameNodeDir = (String) hdfsSite.get("dfs.namenode.name.dir");
        nameNodeFormattedDirs = Arrays.stream(dfsNameNodeDir.split(","))
                .map(x -> x + "/namenode-formatted/")
                .toList();

        String dfsDomainSocketPath = (String) hdfsSite.get("dfs.domain.socket.path");
        if (StringUtils.isNotBlank(dfsDomainSocketPath)) {
            dfsDomainSocketPathPrefix = dfsDomainSocketPath.replace("dn._PORT", "");
        }
        dfsNameNodeCheckPointDir = (String) hdfsSite.get("dfs.namenode.checkpoint.dir");
        return hdfsSite;
    }

    @GlobalParams
    public Map<String, Object> yarnLog4j() {
        return LocalSettings.configurations(getServiceName(), "yarn-log4j");
    }

    @GlobalParams
    public Map<String, Object> yarnSite() {
        Map<String, Object> yarnSite = LocalSettings.configurations(getServiceName(), "yarn-site");
        List<String> resourcemanagerList = LocalSettings.hosts("resourcemanager");
        if (!resourcemanagerList.isEmpty()) {
            yarnSite.put("yarn.resourcemanager.hostname", MessageFormat.format("{0}", resourcemanagerList.get(0)));
        }

        nodeManagerLogDir = (String) yarnSite.get("yarn.nodemanager.log-dirs");
        nodeManagerLocalDir = (String) yarnSite.get("yarn.nodemanager.local-dirs");
        return yarnSite;
    }

    @GlobalParams
    public Map<String, Object> mapredSite() {
        return LocalSettings.configurations(getServiceName(), "mapred-site");
    }

    @GlobalParams
    public Map<String, Object> hadoopEnv() {
        Map<String, Object> configurations = LocalSettings.configurations(getServiceName(), "hadoop-env");
        configurations.put("hadoop_log_dir", hadoopLogDir);
        configurations.put("hadoop_pid_dir", hadoopPidDir);
        return configurations;
    }

    @GlobalParams
    public Map<String, Object> yarnEnv() {
        return LocalSettings.configurations(getServiceName(), "yarn-env");
    }

    @GlobalParams
    public Map<String, Object> mapredEnv() {
        return LocalSettings.configurations(getServiceName(), "mapred-env");
    }

    @Override
    public String confDir() {
        return serviceHome() + "/etc/hadoop";
    }

    public String binDir() {
        return serviceHome() + "/bin";
    }

    @Override
    public String getServiceName() {
        return "hadoop";
    }
}
