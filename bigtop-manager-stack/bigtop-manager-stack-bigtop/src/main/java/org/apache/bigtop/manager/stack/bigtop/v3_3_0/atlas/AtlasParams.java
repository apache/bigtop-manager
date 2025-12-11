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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.atlas;

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
import java.util.stream.Collectors;

@Getter
@Slf4j
@AutoService(Params.class)
@NoArgsConstructor
public class AtlasParams extends BigtopParams {

    /* ======== Atlas Settings ======== */
    private String atlasPort        = "21000";
    private String atlasLogDir      = "/var/log/atlas";
    private String atlasPidDir      = "/var/run/atlas";
    private String atlasPidFile     = atlasPidDir + "/atlas.pid";
    private String atlasConfDir     = null;

    /* ======== External Components ======== */
    private String hbaseConfDir     = "/etc/hbase/conf";
    private String hadoopHome       = "/usr/lib/hadoop";
    private String zkQuorum         = null;

    /* ======== Solr ======== */
    private String solrZkQuorum     = null;
    private String solrZkZnode      = "/solr";
    private String solrPort         = "8983";

    /* ======== Elasticsearch ======== */
    private String esHosts          = null;          // host1:9200,host2:9200
    private String esHttpUrls       = null;          // http://host1:9200,http://host2:9200
    private String esUsername       = "elastic";
    private String esPassword       = "";

    /* ======== Kafka ======== */
    private String kafkaBootstrapServers = null;

    /* ======== Search Backend Type ======== */
    private String searchBackend = "elasticsearch";  // "elasticsearch" or "solr"

    /* ======== Configuration Sources ======== */
    private Map<String, Object> atlasEnvMap;
    private Map<String, Object> appPropsMap;

    public AtlasParams(ComponentCommandPayload payload) {
        super(payload);
        atlasConfDir = serviceHome() + "/conf";
        globalParamsMap.put("java_home",      javaHome());
        globalParamsMap.put("atlas_home",     serviceHome());
        globalParamsMap.put("atlas_user",     user());
        globalParamsMap.put("atlas_group",    group());
        globalParamsMap.put("atlas_conf_dir", atlasConfDir);
        globalParamsMap.put("atlas_pid_file", atlasPidFile);
    }

    /* --------------------------------------------------
     * 1. atlas-env.xml
     * -------------------------------------------------- */
    @GlobalParams
    public Map<String, Object> atlasEnv() {
        atlasEnvMap = LocalSettings.configurations(getServiceName(), "atlas-env");

        atlasLogDir  = (String) atlasEnvMap.getOrDefault("atlas_log_dir",  atlasLogDir);
        atlasPidDir  = (String) atlasEnvMap.getOrDefault("atlas_pid_dir",  atlasPidDir);
        atlasPort    = (String) atlasEnvMap.getOrDefault("atlas_port",    atlasPort);
        atlasPidFile = atlasPidDir + "/atlas.pid";

        hbaseConfDir = (String) atlasEnvMap.getOrDefault("hbase_conf_dir", hbaseConfDir);
        hadoopHome   = (String) atlasEnvMap.getOrDefault("hadoop_home",    hadoopHome);

        /* ZooKeeper Quorum */
        List<String> zkHosts = LocalSettings.componentHosts("zookeeper_server");
        String zkPort = (String) LocalSettings.configurations("zookeeper", "zoo.cfg").get("clientPort");
        zkQuorum = zkHosts.stream().map(h -> h + ":" + zkPort).collect(Collectors.joining(","));
        atlasEnvMap.put("zk_quorum", zkQuorum);

        /* Determine search backend: try Elasticsearch first, then Solr */
        boolean esAvailable = isElasticsearchAvailable();
        boolean solrAvailable = isSolrAvailable();

        if (esAvailable) {
            searchBackend = "elasticsearch";
            loadEsParams();
            log.info("Atlas will use Elasticsearch as search backend");
        } else if (solrAvailable) {
            searchBackend = "solr";
            loadSolrParams();
            log.info("Atlas will use Solr as search backend");
        } else {
            log.warn("Neither Elasticsearch nor Solr is available. Atlas search functionality may be limited.");
            // Load both with empty values for graceful degradation
            loadSolrParams();
            loadEsParams();
        }

        /* Kafka */
        loadKafkaParams();

        globalParamsMap.putAll(atlasEnvMap);
        return atlasEnvMap;
    }

