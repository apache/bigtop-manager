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
package org.apache.bigtop.manager.stack.infra.v1_0_0.prometheus;

import org.apache.bigtop.manager.grpc.payload.ComponentCommandPayload;
import org.apache.bigtop.manager.grpc.pojo.ClusterInfo;
import org.apache.bigtop.manager.stack.core.annotations.GlobalParams;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;
import org.apache.bigtop.manager.stack.infra.param.InfraParams;

import com.google.auto.service.AutoService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Slf4j
@AutoService(Params.class)
@NoArgsConstructor
public class PrometheusParams extends InfraParams {

    protected final String PROMETHEUS_SELF_JOB_NAME = "prometheus";
    protected final String BM_AGENT_JOB_NAME = "bm-agent";
    protected final String BM_AGENT_PORT = "8081";
    public static final String AGENT_TARGET_LABEL = "cluster";

    private Map<String, Object> prometheusScrapeJob;
    private Map<String, Object> agentScrapeJob;
    private List<Map<String, Object>> zookeeperScrapeJobs;
    private List<Map<String, Object>> scrapeJobs;
    private String prometheusPort;
    private String prometheusContent;

    public PrometheusParams(ComponentCommandPayload componentCommandPayload) {
        super(componentCommandPayload);

        globalParamsMap.put("prometheus_user", user());
        globalParamsMap.put("prometheus_group", group());
        globalParamsMap.put("prometheus_home", serviceHome());
    }

    @Override
    public void initGlobalParams() {
        super.initGlobalParams();

        setAgentScrapeJob();
        setZookeeperScrapeJob();

        scrapeJobs = new ArrayList<>();
        scrapeJobs.add(prometheusScrapeJob);
        scrapeJobs.add(agentScrapeJob);
        scrapeJobs.addAll(zookeeperScrapeJobs);
        globalParamsMap.put("scrape_jobs", scrapeJobs);
    }

    public String dataDir() {
        return MessageFormat.format("{0}/data", serviceHome());
    }

    public String targetsConfigFile(String jobName) {
        return MessageFormat.format("{0}/{1}_targets.json", confDir(), jobName);
    }

    @Override
    public String getServiceName() {
        return "prometheus";
    }

    @GlobalParams
    public Map<String, Object> prometheusJob() {
        Map<String, Object> configuration = LocalSettings.configurations(getServiceName(), "prometheus");
        prometheusPort = (String) configuration.get("port");
        Map<String, Object> job = new HashMap<>();
        job.put("name", PROMETHEUS_SELF_JOB_NAME);
        job.put("targets_file", targetsConfigFile(PROMETHEUS_SELF_JOB_NAME));

        Map<String, Object> target = new HashMap<>();
        target.put("targets", List.of(MessageFormat.format("localhost:{0}", prometheusPort)));
        job.put("targets_list", List.of(target));

        prometheusScrapeJob = job;
        return configuration;
    }

    @GlobalParams
    public Map<String, Object> configs() {
        Map<String, Object> configuration = LocalSettings.configurations(getServiceName(), "prometheus");

        prometheusContent = (String) configuration.get("content");
        return configuration;
    }

    public String listenAddress() {
        return MessageFormat.format("0.0.0.0:{0}", prometheusPort);
    }

    public void setAgentScrapeJob() {
        agentScrapeJob = new HashMap<>();
        agentScrapeJob.put("name", BM_AGENT_JOB_NAME);
        agentScrapeJob.put("targets_file", targetsConfigFile(BM_AGENT_JOB_NAME));
        agentScrapeJob.put("metrics_path", "/actuator/prometheus");

        List<Map<String, Object>> agentTargets = new ArrayList<>();
        Map<String, List<String>> clusterHosts = getClusterHosts();
        if (clusterHosts != null) {
            clusterHosts.forEach((cluster, hosts) -> {
                Map<String, Object> agentTarget = new HashMap<>();
                List<String> targets = new ArrayList<>();
                for (String host : hosts) {
                    targets.add(MessageFormat.format("{0}:{1}", host, BM_AGENT_PORT));
                }
                agentTarget.put("targets", targets);
                agentTarget.put("labels", Map.of(AGENT_TARGET_LABEL, cluster));
                agentTargets.add(agentTarget);
            });
        }

        agentScrapeJob.put("targets_list", agentTargets);
    }

    public void setZookeeperScrapeJob() {
        zookeeperScrapeJobs = new ArrayList<>();
        Map<String, Map<String, Object>> configurations = configurations("zookeeper", "zoo.cfg");
        for (ClusterInfo clusterInfo : clusters()) {
            Map<String, Object> zooCfg = configurations.get(clusterInfo.getName());
            Object metricsClass = zooCfg.get("metricsProvider.className");
            String defaultProvider = "org.apache.zookeeper.metrics.prometheus.PrometheusMetricsProvider";
            if (metricsClass == null || !metricsClass.equals(defaultProvider)) {
                continue;
            }

            String clusterName = clusterInfo.getName();
            String jobName = MessageFormat.format("{0}-zookeeper", clusterName);
            Map<String, Object> job = new HashMap<>();
            job.put("name", jobName);
            job.put("targets_file", targetsConfigFile(jobName));

            Map<String, Object> target = new HashMap<>();
            List<String> zkServers = getComponentHosts("zookeeper_server").get(clusterName);
            Object port = zooCfg.getOrDefault("metricsProvider.httpPort", 7000L);

            List<String> targets = zkServers.stream().map(s -> s + ":" + port).toList();
            target.put("targets", targets);
            job.put("targets_list", List.of(target));

            zookeeperScrapeJobs.add(job);
        }
    }
}
