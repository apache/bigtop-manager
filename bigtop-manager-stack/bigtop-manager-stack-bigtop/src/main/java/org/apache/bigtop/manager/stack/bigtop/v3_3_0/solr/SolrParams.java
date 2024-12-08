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

import org.apache.bigtop.manager.common.message.entity.payload.CommandPayload;
import org.apache.bigtop.manager.common.utils.Environments;
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
    private String solrHomeDir = "/var/lib/solr";
    private String solrDataDir = "/var/lib/solr/data";
    private String solrPidFile = solrPidDir + "/solr-" + solrPort + ".pid";

    public SolrParams(CommandPayload commandPayload) {
        super(commandPayload);
        globalParamsMap.put("java_home", Environments.getJavaHome());
        globalParamsMap.put("solr_home", solrHomeDir);
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
    public Map<String, Object> solrLog4j() {
        return LocalSettings.configurations(getServiceName(), "solr-log4j");
    }

    public String getZnode() {
        Map<String, Object> solrEnv = LocalSettings.configurations(getServiceName(), "solr-env");
        return (String) solrEnv.get("solr_znode");
    }

    public String getzkString() {
        List<String> ZookeeperServerHosts = LocalSettings.hosts("zookeeper_server");
        Map<String, Object> Zkport = LocalSettings.configurations("zookeeper", "zoo.cfg");
        String clientPort = (String) Zkport.get("clientPort");
        return MessageFormat.format("{0}:{1}", ZookeeperServerHosts.get(0), clientPort);
    }

    public String zkHost() {
        String Znode = getZnode();
        String SolrZkstring = getzkString();
        return MessageFormat.format("{0}{1}", SolrZkstring, Znode);
    }

    @GlobalParams
    public Map<String, Object> solrEnv() {
        Map<String, Object> solrEnv = LocalSettings.configurations(getServiceName(), "solr-env");
        solrLogDir = (String) solrEnv.get("solr_log_dir");
        solrPidDir = (String) solrEnv.get("solr_pid_dir");
        solrDataDir = (String) solrEnv.get("solr_datadir");
        solrPort = (String) solrEnv.get("SOLR_PORT");
        solrPidFile = solrPidDir + "/solr-" + solrPort + ".pid";
        return solrEnv;
    }

    @Override
    public String getServiceName() {
        return "solr";
    }
}
