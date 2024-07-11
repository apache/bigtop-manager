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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.hdfs;

import org.apache.bigtop.manager.common.message.entity.payload.CommandPayload;
import org.apache.bigtop.manager.stack.common.annotations.GlobalParams;
import org.apache.bigtop.manager.stack.common.utils.BaseParams;
import org.apache.bigtop.manager.stack.common.utils.LocalSettings;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
@Slf4j
public class HdfsParams extends BaseParams {

    private String hadoopLogDir = "/var/log/hadoop";
    private String hadoopPidDir = "/var/run/hadoop";
    private String dfsDataDir = "/hadoop/dfs/data";
    private String dfsNameNodeDir = "/hadoop/dfs/name";
    private String dfsNameNodeCheckPointDir = "/hadoop/dfs/namesecondary";
    private String dfsDomainSocketPathPrefix = "/var/run/hadoop-hdfs";
    /* pid file */
    private String nameNodePidFile = hadoopPidDir + "/hdfs/hadoop-hdfs-namenode.pid";
    private String dataNodePidFile = hadoopPidDir + "/hdfs/hadoop-hdfs-datanode.pid";
    private String sNameNodePidFile = hadoopPidDir + "/hdfs/hadoop-hdfs-secondarynamenode.pid";
    private String journalNodePidFile = hadoopPidDir + "/hdfs/hadoop-hdfs-journalnode.pid";
    private String zkfcPidFile = hadoopPidDir + "/hdfs/hadoop-hdfs-zkfc.pid";
    /* pid file */
    private List<String> nameNodeFormattedDirs;

    public HdfsParams(CommandPayload commandPayload) {
        super(commandPayload);
        globalParamsMap.put("hdfs_user", user());
        globalParamsMap.put("hdfs_group", group());
        globalParamsMap.put("datanode_hosts", LocalSettings.hosts("datanode"));
        globalParamsMap.put("java_home", "/usr/local/java");
        globalParamsMap.put("hadoop_home", serviceHome());
        globalParamsMap.put("hadoop_hdfs_home", hdfsHome());
        globalParamsMap.put("hadoop_conf_dir", confDir());
        globalParamsMap.put("hadoop_libexec_dir", serviceHome() + "/libexec");
    }

    public String hdfsLimits() {
        Map<String, Object> hdfsConf = LocalSettings.configurations(serviceName(), "hdfs.conf");
        return (String) hdfsConf.get("content");
    }

    public String workers() {
        Map<String, Object> hdfsConf = LocalSettings.configurations(serviceName(), "workers");
        return (String) hdfsConf.get("content");
    }

    @GlobalParams
    public Map<String, Object> hdfsLog4j() {
        return LocalSettings.configurations(serviceName(), "hdfs-log4j");
    }

    @GlobalParams
    public Map<String, Object> coreSite() {
        Map<String, Object> coreSite = LocalSettings.configurations(serviceName(), "core-site");
        List<String> namenodeList = LocalSettings.hosts("namenode");
        if (!namenodeList.isEmpty()) {
            coreSite.put("fs.defaultFS", MessageFormat.format("hdfs://{0}:8020", namenodeList.get(0)));
        }
        return coreSite;
    }

    @GlobalParams
    public Map<String, Object> hadoopPolicy() {
        return LocalSettings.configurations(serviceName(), "hadoop-policy");
    }

    @GlobalParams
    public Map<String, Object> hdfsSite() {
        Map<String, Object> hdfsSite = LocalSettings.configurations(serviceName(), "hdfs-site");
        List<String> namenodeList = LocalSettings.hosts("namenode");
        if (!namenodeList.isEmpty()) {
            hdfsSite.put("dfs.namenode.rpc-address", MessageFormat.format("{0}:8020", namenodeList.get(0)));
            hdfsSite.put("dfs.namenode.http-address", MessageFormat.format("{0}:50070", namenodeList.get(0)));
            hdfsSite.put("dfs.namenode.https-address", MessageFormat.format("{0}:50470", namenodeList.get(0)));
        }
        List<String> snamenodeList = LocalSettings.hosts("secondary_namenode");
        if (!snamenodeList.isEmpty()) {
            hdfsSite.put("dfs.namenode.secondary.http-address", MessageFormat.format("{0}:50090", snamenodeList.get(0)));
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
    public Map<String, Object> hadoopEnv() {
        Map<String, Object> hadoopEnv = LocalSettings.configurations(serviceName(), "hadoop-env");

        hadoopLogDir = (String) hadoopEnv.get("hadoop_log_dir_prefix");
        hadoopPidDir = (String) hadoopEnv.get("hadoop_pid_dir_prefix");
        nameNodePidFile = MessageFormat.format("{0}/{1}/hadoop-{1}-namenode.pid", hadoopPidDir, user());
        dataNodePidFile = MessageFormat.format("{0}/{1}/hadoop-{1}-datanode.pid", hadoopPidDir, user());
        sNameNodePidFile = MessageFormat.format("{0}/{1}/hadoop-{1}-secondarynamenode.pid", hadoopPidDir, user());
        journalNodePidFile = MessageFormat.format("{0}/{1}/hadoop-{1}-journalnode.pid", hadoopPidDir, user());
        zkfcPidFile = MessageFormat.format("{0}/{1}/hadoop-{1}-zkfc.pid", hadoopPidDir, user());
        return hadoopEnv;
    }

    @Override
    public String confDir() {
        return "/etc/hadoop/conf";
    }

    @Override
    public String serviceHome() {
        return stackLibDir() + "/hadoop";
    }

    public String hdfsHome() {
        return stackLibDir() + "/hadoop-hdfs";
    }

    public String hdfsExec() {
        return stackBinDir() + "/hdfs";
    }
}
