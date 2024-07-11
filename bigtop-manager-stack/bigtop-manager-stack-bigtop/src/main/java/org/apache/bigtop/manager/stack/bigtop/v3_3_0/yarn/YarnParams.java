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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.yarn;

import org.apache.bigtop.manager.common.message.entity.payload.CommandPayload;
import org.apache.bigtop.manager.stack.common.annotations.GlobalParams;
import org.apache.bigtop.manager.stack.common.utils.BaseParams;
import org.apache.bigtop.manager.stack.common.utils.LocalSettings;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

@Getter
@Slf4j
public class YarnParams extends BaseParams {

    private String yarnLogDir = "/var/log/hadoop-yarn";
    private String yarnPidDir = "/var/run/hadoop-yarn";
    private String rmNodesExcludeDir = "/etc/hadoop/conf/yarn.exclude";
    private String tmpDir = "/tmp/hadoop-yarn";
    private String nodemanagerLogDir = "/hadoop/yarn/log";
    private String nodemanagerLocalDir = "/hadoop/yarn/local";
    /* pid file */
    private String resourceManagerPidFile = yarnPidDir + "/yarn/hadoop-yarn-resourcemanager.pid";
    private String nodeManagerPidFile = yarnPidDir + "/yarn/hadoop-yarn-nodemanager.pid";
    /* pid file */
    private List<String> excludeHosts = List.of();

    public YarnParams(CommandPayload commandPayload) {
        super(commandPayload);
        globalParamsMap.put("yarn_user", user());
        globalParamsMap.put("yarn_group", group());
        globalParamsMap.put("java_home", "/usr/local/java");
        globalParamsMap.put("hadoop_home", serviceHome());
        globalParamsMap.put("hadoop_hdfs_home", hdfsHome());
        globalParamsMap.put("hadoop_yarn_home", yarnHome());
        globalParamsMap.put("hadoop_mapred_home", mapredHome());
        globalParamsMap.put("hadoop_conf_dir", confDir());
        globalParamsMap.put("hadoop_libexec_dir", serviceHome() + "/libexec");
        globalParamsMap.put("exclude_hosts", excludeHosts);
    }

    public String yarnLimits() {
        Map<String, Object> yarnConf = LocalSettings.configurations(serviceName(), "yarn.conf");
        return (String) yarnConf.get("content");
    }

    public String excludeNodesContent() {
        Map<String, Object> excludeNodesContent = LocalSettings.configurations(serviceName(), "exclude-nodes");
        return (String) excludeNodesContent.get("content");
    }

    @GlobalParams
    public Map<String, Object> yarnLog4j() {
        return LocalSettings.configurations(serviceName(), "yarn-log4j");
    }

    @GlobalParams
    public Map<String, Object> yarnSite() {
        Map<String, Object> yarnSite = LocalSettings.configurations(serviceName(), "yarn-site");
        List<String> resourcemanagerList = LocalSettings.hosts("resourcemanager");
        if (!resourcemanagerList.isEmpty()) {
            yarnSite.put("yarn.resourcemanager.hostname", MessageFormat.format("{0}", resourcemanagerList.get(0)));
            yarnSite.put("yarn.resourcemanager.resource-tracker.address", MessageFormat.format("{0}:8025", resourcemanagerList.get(0)));
            yarnSite.put("yarn.resourcemanager.address", MessageFormat.format("{0}:8050", resourcemanagerList.get(0)));
            yarnSite.put("yarn.resourcemanager.admin.address", MessageFormat.format("{0}:8141", resourcemanagerList.get(0)));
            yarnSite.put("yarn.resourcemanager.webapp.address", MessageFormat.format("{0}:8088", resourcemanagerList.get(0)));
            yarnSite.put("yarn.resourcemanager.webapp.https.address", MessageFormat.format("{0}:8090", resourcemanagerList.get(0)));
        }

        rmNodesExcludeDir = (String) yarnSite.get("yarn.resourcemanager.nodes.exclude-path");
        nodemanagerLogDir = (String) yarnSite.get("yarn.nodemanager.log-dirs");
        nodemanagerLocalDir = (String) yarnSite.get("yarn.nodemanager.local-dirs");
        return yarnSite;
    }

    @GlobalParams
    public Map<String, Object> yarnEnv() {
        Map<String, Object> yarnEnv = LocalSettings.configurations(serviceName(), "yarn-env");

        tmpDir = (String) yarnEnv.get("hadoop_java_io_tmpdir");
        yarnLogDir = (String) yarnEnv.get("yarn_log_dir_prefix");
        yarnPidDir = (String) yarnEnv.get("yarn_pid_dir_prefix");
        resourceManagerPidFile = MessageFormat.format("{0}/{1}/hadoop-{1}-resourcemanager.pid", yarnPidDir, user());
        nodeManagerPidFile = MessageFormat.format("{0}/{1}/hadoop-{1}-nodemanager.pid", yarnPidDir, user());
        return yarnEnv;
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

    public String yarnExec() {
        return stackBinDir() + "/yarn";
    }

    public String yarnHome() {
        return stackLibDir() + "/hadoop-yarn";
    }

    public String mapredHome() {
        return stackLibDir() + "/hadoop-mapreduce";
    }
}
