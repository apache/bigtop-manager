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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.tez;

import org.apache.bigtop.manager.grpc.payload.ComponentCommandPayload;
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
import java.util.Map;

@Getter
@Slf4j
@AutoService(Params.class)
@NoArgsConstructor
public class TezParams extends BigtopParams {

    private String headDumpOpts;
    private String tezEnvContent;

    public TezParams(ComponentCommandPayload componentCommandPayload) {
        super(componentCommandPayload);
        globalParamsMap.put("tez_user", user());
        globalParamsMap.put("tez_group", group());
        globalParamsMap.put("java_home", javaHome());
        globalParamsMap.put("hadoop_home", hadoopHome());
        globalParamsMap.put("hadoop_conf_dir", hadoopConfDir());
        globalParamsMap.put("tez_home", serviceHome());
        globalParamsMap.put("tez_conf_dir", confDir());
        globalParamsMap.put("head_dump_opts", headDumpOpts);
    }

    @GlobalParams
    public Map<String, Object> tezSite() {
        return LocalSettings.configurations(getServiceName(), "tez-site");
    }

    @GlobalParams
    public Map<String, Object> tezEnv() {
        Map<String, Object> tezEnv = LocalSettings.configurations(getServiceName(), "tez-env");

        String heapDumpEnabled = (String) tezEnv.get("enable_heap_dump");
        if (StringUtils.isNotBlank(heapDumpEnabled) && Boolean.parseBoolean(heapDumpEnabled)) {
            String heapDumpLocation = StringUtils.defaultIfBlank((String) tezEnv.get("heap_dump_location"), "/tmp");
            headDumpOpts =
                    MessageFormat.format("-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath={0}", heapDumpLocation);
        }

        tezEnvContent = (String) tezEnv.get("content");

        return tezEnv;
    }

    public String hadoopConfDir() {
        return hadoopHome() + "/etc/hadoop";
    }

    public String hadoopHome() {
        return stackHome() + "/hadoop";
    }

    @Override
    public String getServiceName() {
        return "tez";
    }
}