    /* --------------------------------------------------
     * 2. atlas-application-properties.xmlaa
     * -------------------------------------------------- */
    @GlobalParams
    public Map<String, Object> applicationProperties() {
        appPropsMap = LocalSettings.configurations(getServiceName(), "application-properties");

        /* Populate dynamic values into template, ${xxx} will be auto-replaced */
        // Search backend type
        appPropsMap.put("atlas_graph_index_search_backend", searchBackend);
        
        // ZooKeeper related
        appPropsMap.put("atlas_graph_storage_hostname", zkQuorum);
        appPropsMap.put("atlas_kafka_zookeeper_connect", zkQuorum);
        appPropsMap.put("atlas_audit_hbase_zookeeper_quorum", zkQuorum);

        // Elasticsearch
        appPropsMap.put("atlas_graph_index_search_hostname", esHosts);
        appPropsMap.put("atlas_elasticsearch_username", esUsername);
        appPropsMap.put("atlas_elasticsearch_password", esPassword);

        // Kafka
        appPropsMap.put("atlas_kafka_bootstrap_servers", kafkaBootstrapServers);

        // Atlas REST address
        String atlasRestAddress = MessageFormat.format("http://{0}:{1}", hostname(), atlasPort);
        appPropsMap.put("atlas_rest_address", atlasRestAddress);

        // Legacy properties for backwards compatibility
        appPropsMap.put("atlas.graph.index.search.solr.zookeeper-url", solrZkQuorum);
        appPropsMap.put("atlas.graph.index.search.solr.zookeeper-znode", solrZkZnode);
        appPropsMap.put("atlas.graph.index.search.elasticsearch.http-urls", esHttpUrls);

        globalParamsMap.putAll(appPropsMap);
        return appPropsMap;
    }

    /* ======== Private Helper Methods ======== */
    
    /**
     * Check if Elasticsearch is available in the cluster
     */
    private boolean isElasticsearchAvailable() {
        try {
            List<String> esHosts = LocalSettings.componentHosts("elasticsearch_master");
            return esHosts != null && !esHosts.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if Solr is available in the cluster
     */
    private boolean isSolrAvailable() {
        try {
            Map<String, Object> solrEnv = LocalSettings.configurations("solr", "solr-env");
            List<String> solrHosts = LocalSettings.componentHosts("solr_instance");
            return solrEnv != null && solrHosts != null && !solrHosts.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    private void loadSolrParams() {
        try {
            Map<String, Object> solrEnv = LocalSettings.configurations("solr", "solr-env");
            if (solrEnv != null) {
                solrPort    = (String) solrEnv.getOrDefault("solr_port", "8983");
                solrZkZnode = (String) solrEnv.getOrDefault("solr_znode", "/solr");
            }

            List<String> zkHosts = LocalSettings.componentHosts("zookeeper_server");
            if (zkHosts != null && !zkHosts.isEmpty()) {
                String zkPort = (String) LocalSettings.configurations("zookeeper", "zoo.cfg").get("clientPort");
                solrZkQuorum = zkHosts.stream()
                        .map(h -> h + ":" + zkPort + solrZkZnode)
                        .collect(Collectors.joining(","));
            } else {
                solrZkQuorum = "";
            }
            atlasEnvMap.put("solr_zk_quorum", solrZkQuorum);
            atlasEnvMap.put("solr_port", solrPort);
            atlasEnvMap.put("solr_zk_znode", solrZkZnode);
        } catch (Exception e) {
            log.warn("Failed to load Solr params, Solr may not be installed: {}", e.getMessage());
            atlasEnvMap.put("solr_zk_quorum", "");
            atlasEnvMap.put("solr_port", solrPort);
            atlasEnvMap.put("solr_zk_znode", solrZkZnode);
        }
    }

    private void loadEsParams() {
        try {
            // Try to get Elasticsearch hosts, if not available, use empty string
            List<String> esHostsList = LocalSettings.componentHosts("elasticsearch_master");
            if (esHostsList != null && !esHostsList.isEmpty()) {
                String esPort = "9200";   // Can also read from es-config.xml
                esHosts = esHostsList.stream().map(h -> h + ":" + esPort).collect(Collectors.joining(","));
                esHttpUrls = esHostsList.stream().map(h -> "http://" + h + ":" + esPort).collect(Collectors.joining(","));
            } else {
                esHosts = "";
                esHttpUrls = "";
            }

            // Read ES credentials from configuration if available
            Map<String, Object> appProps = LocalSettings.configurations(getServiceName(), "application-properties");
            if (appProps != null) {
                if (appProps.containsKey("atlas_elasticsearch_username")) {
                    esUsername = (String) appProps.get("atlas_elasticsearch_username");
                }
                if (appProps.containsKey("atlas_elasticsearch_password")) {
                    esPassword = (String) appProps.get("atlas_elasticsearch_password");
                }
            }
        } catch (Exception e) {
            log.warn("Failed to load Elasticsearch params, ES may not be installed: {}", e.getMessage());
            esHosts = "";
            esHttpUrls = "";
        }

        atlasEnvMap.put("es_hosts", esHosts);
        atlasEnvMap.put("es_http_urls", esHttpUrls);
    }

    private void loadKafkaParams() {
        try {
            List<String> kafkaHosts = LocalSettings.componentHosts("kafka_broker");
            if (kafkaHosts != null && !kafkaHosts.isEmpty()) {
                String kafkaPort = "9092";  // Can also read from kafka-config.xml
                kafkaBootstrapServers = kafkaHosts.stream()
                        .map(h -> h + ":" + kafkaPort)
                        .collect(Collectors.joining(","));
            } else {
                kafkaBootstrapServers = "";
            }
        } catch (Exception e) {
            log.warn("Failed to load Kafka params, Kafka may not be installed: {}", e.getMessage());
            kafkaBootstrapServers = "";
        }
        atlasEnvMap.put("kafka_bootstrap_servers", kafkaBootstrapServers);
    }

    @Override
    public String confDir() {
        return atlasConfDir;
    }

    @Override
    public String getServiceName() {
        return "atlas";
    }
}