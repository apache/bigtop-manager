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
package org.apache.bigtop.manager.stack.extra.v1_0_0.trino;

import com.google.auto.service.AutoService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.bigtop.manager.grpc.payload.ComponentCommandPayload;
import org.apache.bigtop.manager.stack.core.annotations.GlobalParams;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;
import org.apache.bigtop.manager.stack.extra.param.ExtraParams;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
@NoArgsConstructor
@AutoService(Params.class)
public class TrinoParams extends ExtraParams {

    private String jvmConfigContent;

    public TrinoParams(ComponentCommandPayload payload) {
        super(payload);
        globalParamsMap.put("java_home", javaHome());
        globalParamsMap.put("trino_user", user());
        globalParamsMap.put("trino_group", group());
        globalParamsMap.put("trino_home", serviceHome());
        globalParamsMap.put("trino_conf_dir", confDir());
    }

    @Override
    public String getServiceName() {
        return "trino";
    }

    @Override
    public String javaHome() {
        String root = LocalSettings.cluster().getRootDir();
        return MessageFormat.format("{0}/dependencies/jdk25", root);
    }

    @Override
    public String confDir() {
        return serviceHome() + "/etc";
    }

    public List<String> coordinatorHosts() {
        return LocalSettings.componentHosts("coordinator");
    }

    @GlobalParams
    public Map<String, Object> nodeProperties() {
        return LocalSettings.configurations(getServiceName(), "node.properties");
    }

    @GlobalParams
    public Map<String, Object> jvmConfig() {
        Map<String, Object> configuration = LocalSettings.configurations(getServiceName(), "jvm.config");
        jvmConfigContent = configuration.get("content").toString();
        return configuration;
    }

    @GlobalParams
    public Map<String, Object> configProperties() {
        Map<String, Object> configuration = LocalSettings.configurations(getServiceName(), "config.properties");
        Object port = configuration.get("http-server.http.port");
        List<String> hosts = coordinatorHosts();
        if (!hosts.isEmpty()) {
            configuration.put("discovery.uri", MessageFormat.format("http://{0}:{1}", hosts.get(0), port));
        }
        return configuration;
    }

    @GlobalParams
    public Map<String, Object> hiveProperties() {
        Map<String, Object> configuration = LocalSettings.configurations(getServiceName(), "hive.properties");
        return configuration;
    }
}
