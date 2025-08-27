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

import org.apache.bigtop.manager.grpc.payload.ComponentCommandPayload;
import org.apache.bigtop.manager.grpc.pojo.RepoInfo;
import org.apache.bigtop.manager.stack.core.annotations.GlobalParams;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;
import org.apache.bigtop.manager.stack.infra.param.InfraParams;
import org.apache.bigtop.manager.stack.infra.v1_0_0.prometheus.PrometheusParams;

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
public class GrafanaParams extends InfraParams {

    private String grafanaContent;
    private String grafanaDashboardContent;
    private String prometheusDashboardPath;
    private String grafanaPort;
    private String grafanaLogLevel;
    private String dataSourceContent;
    private String prometheusServer;
    private String prometheusPort;

    private List<Map<String, Object>> dashboards;

    public GrafanaParams(ComponentCommandPayload componentCommandPayload) {
        super(componentCommandPayload);

        globalParamsMap.put("grafana_user", user());
        globalParamsMap.put("grafana_group", group());
        globalParamsMap.put("grafana_home", serviceHome());
    }

    @Override
    public void initGlobalParams() {
        super.initGlobalParams();

        globalParamsMap.put("port", grafanaPort);
        globalParamsMap.put("log_level", grafanaLogLevel);
        globalParamsMap.put("provisioning_path", provisioningDir());
        globalParamsMap.put("prometheus_url", MessageFormat.format("http://{0}:{1}", prometheusServer, prometheusPort));
        setDashboards();
        globalParamsMap.put("dashboards", dashboards);
    }

    public String dataDir() {
        return MessageFormat.format("{0}/data", serviceHome());
    }

    public String provisioningDir() {
        return MessageFormat.format("{0}/provisioning", confDir());
    }

    public String dataSourceDir() {
        return MessageFormat.format("{0}/datasources", provisioningDir());
    }

    public String dashboardsDir() {
        return MessageFormat.format("{0}/dashboards", provisioningDir());
    }

    public String dashboardConfigDir(String type) {
        return MessageFormat.format("{0}/{1}", dashboardsDir(), type);
    }

    @GlobalParams
    public Map<String, Object> configs() {
        Map<String, Object> configuration = LocalSettings.configurations(getServiceName(), "grafana");
        grafanaContent = (String) configuration.get("content");
        grafanaPort = (String) configuration.get("port");
        grafanaLogLevel = (String) configuration.get("log_level");
        return configuration;
    }

    @GlobalParams
    public Map<String, Object> dataSources() {
        Map<String, Object> configuration = LocalSettings.configurations(getServiceName(), "grafana-datasources");
        dataSourceContent = (String) configuration.get("content");
        return configuration;
    }

    @GlobalParams
    public Map<String, Object> prometheus() {
        Map<String, Object> configuration = LocalSettings.configurations(getServiceName(), "grafana-datasources");
        List<String> prometheusServers = LocalSettings.componentHosts().get("prometheus_server");
        if (prometheusServers == null || prometheusServers.isEmpty()) {
            return configuration;
        }
        prometheusServer = prometheusServers.get(0);

        Map<String, Object> prometheusConf = LocalSettings.configurations("prometheus", "prometheus");
        if (prometheusConf == null) {
            return configuration;
        }
        prometheusPort = (String) prometheusConf.get("port");
        return configuration;
    }

    @GlobalParams
    public Map<String, Object> dashboards() {
        Map<String, Object> configuration = LocalSettings.configurations(getServiceName(), "grafana-dashboard");
        grafanaDashboardContent = (String) configuration.get("content");
        prometheusDashboardPath = MessageFormat.format("{0}/prometheus", dashboardsDir());
        return configuration;
    }

    public List<String> getClusters() {
        if (getClusterHosts() == null) {
            return null;
        }
        return new ArrayList<>(getClusterHosts().keySet());
    }

    public void setDashboards() {
        dashboards = new ArrayList<>();

        // Used for dashboard yaml configuration
        Map<String, Object> clusterDashboard = new HashMap<>();
        clusterDashboard.put("name", "Cluster");
        clusterDashboard.put("path", dashboardConfigDir("cluster"));

        Map<String, Object> hostDashboard = new HashMap<>();
        hostDashboard.put("name", "Host");
        hostDashboard.put("path", dashboardConfigDir("host"));

        Map<String, Object> zookeeperDashboard = new HashMap<>();
        zookeeperDashboard.put("name", "ZooKeeper");
        zookeeperDashboard.put("path", dashboardConfigDir("zookeeper"));

        dashboards.add(clusterDashboard);
        dashboards.add(hostDashboard);
        dashboards.add(zookeeperDashboard);

        // Used for dashboard json configuration
        globalParamsMap.put("cluster_label", PrometheusParams.AGENT_TARGET_LABEL);

        Map<String, List<String>> clusterHost = getClusterHosts();
        if (clusterHost != null && !clusterHost.isEmpty()) {
            String defaultCluster = clusterHost.keySet().iterator().next();
            String defaultHost = clusterHost.get(defaultCluster).isEmpty()
                    ? ""
                    : clusterHost.get(defaultCluster).get(0);
            clusterDashboard.put("default_cluster_name", defaultCluster);
            globalParamsMap.put("default_cluster_name", defaultCluster);
            globalParamsMap.put("default_host_name", defaultHost);
        }
    }

    @Override
    public RepoInfo repo() {
        return LocalSettings.repo("grafana");
    }

    @Override
    public String getServiceName() {
        return "grafana";
    }
}
