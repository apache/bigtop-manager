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

import org.apache.bigtop.manager.grpc.payload.ComponentCommandPayload;
import org.apache.bigtop.manager.stack.bigtop.param.BigtopParams;
import org.apache.bigtop.manager.stack.core.annotations.GlobalParams;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;

import com.google.auto.service.AutoService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Map;

@Getter
@Slf4j
@AutoService(Params.class)
@NoArgsConstructor
public class FlinkParams extends BigtopParams {

    private String flinkLogDir;
    private String flinkPidDir;
    private String historyServerPidFile;
    private String flinkConfContent;
    private String flinkLog4jPropertiesContent;
    private String flinkLog4jCLiPropertiesContent;
    private String flinkLog4jConsolePropertiesContent;
    private String flinkLog4jSessionPropertiesContent;

    public FlinkParams(ComponentCommandPayload componentCommandPayload) {
        super(componentCommandPayload);
        globalParamsMap.put("flink_user", user());
        globalParamsMap.put("flink_group", group());
        globalParamsMap.put("java_home", javaHome());
        globalParamsMap.put("hadoop_home", hadoopHome());
        globalParamsMap.put("hadoop_conf_dir", hadoopConfDir());
    }

    @GlobalParams
    public Map<String, Object> flinkConf() {
        Map<String, Object> configurations = LocalSettings.configurations(getServiceName(), "flink-conf");
        flinkConfContent = (String) configurations.get("content");
        return configurations;
    }

    @GlobalParams
    public Map<String, Object> flinkEnv() {
        Map<String, Object> configurations = LocalSettings.configurations(getServiceName(), "flink-env");
        flinkLogDir = (String) configurations.get("flink_log_dir");
        flinkPidDir = (String) configurations.get("flink_pid_dir");
        historyServerPidFile = MessageFormat.format("{0}/flink-{1}-historyserver.pid", flinkPidDir, user());
        return configurations;
    }

    @GlobalParams
    public Map<String, Object> flinkLog4jProperties() {
        Map<String, Object> configurations = LocalSettings.configurations(getServiceName(), "flink-log4j-properties");
        flinkLog4jPropertiesContent = (String) configurations.get("content");
        return configurations;
    }

    @GlobalParams
    public Map<String, Object> flinkLog4jCLiProperties() {
        Map<String, Object> configurations =
                LocalSettings.configurations(getServiceName(), "flink-log4j-cli-properties");
        flinkLog4jCLiPropertiesContent = (String) configurations.get("content");
        return configurations;
    }

    @GlobalParams
    public Map<String, Object> flinkLog4jConsoleProperties() {
        Map<String, Object> configurations =
                LocalSettings.configurations(getServiceName(), "flink-log4j-console-properties");
        flinkLog4jConsolePropertiesContent = (String) configurations.get("content");
        return configurations;
    }

    @GlobalParams
    public Map<String, Object> flinkLog4jSessionProperties() {
        Map<String, Object> configurations =
                LocalSettings.configurations(getServiceName(), "flink-log4j-session-properties");
        flinkLog4jSessionPropertiesContent = (String) configurations.get("content");
        return configurations;
    }

    public String hadoopConfDir() {
        return hadoopHome() + "/etc/hadoop";
    }

    public String hadoopHome() {
        return stackHome() + "/hadoop";
    }

    @Override
    public String getServiceName() {
        return "flink";
    }
}
