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

import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Component
public class PrometheusProxy {

    private final WebClient webClient;

    @Resource
    private BigtopProxy bigtopProxy;

    @Value("${monitoring.agent-host-job-name}")
    private String agentHostJobName;

    public PrometheusProxy(
            WebClient.Builder webClientBuilder, @Value("${monitoring.prometheus-host}") String prometheusHost) {
        this.webClient = webClientBuilder.baseUrl(prometheusHost).build();
    }

    /**
     * query agents healthy
     */
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

    /**
     * query instant data
     */
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

    /**
     * query a range of data
     */
    public JsonNode queryRange(String query, long start, long end, String step) {
        Mono<JsonNode> body = webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/query_range")
                        .queryParam("query", query)
                        .queryParam("start", start)
                        .queryParam("end", end)
                        .queryParam("step", step)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class);

        JsonNode result = body.block();
        if (result == null
                || result.isEmpty()
                || !"success".equals(result.path("status").asText("failure"))) {
            return null;
        }
        return result;
    }

    /**
     * query agents list
     */
    private JsonNode queryAgentsList() {
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

    /**
     * query agents info
     */
    public JsonNode queryAgentsInfo() {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode agentsInfo = objectMapper.createArrayNode();
        JsonNode agents = queryAgentsList().get("iPv4addr"); // get all host
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
            temp.set("diskSpace", diskResult.get("diskInfo"));
            // DISK IO
            temp.set("diskIO", diskIOResult.get("diskIO"));
            agentsInfo.add(temp);
        }
        return agentsInfo;
    }

    /**
     * query cluster info
     */
    public JsonNode queryClusterInfo(String clusterId, String step) {
        JsonNode agentsIpv4 = bigtopProxy.queryClusterAgentsList(clusterId).get("hosts"); // cluster's agents
        ObjectMapper mapper = new ObjectMapper();
        int totalCpuCores = 0;
        long totalMemSpace = 0L;
        long totalDiskSpace = 0L;
        long totalMemIdle = 0L;
        double instantCpuUsage = 0.0;

        ObjectNode clusterInfo = mapper.createObjectNode();
        for (JsonNode agentIpv4 : agentsIpv4) {
            JsonNode agentCpuStep = queryAgentCpu(agentIpv4.asText(), step);
            // JsonNode agentMemStep = queryAgentMemory(agentsIpv4.asText(),step);
            JsonNode agentMem = queryAgentMemory(agentsIpv4.asText());
            JsonNode agentDisk = queryAgentDisk(agentIpv4.asText());
            JsonNode agentCpu = queryAgentCpu(agentIpv4.asText());

            instantCpuUsage += agentCpu.get("cpuUsage").asDouble()
                    * agentCpuStep.get("physical_cores").asInt();
            totalMemIdle += agentMem.get("memIdle").asLong();
            totalCpuCores += agentCpuStep.get("physical_cores").asInt();
            totalMemSpace += agentMem.get(("memTotal")).asLong();
            for (JsonNode diskInfo : agentDisk.get("diskInfo")) {
                if (Objects.equals(diskInfo.get("diskTotalSpace").asText(), "diskTotalSpace")) {
                    totalDiskSpace += diskInfo.get("diskValue").asLong();
                }
            }

            //            JsonNode agentCpuUsage = agentCpuStep.get("cpuUsage");
            //            JsonNode memIdle = agentMemStep.get("memIdle");
            //            JsonNode memTotal = agentMemStep.get("memTotal");
            //            for(int i = 0; i < 7;i++){
            //                if (i < memUsage.size()) {
            //
            //                    long currentMemIdleValue = memIdle.get(i).asLong();
            //                    long currentMemTotalValue = memTotal.get(i).asLong();
            //                    long newValue =
            //
            //
            //                    ((ArrayNode) memUsage).set(i, );
            //                } else if (indexToUpdate < arrayNode.size()) {
            //                    ((ArrayNode) arrayNode).set(indexToUpdate, factory.numberNode(valueToAdd));
            //                }
            // mem.add(i,mem.get(i) + memIdle.get(0).asLong() );
        }

        clusterInfo.put("total_physical_cores", totalCpuCores);
        clusterInfo.put("total_memory", totalMemSpace);
        clusterInfo.put("total_disk", totalDiskSpace);
        clusterInfo.put("cpu_usage_cur", instantCpuUsage / totalCpuCores);
        clusterInfo.put("memory_usage_cur", totalMemIdle / totalMemSpace);
        //        clusterInfo.put("cpu_usage",);
        //        clusterInfo.put("memory_usage",);
        return clusterInfo;
    }

    /**
     * query cpu filter by step
     */
    public JsonNode queryAgentCpu(String iPv4addr, String step) {
        String params = String.format("agent_host_monitoring_cpu{iPv4addr=\"%s\"})", iPv4addr);
        ArrayList<Long> timeStampsList = getTimeStampsList(Integer.parseInt(step)); // sum 8 and between 7
        JsonNode result = queryRange(
                params, timeStampsList.get(timeStampsList.size() - 1), timeStampsList.get(0), step); // end start
        ObjectMapper objectMapper = new ObjectMapper();
        if (result != null) {
            JsonNode agentCpu = result.get("data").get("result"); // differ type cpu
            if (agentCpu.isArray() && !agentCpu.isEmpty()) {
                ObjectNode agentCpuInfo = objectMapper.createObjectNode();
                // metric
                JsonNode agentCpuMetrics = agentCpuInfo.get(0).get("metric");
                agentCpuInfo.put("hostname", agentCpuMetrics.get("hostname").asText());
                agentCpuInfo.put("cpuInfo", agentCpuMetrics.get("cpu_info").asText());
                agentCpuInfo.put("iPv4addr", agentCpuMetrics.get("iPv4addr").asText());
                agentCpuInfo.put("os", agentCpuMetrics.get("os").asText());
                agentCpuInfo.put("architecture", agentCpuMetrics.get("arch").asText());
                agentCpuInfo.put(
                        "physical_cores", agentCpuMetrics.get("physical_cores").asText());
                agentCpuInfo.put(
                        "fileOpenDescriptor",
                        agentCpuMetrics.get("fileOpenDescriptor").asLong());
                agentCpuInfo.put(
                        "fileTotalDescriptor",
                        agentCpuMetrics.get("fileTotalDescriptor").asLong());

                // value
                for (JsonNode cpuType : agentCpu) {
                    JsonNode agentCpuValues = cpuType.get("values");
                    ArrayNode cpuValues = objectMapper.createArrayNode();
                    for (JsonNode stepValue : agentCpuValues) { // by step
                        cpuValues.add(stepValue.get(1).asDouble());
                    }
                    agentCpuInfo.set(cpuType.get("metric").get("cpuUsage").asText(), cpuValues);
                    return agentCpuInfo;
                }
            }
        }
        return objectMapper.createObjectNode();
    }

    /**
     * query agent cpu by ipv4
     */
    public JsonNode queryAgentCpu(String iPv4addr) {
        String params = String.format("agent_host_monitoring_cpu{iPv4addr=\"%s\"}", iPv4addr);
        JsonNode result = query(params);
        ObjectMapper objectMapper = new ObjectMapper();
        if (result != null) {
            JsonNode agentCpus = result.get("data").get("result");
            if (agentCpus.isArray() && !agentCpus.isEmpty()) { // differ cpu on an agent
                // metric
                JsonNode agentCpuMetric = agentCpus.get(0).get("metric");
                ObjectNode agentInfo = objectMapper.createObjectNode();
                agentInfo.put("hostname", agentCpuMetric.get("hostname").asText());
                agentInfo.put("cpuInfo", agentCpuMetric.get("cpu_info").asText());
                agentInfo.put("iPv4addr", agentCpuMetric.get("iPv4addr").asText());
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

                // value
                JsonNode agentCpuValue = agentCpus.get(0).get("value");
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

    /**
     * query agent memory by ipv4
     */
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

    /**
     * query mem filter by step
     */
    public JsonNode queryAgentMemory(String iPv4addr, String step) {
        String params = String.format("agent_host_monitoring_mem{iPv4addr=\"%s\"}", iPv4addr);
        ArrayList<Long> timeStampsList = getTimeStampsList(Integer.parseInt(step)); // sum 8 and between 7
        JsonNode result = queryRange(
                params, timeStampsList.get(timeStampsList.size() - 1), timeStampsList.get(0), step); // end start
        ObjectMapper objectMapper = new ObjectMapper();
        if (result != null) {
            JsonNode agentMem = result.get("data").get("result");
            if (agentMem.isArray() && !agentMem.isEmpty()) { // differ memory
                ObjectNode agentMemInfo = objectMapper.createObjectNode();
                // metric
                JsonNode agentMemMetrics = agentMem.get(0).get("metric");
                agentMemInfo.put("hostname", agentMemMetrics.get("hostname").asText());
                agentMemInfo.put("iPv4addr", agentMemMetrics.get("iPv4addr").asText());
                agentMemInfo.put("memTotal", agentMemMetrics.get("memTotal").asText());

                // value
                for (JsonNode stepAgent : agentMem) {
                    JsonNode agentMemValues = stepAgent.get("value");
                    ArrayNode memValues = objectMapper.createArrayNode();
                    for (JsonNode value : agentMemValues) {
                        memValues.add(value.get(1).asDouble());
                    }
                    agentMemInfo.set(agentMemMetrics.get("memUsage").asText(), memValues);
                    return agentMemInfo;
                }
            }
        }
        return objectMapper.createObjectNode();
    }

    /**
     * query agent disk by ipv4
     */
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
                    temp.put("diskName", agentDisk.get("diskName").asText());
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

    /**
     * query agent diskIO by ipv4
     */
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

    /**
     * get time step
     */
    public ArrayList<Long> getTimeStampsList(int step) {
        // format
        String currentTimeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String currentDateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime currentDateTime = LocalDateTime.parse(currentDateStr + " " + currentTimeStr, formatter);
        int roundedMinute = (currentDateTime.getMinute() / step) * step;
        LocalDateTime roundedCurrentDateTime =
                currentDateTime.withMinute(roundedMinute).withSecond(0).withNano(0);
        // get 8 node
        ArrayList<Long> timestamps = new ArrayList<>();
        ZoneId zid = ZoneId.systemDefault();
        for (int i = 0; i < 7; i++) {
            LocalDateTime pastTime = roundedCurrentDateTime.minus(Duration.ofMinutes((long) step * i));
            long timestamp = pastTime.atZone(zid).toInstant().toEpochMilli() / 1000L;
            timestamps.add(timestamp);
        }
        return timestamps;
    }
}
