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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.solr;

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
import java.util.List;
import java.util.Map;

@Getter
@Slf4j
@AutoService(Params.class)
@NoArgsConstructor
public class SolrParams extends BigtopParams {

    private String solrPort = "8983";
    private String solrLogDir = "/var/log/solr";
    private String solrPidDir = "/var/run/solr";
    private String solrPidFile = solrPidDir + "/solr-" + solrPort + ".pid";

    private String zNode;
    private String zkString;
    private String zkHost;

    public SolrParams(ComponentCommandPayload componentCommandPayload) {
        super(componentCommandPayload);
        globalParamsMap.put("java_home", javaHome());
        globalParamsMap.put("solr_home", serviceHome());
        globalParamsMap.put("security_enabled", false);
        globalParamsMap.put("solr_pid_file", solrPidFile);
        globalParamsMap.put("solr_user", user());
        globalParamsMap.put("solr_group", group());
    }

    @GlobalParams
    public Map<String, Object> solrXml() {
        return LocalSettings.configurations(getServiceName(), "solr-xml");
    }

    @GlobalParams
    public Map<String, Object> solrEnv() {
        Map<String, Object> solrEnv = LocalSettings.configurations(getServiceName(), "solr.in");
        solrLogDir = (String) solrEnv.get("solr_log_dir");
        solrPidDir = (String) solrEnv.get("solr_pid_dir");
        solrPort = (String) solrEnv.get("solr_port");
        solrPidFile = solrPidDir + "/solr-" + solrPort + ".pid";

        List<String> ZookeeperServerHosts = LocalSettings.hosts("zookeeper_server");
        Map<String, Object> ZKPort = LocalSettings.configurations("zookeeper", "zoo.cfg");
        String clientPort = (String) ZKPort.get("clientPort");
        zNode = (String) solrEnv.get("solr_znode");
        zkString = MessageFormat.format("{0}:{1}", ZookeeperServerHosts.get(0), clientPort);
        zkHost = MessageFormat.format("{0}{1}", zkString, zNode);
        return solrEnv;
    }

    @Override
    public String confDir() {
        return serviceHome() + "/server/solr";
    }

    @Override
    public String getServiceName() {
        return "solr";
    }
}
