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
    private List<Map<String, Object>> scrapeJobs;
    private String prometheusPort;
    private String prometheusContent;
    private String prometheusRulesFilename;
    private String prometheusRulesFileContent;

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
        scrapeJobs = new ArrayList<>();
        scrapeJobs.add(prometheusScrapeJob);
        scrapeJobs.add(agentScrapeJob);
        globalParamsMap.put("scrape_jobs", scrapeJobs);
        globalParamsMap.put("rules_file_name", prometheusRulesFilename);
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

    @GlobalParams
    public Map<String, Object> rules() {
        Map<String, Object> configuration = LocalSettings.configurations(getServiceName(), "prometheus-rule");

        prometheusRulesFilename = (String) configuration.get("rules_file_name");
        prometheusRulesFileContent = (String) configuration.get("content");
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
}
