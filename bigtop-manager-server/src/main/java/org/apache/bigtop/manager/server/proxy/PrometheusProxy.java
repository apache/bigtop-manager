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
package org.apache.bigtop.manager.server.proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Component
public class PrometheusProxy {

    private final WebClient webClient;

    @Value("${monitoring.agent-host-job-name}")
    private String agentHostJobName;

    public PrometheusProxy(
            WebClient.Builder webClientBuilder, @Value("${monitoring.prometheus-host}") String prometheusHost) {
        this.webClient = webClientBuilder.baseUrl(prometheusHost).build();
    }

    public JsonNode queryAgentsHealthyStatus() {
        Mono<JsonNode> body = webClient
                .post()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/query").build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("query", "up{job=\"%s\"}".formatted(agentHostJobName))
                        .with("timeout", "10"))
                .retrieve()
                .bodyToMono(JsonNode.class);
        JsonNode result = body.block();

        ObjectMapper objectMapper = new ObjectMapper();
        if (result == null
                || result.isEmpty()
                || !"success".equals(result.get("status").asText("failure"))) {
            return objectMapper.createObjectNode();
        }
        JsonNode agents = result.get("data").get("result");
        ArrayNode agentsHealthyStatus = objectMapper.createArrayNode();
        for (JsonNode agent : agents) {
            JsonNode agentStatus = agent.get("metric");
            ObjectNode temp = objectMapper.createObjectNode();
            temp.put("agentInfo", agentStatus.get("instance").asText());
            temp.put("prometheusAgentJob", agentStatus.get("job").asText());
            JsonNode status = agent.get("value");
            LocalDateTime instant = Instant.ofEpochSecond(status.get(0).asLong())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            temp.put("time", instant.toString());
            temp.put("agentHealthyStatus", status.get(1).asInt() == 1 ? "running" : "down");
            agentsHealthyStatus.add(temp);
        }
        return agentsHealthyStatus;
    }

    private JsonNode queryAgents() {
        JsonNode result = query("agent_host_monitoring_cpu");
        ObjectMapper objectMapper = new ObjectMapper();
        if (result != null) {
            JsonNode agentCpus = result.get("data").get("result");
            if (agentCpus.isArray() && !agentCpus.isEmpty()) {
                Set<String> iPv4addrSet = new HashSet<>();
                for (JsonNode agent : agentCpus) {
                    iPv4addrSet.add(agent.get("metric").get("iPv4addr").asText());
                }
                ArrayNode iPv4addrArray = objectMapper.createArrayNode();
                for (String value : iPv4addrSet.toArray(new String[0])) {
                    iPv4addrArray.add(value);
                }
                return objectMapper.createObjectNode().set("iPv4addr", iPv4addrArray); // iPv4
            }
        }
        return objectMapper.createObjectNode();
    }

    public JsonNode queryAgentsInfo() {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode agentsInfo = objectMapper.createArrayNode();
        JsonNode agents = queryAgents().get("iPv4addr"); // get all host

        for (JsonNode agent : agents) {
            ObjectNode temp = objectMapper.createObjectNode();
            JsonNode cpuResult = queryAgentCpu(agent.asText());
            JsonNode memResult = queryAgentMemory(agent.asText());
            JsonNode diskResult = queryAgentDisk(agent.asText());
            // hostInfo
            temp.put("hostname", cpuResult.get("hostname").asText());
            temp.put("iPv4addr", cpuResult.get("iPv4addr").asText());
            // temp.put("iPv6addr", cpuResult.get("iPv6addr").asText());
            temp.put("cpuInfo", cpuResult.get("cpuInfo").asText().strip());
            temp.put("time", cpuResult.get("time").asText());
            temp.put("os", cpuResult.get("os").asText());
            temp.put("architecture", cpuResult.get("architecture").asText());
            temp.put("physical_cores", cpuResult.get("physical_cores").asText());
            // MEM
            temp.put("memIdle", memResult.get("memIdle").asLong());
            temp.put("memTotal", memResult.get("memTotal").asLong());
            // DISK
            long totalSpace = 0;
            long freeSpace = 0;
            for (JsonNode disk : diskResult.get("diskInfo")) {
                if (Objects.equals(disk.get("diskUsage").asText(), "diskTotalSpace")) {
                    totalSpace += disk.get("diskValue").asLong();
                } else if (Objects.equals(disk.get("diskUsage").asText(), "diskFreeSpace")) {
                    freeSpace += disk.get("diskValue").asLong();
                }
            }
            temp.put("diskFreeSpace", freeSpace);
            temp.put("diskTotalSpace", totalSpace);
            agentsInfo.add(temp);
        }

        return agentsInfo;
    }

    public JsonNode queryAgentsInstStatus() {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode agentsInfo = objectMapper.createArrayNode();
        JsonNode agents = queryAgents().get("iPv4addr"); // get all host

        for (JsonNode agent : agents) {
            JsonNode cpuResult = queryAgentCpu(agent.asText());
            JsonNode memResult = queryAgentMemory(agent.asText());
            JsonNode diskResult = queryAgentDisk(agent.asText());
            JsonNode diskIOResult = queryAgentDiskIO(agent.asText());
            ObjectNode temp = objectMapper.createObjectNode();

            // hostInfo
            temp.put("hostname", cpuResult.get("hostname").asText());
            temp.put("iPv4addr", cpuResult.get("iPv4addr").asText());
            temp.put("cpuInfo", cpuResult.get("cpuInfo").asText().strip());
            temp.put("time", cpuResult.get("time").asText());
            temp.put("cpuLoadAvgMin_1", cpuResult.get("cpuLoadAvgMin_1").asDouble());
            temp.put("cpuLoadAvgMin_5", cpuResult.get("cpuLoadAvgMin_5").asDouble());
            temp.put("cpuLoadAvgMin_15", cpuResult.get("cpuLoadAvgMin_15").asDouble());
            temp.put("cpuUsage", cpuResult.get("cpuUsage").asDouble());
            temp.put("fileTotalDescriptor", cpuResult.get("fileTotalDescriptor").asLong());
            temp.put("fileOpenDescriptor", cpuResult.get("fileOpenDescriptor").asLong());
            // MEM
            temp.put("memIdle", memResult.get("memIdle").asLong());
            temp.put("memTotal", memResult.get("memTotal").asLong());
            // DISK
            long totalSpace = 0;
            long freeSpace = 0;
            long totalFileHandle = 0;
            long freeFileHandle = 0;
            for (JsonNode disk : diskResult.get("diskInfo")) {
                if (Objects.equals(disk.get("diskUsage").asText(), "diskTotalSpace")) {
                    totalSpace += disk.get("diskValue").asLong();
                } else if (Objects.equals(disk.get("diskUsage").asText(), "diskFreeSpace")) {
                    freeSpace += disk.get("diskValue").asLong();
                } else if (Objects.equals(disk.get("diskUsage").asText(), "diskTotalFileHandle")) {
                    totalFileHandle += disk.get("diskValue").asLong();
                } else if (Objects.equals(disk.get("diskUsage").asText(), "diskFreeFileHandle")) {
                    freeFileHandle += disk.get("diskValue").asLong();
                }
            }
            temp.put("diskFreeSpace", freeSpace);
            temp.put("diskTotalSpace", totalSpace);
            temp.put("diskFreeFileHandle", freeFileHandle);
            temp.put("diskTotalFileHandle", totalFileHandle);
            // DISK IO
            temp.set("diskIO", diskIOResult.get("diskIO"));
            agentsInfo.add(temp);
        }

        return agentsInfo;
    }

    public JsonNode query(String params) {
        Mono<JsonNode> body = webClient
                .post()
                .uri("/api/v1/query")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("query", params).with("timeout", "10"))
                .retrieve()
                .bodyToMono(JsonNode.class);
        JsonNode result = body.block();
        if (result == null
                || result.isEmpty()
                || !"success".equals(result.get("status").asText("failure"))) {
            return null;
        }
        return result;
    }

    public JsonNode queryAgentCpu(String iPv4addr) {
        String params = String.format("agent_host_monitoring_cpu{iPv4addr=\"%s\"}", iPv4addr);
        JsonNode result = query(params);
        ObjectMapper objectMapper = new ObjectMapper();
        if (result != null) {
            JsonNode agentCpus = result.get("data").get("result");
            if (agentCpus.isArray() && !agentCpus.isEmpty()) {
                JsonNode agentCpuMetric = agentCpus.get(0).get("metric");
                JsonNode agentCpuValue = agentCpus.get(0).get("value");
                ObjectNode agentInfo = objectMapper.createObjectNode();
                agentInfo.put("hostname", agentCpuMetric.get("hostname").asText());
                agentInfo.put("cpuInfo", agentCpuMetric.get("cpu_info").asText());
                agentInfo.put("iPv4addr", agentCpuMetric.get("iPv4addr").asText());
                // temp.put("iPv6addr", agentCpuMetric.get("iPv6addr").asText());
                agentInfo.put("os", agentCpuMetric.get("os").asText());
                agentInfo.put("architecture", agentCpuMetric.get("arch").asText());
                agentInfo.put(
                        "physical_cores", agentCpuMetric.get("physical_cores").asText());
                agentInfo.put(
                        "fileOpenDescriptor",
                        agentCpuMetric.get("fileOpenDescriptor").asLong());
                agentInfo.put(
                        "fileTotalDescriptor",
                        agentCpuMetric.get("fileTotalDescriptor").asLong());
                LocalDateTime instant = Instant.ofEpochSecond(
                                agentCpuValue.get(0).asLong())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                agentInfo.put("time", instant.toString());
                for (JsonNode agent : agentCpus) {
                    agentInfo.put(
                            agent.get("metric").get("cpuUsage").asText(),
                            agent.get("value").get(1).asDouble()); // cpu metric
                }
                return agentInfo;
            }
        }
        return objectMapper.createObjectNode();
    }

    public JsonNode queryAgentMemory(String iPv4addr) {
        ObjectMapper objectMapper = new ObjectMapper();
        String query = String.format("agent_host_monitoring_mem{iPv4addr=\"%s\"}", iPv4addr);
        JsonNode result = query(query);
        if (result != null) {
            JsonNode agentsMem = result.get("data").get("result");
            if (agentsMem.isArray() && !agentsMem.isEmpty()) {
                JsonNode agentMemValue = agentsMem.get(0).get("value");
                JsonNode agentMemMetric = agentsMem.get(0).get("metric");
                ObjectNode agentsInfo = objectMapper.createObjectNode();
                agentsInfo.put("hostname", agentMemMetric.get("hostname").asText());
                agentsInfo.put("iPv4addr", agentMemMetric.get("iPv4addr").asText());
                LocalDateTime instant = Instant.ofEpochSecond(
                                agentMemValue.get(0).asLong())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                agentsInfo.put("time", instant.toString());
                for (JsonNode agent : agentsMem) {
                    agentsInfo.put(
                            agent.get("metric").get("memUsage").asText(),
                            agent.get("value").get(1).asLong()); // mem metric
                }
                return agentsInfo;
            }
        }
        return objectMapper.createObjectNode();
    }

    public JsonNode queryAgentDisk(String iPv4addr) {
        ObjectMapper objectMapper = new ObjectMapper();
        String params = String.format("agent_host_monitoring_disk{iPv4addr=\"%s\"}", iPv4addr);
        JsonNode result = query(params);
        if (result != null) {
            JsonNode agentDisksResult = result.get("data").get("result");
            if (agentDisksResult.isArray() && !agentDisksResult.isEmpty()) {
                JsonNode agentDisksMetric = agentDisksResult.get(0).get("metric");
                JsonNode agentDisksValue = agentDisksResult.get(0).get("value");
                ObjectNode agentDiskInfo = objectMapper.createObjectNode();
                agentDiskInfo.put("hostname", agentDisksMetric.get("hostname").asText());
                agentDiskInfo.put("iPv4addr", agentDisksMetric.get("iPv4addr").asText());
                LocalDateTime instant = Instant.ofEpochSecond(
                                agentDisksValue.get(0).asLong())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                agentDiskInfo.put("time", instant.toString());

                ArrayNode tempDiskInfo = objectMapper.createArrayNode();
                for (JsonNode agent : agentDisksResult) {
                    JsonNode agentDisk = agent.get("metric");
                    ObjectNode temp = objectMapper.createObjectNode();
                    temp.put("diskName", agentDisk.get("diskUsage").asText());
                    temp.put("diskUsage", agentDisk.get("diskUsage").asText());
                    temp.put("diskValue", agent.get("value").get(1).asLong());
                    tempDiskInfo.add(temp);
                }
                agentDiskInfo.set("diskInfo", tempDiskInfo);
                return agentDiskInfo;
            }
        }
        return objectMapper.createObjectNode();
    }

    public JsonNode queryAgentDiskIO(String iPv4addr) {
        ObjectMapper objectMapper = new ObjectMapper();
        String params = String.format("agent_host_monitoring_diskIO{iPv4addr=\"%s\"}", iPv4addr);
        JsonNode result = query(params);
        if (result != null) {
            JsonNode agentDisksResult = result.get("data").get("result");
            if (agentDisksResult.isArray() && !agentDisksResult.isEmpty()) {
                JsonNode agentDisksValue = agentDisksResult.get(0).get("value");
                JsonNode agentDisksMetric = agentDisksResult.get(0).get("metric");
                ObjectNode agentDiskIOInfo = objectMapper.createObjectNode();
                agentDiskIOInfo
                        .put("hostname", agentDisksMetric.get("hostname").asText())
                        .put("iPv4addr", agentDisksMetric.get("iPv4addr").asText());
                LocalDateTime instant = Instant.ofEpochSecond(
                                agentDisksValue.get(0).asLong())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                agentDiskIOInfo.put("time", instant.toString());

                ArrayNode tempDiskInfo = objectMapper.createArrayNode();
                for (JsonNode agent : agentDisksResult) {
                    JsonNode agentDisk = agent.get("metric");
                    ObjectNode temp = objectMapper.createObjectNode();
                    temp.put(
                            "physicalDiskName",
                            agentDisk.get("physicalDiskName").asText());
                    temp.put("physicalDiskUsage", agentDisk.get("diskIO").asText());
                    if (Objects.equals(agentDisk.get("diskIO").asText(), "physicalDiskWrite")) {
                        temp.put("physicalDiskWrite", agent.get("value").get(1).asLong());
                    } else {
                        temp.put("physicalDiskRead", agent.get("value").get(1).asLong());
                    }
                    tempDiskInfo.add(temp);
                }
                agentDiskIOInfo.set("diskIO", tempDiskInfo);
                return agentDiskIOInfo;
            }
        }
        return objectMapper.createObjectNode();
    }
}
