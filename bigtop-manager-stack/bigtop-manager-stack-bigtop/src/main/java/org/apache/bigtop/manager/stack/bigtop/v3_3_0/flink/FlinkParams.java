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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.flink;

import org.apache.bigtop.manager.common.message.entity.payload.CommandPayload;
import org.apache.bigtop.manager.common.utils.Environments;
import org.apache.bigtop.manager.stack.core.annotations.GlobalParams;
import org.apache.bigtop.manager.stack.core.param.BaseParams;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Map;

@Getter
@Slf4j
public class FlinkParams extends BaseParams {

    private String flinkLogDir;
    private String flinkPidDir;
    private String historyServerPidFile;
    private String flinkConfContent;
    private String flinkLog4jPropertiesContent;
    private String flinkLog4jCLiPropertiesContent;
    private String flinkLog4jConsolePropertiesContent;
    private String flinkLog4jSessionPropertiesContent;

    private String jobManagerArchiveFsDir;
    private String historyServerWebPort;
    private String historyServerArchiveFsDir;
    private String historyServerArchiveFsRefreshInterval;

    public FlinkParams(CommandPayload commandPayload) {
        super(commandPayload);
        globalParamsMap.put("flink_user", user());
        globalParamsMap.put("flink_group", group());
        globalParamsMap.put("java_home", Environments.getJavaHome());
        globalParamsMap.put("hadoop_home", hadoopHome());
        globalParamsMap.put("hadoop_conf_dir", hadoopConfDir());

        globalParamsMap.put("jobmanager_archive_fs_dir", jobManagerArchiveFsDir);
        globalParamsMap.put("historyserver_web_port", historyServerWebPort);
        globalParamsMap.put("historyserver_archive_fs_dir", historyServerArchiveFsDir);
        globalParamsMap.put("historyserver_archive_fs_refresh_interval", historyServerArchiveFsRefreshInterval);
    }

    @GlobalParams
    public Map<String, Object> flinkConf() {
        Map<String, Object> configurations = LocalSettings.configurations(serviceName(), "flink-conf");
        flinkConfContent = (String) configurations.get("content");

        jobManagerArchiveFsDir = (String) configurations.get("jobmanager.archive.fs.dir");
        historyServerWebPort = (String) configurations.get("historyserver.web.port");
        historyServerArchiveFsDir = (String) configurations.get("historyserver.archive.fs.dir");
        historyServerArchiveFsRefreshInterval =
                (String) configurations.get("historyserver.archive.fs.refresh-interval");
        return configurations;
    }

    @GlobalParams
    public Map<String, Object> flinkEnv() {
        Map<String, Object> configurations = LocalSettings.configurations(serviceName(), "flink-env");
        flinkLogDir = (String) configurations.get("flink_log_dir");
        flinkPidDir = (String) configurations.get("flink_pid_dir");
        historyServerPidFile = MessageFormat.format("{0}/flink-{1}-historyserver.pid", flinkPidDir, user());
        return configurations;
    }

    @GlobalParams
    public Map<String, Object> flinkLog4jProperties() {
        Map<String, Object> configurations = LocalSettings.configurations(serviceName(), "flink-log4j-properties");
        flinkLog4jPropertiesContent = (String) configurations.get("content");
        return configurations;
    }

    @GlobalParams
    public Map<String, Object> flinkLog4jCLiProperties() {
        Map<String, Object> configurations = LocalSettings.configurations(serviceName(), "flink-log4j-cli-properties");
        flinkLog4jCLiPropertiesContent = (String) configurations.get("content");
        return configurations;
    }

    @GlobalParams
    public Map<String, Object> flinkLog4jConsoleProperties() {
        Map<String, Object> configurations =
                LocalSettings.configurations(serviceName(), "flink-log4j-console-properties");
        flinkLog4jConsolePropertiesContent = (String) configurations.get("content");
        return configurations;
    }

    @GlobalParams
    public Map<String, Object> flinkLog4jSessionProperties() {
        Map<String, Object> configurations =
                LocalSettings.configurations(serviceName(), "flink-log4j-session-properties");
        flinkLog4jSessionPropertiesContent = (String) configurations.get("content");
        return configurations;
    }

    @Override
    public String confDir() {
        return "/etc/flink/conf";
    }

    @Override
    public String serviceHome() {
        return stackLibDir() + "/flink";
    }

    public String hadoopConfDir() {
        return "/etc/hadoop/conf";
    }

    public String hadoopHome() {
        return stackLibDir() + "/hadoop";
    }
}
