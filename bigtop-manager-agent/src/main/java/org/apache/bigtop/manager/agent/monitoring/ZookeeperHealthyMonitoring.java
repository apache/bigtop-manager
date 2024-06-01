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
package org.apache.bigtop.manager.agent.monitoring;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import org.apache.bigtop.manager.common.constants.CacheFiles;
import org.apache.bigtop.manager.common.constants.Constants;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class ZookeeperHealthyMonitoring {

    private final static int STATUS_FOLLOWER = 2;
    private final static int STATUS_LEADER = 1;
    private final static int ZOOKEEPER_UNHEALTHY = 0;

    private final static String ZOOKEEPER_HEALTHY_MONITORING_NAME = "zookeeper_monitoring";

    public static void zookeeperUpdateStatus(MultiGauge multiGauge) {
        Socket sock = null;
        BufferedReader reader = null;
        try {
            JsonNode hostInfo = AgentHostMonitoring.getHostInfo().get(AgentHostMonitoring.AGENT_BASE_INFO);
            String hostName = hostInfo.get("hostname").asText();
            String ipv4 = hostInfo.get("iPv4addr").asText();
            JsonNode hostComponent = getHostComponent();
            if (null == hostComponent || hostComponent.isEmpty()) return;
            Properties zooCFG = getZooCFG(hostComponent);
            String zookeeperPort = zooCFG.getProperty("clientPort", "2181");
            InetSocketAddress hostAddress = new InetSocketAddress(hostName, Integer.parseInt(zookeeperPort));
            sock = new Socket();
            sock.connect(hostAddress, 3000);
            sock.setSoTimeout(3000);
            OutputStream outStream = sock.getOutputStream();
            outStream.write("srvr".getBytes(UTF_8));
            outStream.flush();
            sock.shutdownOutput();
            reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            List<Tag> tags = new ArrayList<>();
            tags.add(Tag.of("host_name", hostName));
            tags.add(Tag.of("ipv4", ipv4));
            tags.add(Tag.of("client_port", zookeeperPort));
            List<String> lines = reader.lines().toList();
            int final_status = ZOOKEEPER_UNHEALTHY;
            for (String line : lines) {
                if (line.contains("Mode")) {
                    String mode = line.split(":")[1].trim();
                    final_status = mode.contains("leader") ? STATUS_LEADER : STATUS_FOLLOWER;
                    tags.add(Tag.of("mode", mode));
                }
                if (line.contains("version")) {
                    tags.add(Tag.of("version", line.split(":")[1].trim()));
                }
            }
            ArrayList<MultiGauge.Row<?>> rows = new ArrayList<>();
            rows.add(MultiGauge.Row.of(Tags.of(tags), final_status));
            multiGauge.register(rows, true);
        } catch (Exception e) {
            log.error("Exception while executing four letter word: srvr", e);
        } finally {
            try {
                if (sock != null) {
                    sock.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    public static MultiGauge registerZookeeperHealthyGauge(MeterRegistry registry) {
        return MultiGauge.builder(ZOOKEEPER_HEALTHY_MONITORING_NAME)
                .description("BigTop Manager Zookeeper Healthy Monitoring, 0:unhealthy, 1:leader, 2:follower")
                .baseUnit("healthy").register(registry);
    }

    private static Properties getZooCFG(JsonNode hostComponent) {
        try {
            String root = hostComponent.get("root").asText("/opt");
            String stackName = hostComponent.get("stackName").asText("bigtop");
            String stackVerion = hostComponent.get("stackVersion").asText("3.3.0");
            String zookeeperCFG = "usr/lib/zookeeper/conf/zoo.cfg";
            String cfgFullPath = String.format("%s/%s/%s/%s", root, stackName, stackVerion, zookeeperCFG);
            FileInputStream fileInputStream = new FileInputStream(cfgFullPath);
            Properties properties = new Properties();
            properties.load(fileInputStream);
            return properties;
        } catch (IOException e) {
            log.error("get Zookeeper Config Error", e);
            throw new RuntimeException(e);
        }
    }

    private static JsonNode getHostComponent() {
        String cacheDir = Constants.STACK_CACHE_DIR;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String clusterInfo = cacheDir + CacheFiles.CLUSTER_INFO;
            String componentInfo = cacheDir + CacheFiles.COMPONENTS_INFO;
            File cluster = new File(clusterInfo);
            File component = new File(componentInfo);
            if (!cluster.exists() || !component.exists()) return null;
            JsonNode clusterJson = objectMapper.readTree(cluster);
            JsonNode componentJson = objectMapper.readTree(component);
            ObjectNode zookeeperServer = (ObjectNode) componentJson.get("zookeeper_server");
            if (null == zookeeperServer || zookeeperServer.isEmpty()) return null;
            ((ObjectNode) clusterJson).setAll(zookeeperServer);
            return clusterJson;
        } catch (IOException e) {
            log.error("get cached cluster info error: ", e);
        }
        return null;
    }
}
