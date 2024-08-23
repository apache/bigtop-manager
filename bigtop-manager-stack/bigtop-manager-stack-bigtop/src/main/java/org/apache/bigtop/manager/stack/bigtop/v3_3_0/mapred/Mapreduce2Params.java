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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.mapred;

import org.apache.bigtop.manager.common.message.entity.payload.CommandPayload;
import org.apache.bigtop.manager.common.utils.Environments;
import org.apache.bigtop.manager.stack.bigtop.param.BigtopParams;
import org.apache.bigtop.manager.stack.core.annotations.GlobalParams;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Map;

@Getter
@Slf4j
public class Mapreduce2Params extends BigtopParams {

    private String mapredEnvContent;
    private String mapredLogDir;
    private String mapredPidDir;
    private String historyServerPidFile;

    public Mapreduce2Params(CommandPayload commandPayload) {
        super(commandPayload);
        globalParamsMap.put("mapred_user", user());
        globalParamsMap.put("mapred_group", group());
        globalParamsMap.put("java_home", Environments.getJavaHome());
        globalParamsMap.put("hadoop_home", serviceHome());
        globalParamsMap.put("hadoop_conf_dir", confDir());
        globalParamsMap.put("hadoop_hdfs_home", hdfsHome());
        globalParamsMap.put("hadoop_yarn_home", yarnHome());
        globalParamsMap.put("hadoop_mapred_home", mapredHome());
        globalParamsMap.put("hadoop_libexec_dir", serviceHome() + "/libexec");
    }

    public String mapredLimits() {
        Map<String, Object> yarnConf = LocalSettings.configurations(serviceName(), "mapreduce.conf");
        return (String) yarnConf.get("content");
    }

    @GlobalParams
    public Map<String, Object> mapredSite() {
        return LocalSettings.configurations(serviceName(), "mapred-site");
    }

    @GlobalParams
    public Map<String, Object> mapredEnv() {
        Map<String, Object> mapredEnv = LocalSettings.configurations(serviceName(), "mapred-env");
        mapredEnvContent = (String) mapredEnv.get("content");
        mapredLogDir = (String) mapredEnv.get("mapred_log_dir_prefix");
        mapredPidDir = (String) mapredEnv.get("mapred_pid_dir_prefix");
        historyServerPidFile = MessageFormat.format("{0}/{1}/hadoop-{1}-historyserver.pid", mapredPidDir, user());
        return mapredEnv;
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

    public String yarnHome() {
        return stackLibDir() + "/hadoop-yarn";
    }

    public String mapredHome() {
        return stackLibDir() + "/hadoop-mapreduce";
    }

    public String mapredExec() {
        return stackBinDir() + "/mapred";
    }
}
