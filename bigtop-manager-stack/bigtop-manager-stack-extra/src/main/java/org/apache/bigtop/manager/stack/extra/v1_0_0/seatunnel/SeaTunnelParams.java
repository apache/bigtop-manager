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
package org.apache.bigtop.manager.stack.extra.v1_0_0.seatunnel;

import org.apache.bigtop.manager.grpc.payload.ComponentCommandPayload;
import org.apache.bigtop.manager.stack.core.annotations.GlobalParams;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;
import org.apache.bigtop.manager.stack.extra.param.ExtraParams;

import com.google.auto.service.AutoService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Getter
@NoArgsConstructor
@AutoService(Params.class)
public class SeaTunnelParams extends ExtraParams {

    private String seatunnelContent;
    private String seatunnelEnvContent;
    private String seatunnelMasterPort;
    private String seatunnelMasterContent;
    private String seatunnelWorkerPort;
    private String seatunnelWorkerContent;
    private String seatunnelClientContent;
    private String jvmMasterOptionsContent;
    private String jvmWorkerOptionsContent;
    private String jvmClientOptionsContent;
    private String log4j2Content;
    private String log4j2ClientContent;

    public SeaTunnelParams(ComponentCommandPayload payload) {
        super(payload);
        globalParamsMap.put("java_home", javaHome());
        globalParamsMap.put("seatunnel_user", user());
        globalParamsMap.put("seatunnel_group", group());
        globalParamsMap.put("seatunnel_home", serviceHome());
        globalParamsMap.put("seatunnel_conf_dir", confDir());
        globalParamsMap.put("spark_home", sparkHome());
        globalParamsMap.put("flink_home", flinkHome());
    }

    @Override
    public String getServiceName() {
        return "seatunnel";
    }

    @Override
    public String confDir() {
        return serviceHome() + "/config";
    }

    public String sparkHome() {
        return stackHome() + "/spark";
    }

    public String flinkHome() {
        return stackHome() + "/flink";
    }

    @GlobalParams
    public Map<String, Object> seatunnel() {
        Map<String, Object> configuration = LocalSettings.configurations(getServiceName(), "seatunnel.yaml");
        seatunnelContent = configuration.get("content").toString();
        globalParamsMap.put(
                "seatunnel_cluster_name",
                configuration.get("seatunnel_cluster_name").toString());
        return configuration;
    }

    @GlobalParams
    public Map<String, Object> seatunnelEnv() {
        Map<String, Object> configuration = LocalSettings.configurations(getServiceName(), "seatunnel-env.sh");
        seatunnelEnvContent = configuration.get("content").toString();
        return configuration;
    }

    @GlobalParams
    public Map<String, Object> seatunnelMaster() {
        Map<String, Object> configuration = LocalSettings.configurations(getServiceName(), "hazelcast-master.yaml");
        seatunnelMasterPort = configuration.get("seatunnel_master_port").toString();
        seatunnelMasterContent = configuration.get("content").toString();
        return configuration;
    }

    @GlobalParams
    public Map<String, Object> seatunnelWorker() {
        Map<String, Object> configuration = LocalSettings.configurations(getServiceName(), "hazelcast-worker.yaml");
        seatunnelWorkerPort = configuration.get("seatunnel_worker_port").toString();
        seatunnelWorkerContent = configuration.get("content").toString();
        return configuration;
    }

    @GlobalParams
    public Map<String, Object> seatunnelClient() {
        Map<String, Object> configuration = LocalSettings.configurations(getServiceName(), "hazelcast-client.yaml");
        seatunnelClientContent = configuration.get("content").toString();
        return configuration;
    }

    @GlobalParams
    public Map<String, Object> jvmMasterOptions() {
        Map<String, Object> configuration = LocalSettings.configurations(getServiceName(), "jvm_master_options");
        jvmMasterOptionsContent = configuration.get("content").toString();
        return configuration;
    }

    @GlobalParams
    public Map<String, Object> jvmWorkerOptions() {
        Map<String, Object> configuration = LocalSettings.configurations(getServiceName(), "jvm_worker_options");
        jvmWorkerOptionsContent = configuration.get("content").toString();
        return configuration;
    }

    @GlobalParams
    public Map<String, Object> jvmClientOptions() {
        Map<String, Object> configuration = LocalSettings.configurations(getServiceName(), "jvm_client_options");
        jvmClientOptionsContent = configuration.get("content").toString();
        return configuration;
    }

    @GlobalParams
    public Map<String, Object> log4j2() {
        Map<String, Object> configuration = LocalSettings.configurations(getServiceName(), "log4j2.properties");
        log4j2Content = configuration.get("content").toString();
        return configuration;
    }

    @GlobalParams
    public Map<String, Object> log4j2Client() {
        Map<String, Object> configuration = LocalSettings.configurations(getServiceName(), "log4j2_client.properties");
        log4j2ClientContent = configuration.get("content").toString();
        return configuration;
    }
}
