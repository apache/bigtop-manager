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
package org.apache.bigtop.manager.stack.infra.v1_0_0.grafana;

import org.apache.bigtop.manager.common.message.entity.payload.CommandPayload;
import org.apache.bigtop.manager.stack.core.annotations.GlobalParams;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;
import org.apache.bigtop.manager.stack.infra.param.InfraParams;

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
public class GrafanaParams extends InfraParams {

    private String grafanaContent;
    private String grafanaPort;
    private String grafanaLogLevel;

    public GrafanaParams(CommandPayload commandPayload) {
        super(commandPayload);
        globalParamsMap.put("port", grafanaPort);
        globalParamsMap.put("log_level", grafanaLogLevel);
    }

    public String dataDir() {
        return MessageFormat.format("{0}/data", serviceHome());
    }

    public String confDir() {
        return MessageFormat.format("{0}/conf", serviceHome());
    }

    @GlobalParams
    public Map<String, Object> configs() {
        Map<String, Object> configuration = LocalSettings.configurations(getServiceName(), "grafana");
        grafanaContent = (String) configuration.get("content");
        grafanaPort = (String) configuration.get("port");
        grafanaLogLevel = (String) configuration.get("logLevel");
        return configuration;
    }

    @Override
    public String getServiceName() {
        return "grafana";
    }
}
