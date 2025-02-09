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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.spark;

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
public class SparkParams extends BigtopParams {

    private String sparkLogDir = "/var/log/spark";
    private String sparkPidDir = "/var/run/spark";
    private String sparkHistoryLogDir;
    private String sparkHistoryServerPidFile;
    private String sparkThriftServerPidFile;

    private String sparkEnvContent;
    private String sparkLog4j2Content;
    private String sparkMetricsContent;
    private String sparkFairSchedulerContent;
    private String sparkDefaultsContent;

    public SparkParams(ComponentCommandPayload componentCommandPayload) {
        super(componentCommandPayload);
        globalParamsMap.put("spark_user", user());
        globalParamsMap.put("spark_group", group());
        globalParamsMap.put("java_home", javaHome());
        globalParamsMap.put("spark_conf_dir", confDir());
        globalParamsMap.put("hadoop_home", hadoopHome());
        globalParamsMap.put("hadoop_conf_dir", hadoopConfDir());
        globalParamsMap.put("hive_home", hiveHome());
        globalParamsMap.put("hive_conf_dir", hiveConfDir());
        globalParamsMap.put("security_enabled", false);
    }

    @GlobalParams
    public Map<String, Object> sparkFairScheduler() {
        Map<String, Object> sparkFairScheduler = LocalSettings.configurations(getServiceName(), "fairscheduler");
        sparkFairSchedulerContent = (String) sparkFairScheduler.get("content");
        return sparkFairScheduler;
    }

    @GlobalParams
    public Map<String, Object> sparkMetrics() {
        Map<String, Object> sparkMetrics = LocalSettings.configurations(getServiceName(), "metrics");
        sparkMetricsContent = (String) sparkMetrics.get("content");
        return sparkMetrics;
    }

    @GlobalParams
    public Map<String, Object> sparkDefaults() {
        Map<String, Object> sparkDefaults = LocalSettings.configurations(getServiceName(), "spark-defaults");
        sparkHistoryLogDir = sparkDefaults.get("spark_history_fs_logDirectory").toString();
        sparkDefaultsContent = (String) sparkDefaults.get("content");
        return sparkDefaults;
    }

    @GlobalParams
    public Map<String, Object> sparkEnv() {
        Map<String, Object> sparkEnv = LocalSettings.configurations(getServiceName(), "spark-env");
        sparkPidDir = sparkEnv.get("spark_pid_dir").toString();
        sparkHistoryServerPidFile =
                sparkPidDir + "/spark-" + user() + "-org.apache.spark.deploy.history.HistoryServer-1.pid";
        sparkThriftServerPidFile =
                sparkPidDir + "/spark-" + user() + "-org.apache.spark.sql.hive.thriftserver.HiveThriftServer2-1.pid";
        sparkLogDir = sparkEnv.get("spark_log_dir").toString();
        sparkEnvContent = sparkEnv.get("content").toString();
        return sparkEnv;
    }

    @GlobalParams
    public Map<String, Object> sparkHiveSite() {
        Map<String, Object> configurations = LocalSettings.configurations(getServiceName(), "spark-hive-site");
        Map<String, Object> hiveSite = LocalSettings.configurations("hive", "hive-site");
        configurations.put("hive.metastore.uris", hiveSite.get("hive.metastore.uris"));
        configurations.put("hive.metastore.warehouse.dir", hiveSite.get("hive.metastore.warehouse.dir"));
        return configurations;
    }

    @GlobalParams
    public Map<String, Object> sparkLog4j2() {
        Map<String, Object> sparkLog4j2 = LocalSettings.configurations(getServiceName(), "log4j2");
        sparkLog4j2Content = (String) sparkLog4j2.get("content");
        return sparkLog4j2;
    }

    public String hadoopConfDir() {
        return hadoopHome() + "/etc/hadoop";
    }

    public String hadoopHome() {
        return stackHome() + "/hadoop";
    }

    public String hiveConfDir() {
        return hiveHome() + "/conf";
    }

    public String hiveHome() {
        return stackHome() + "/hive";
    }

    @Override
    public String getServiceName() {
        return "spark";
    }
}
