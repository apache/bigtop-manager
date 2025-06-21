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

import org.apache.bigtop.manager.server.utils.ProxyUtils;

import org.springframework.http.MediaType;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PrometheusProxy {

    private final WebClient webClient;

    public static final String MEM_IDLE = "memIdle";
    public static final String MEM_TOTAL = "memTotal";
    public static final String DISK_IDLE = "diskFreeSpace";
    public static final String DISK_TOTAL = "diskTotalSpace";
    public static final String FILE_OPEN_DESCRIPTOR = "fileOpenDescriptor";
    public static final String FILE_TOTAL_DESCRIPTOR = "fileTotalDescriptor";
    public static final String CPU_LOAD_AVG_MIN_1 = "cpuLoadAvgMin_1";
    public static final String CPU_LOAD_AVG_MIN_5 = "cpuLoadAvgMin_5";
    public static final String CPU_LOAD_AVG_MIN_15 = "cpuLoadAvgMin_15";
    public static final String CPU_USAGE = "cpuUsage";
    public static final String PHYSICAL_CORES = "physical_cores";
    public static final String DISK_READ = "diskRead";
    public static final String DISK_WRITE = "diskWrite";

    public PrometheusProxy(String prometheusHost, Integer prometheusPort) {
        this.webClient = WebClient.builder()
                .baseUrl("http://" + prometheusHost + ":" + prometheusPort)
                .build();
    }
    /**
     * Retrieve current data
     */
    public JsonNode query(String params) {
        Mono<JsonNode> body = webClient
                .post()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/query").build())
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
     * Retrieve data with a specified interval before the current time
     */
    public JsonNode queryRange(String query, long start, long end, String step) {
        Mono<JsonNode> body = webClient
                .post()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/query_range").build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("query", query)
                        .with("timeout", "10")
                        .with("start", String.valueOf(start))
                        .with("end", String.valueOf(end))
                        .with("step", step))
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
     * query agents healthy
     */
    public JsonNode queryAgentsHealthyStatus() {
        JsonNode result = query("up{job=\"%s\"}".formatted("bm-agent-host"));
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode agentsHealthyStatus = objectMapper.createArrayNode();
        if (result != null) {
            JsonNode agents = result.get("data").get("result");
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
        return objectMapper.createObjectNode();
    }
    /**
     * query agents info interval
     */
    public JsonNode queryAgentsInfo(String agentIpv4, String interval) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (!Objects.equals(agentIpv4, "")) {
            ObjectNode ag = objectMapper.createObjectNode();
            double[] agentsCpuUsage = new double[6];
            double[] agentsCpuLoad1 = new double[6];
            double[] agentsCpuLoad2 = new double[6];
            double[] agentsCpuLoad3 = new double[6];
            long[] agentMemIdle = new long[6];
            long[] agentMemTotal = new long[6];
            long[] agentDiskRead = new long[6];
            long[] agentDiskWrite = new long[6];

            JsonNode agentCpu = retrieveAgentCpu(agentIpv4);
            JsonNode agentMem = retrieveAgentMemory(agentIpv4);
            JsonNode agentDisk = retrieveAgentDisk(agentIpv4);
            JsonNode agentDiskIO = retrieveAgentDiskIO(agentIpv4);
            JsonNode agentCpuInterval = retrieveAgentCpu(agentIpv4, interval);
            JsonNode agentMemInterval = retrieveAgentMemory(agentIpv4, interval);
            JsonNode agentDiskIOInterval = retrieveAgentDiskIO(agentIpv4, interval);

            // data process
            for (int i = 0; i < 6; i++) {
                // CPU
                agentsCpuUsage[i] = ProxyUtils.getDoubleSafely(agentCpuInterval, CPU_USAGE, i);
                agentsCpuLoad1[i] = ProxyUtils.getDoubleSafely(agentCpuInterval, CPU_LOAD_AVG_MIN_1, i);
                agentsCpuLoad2[i] = ProxyUtils.getDoubleSafely(agentCpuInterval, CPU_LOAD_AVG_MIN_5, i);
                agentsCpuLoad3[i] = ProxyUtils.getDoubleSafely(agentCpuInterval, CPU_LOAD_AVG_MIN_15, i);

                // MEM
                agentMemIdle[i] = ProxyUtils.getLongSafely(agentMemInterval, MEM_IDLE, i);
                agentMemTotal[i] = ProxyUtils.getLongSafely(agentMemInterval, MEM_TOTAL, i);

                // DISK IO
                agentDiskRead[i] = ProxyUtils.getLongSafely(agentDiskIOInterval, DISK_READ, i);
                agentDiskWrite[i] = ProxyUtils.getLongSafely(agentDiskIOInterval, DISK_WRITE, i);
            }

            // cur
            ag.put("cpuUsageCur", String.format("%.6f", agentCpu.get(CPU_USAGE).asDouble()));
            ag.put(
                    "memoryUsageCur",
                    String.format(
                            "%.6f",
                            (double) (agentMem.get(MEM_TOTAL).asLong()
                                            - agentMem.get(MEM_IDLE).asLong())
                                    / agentMem.get(MEM_TOTAL).asLong()));
            ag.put(
                    "diskUsageCur",
                    String.format(
                            "%.6f",
                            (double) (agentDisk.get(DISK_TOTAL).asLong()
                                            - agentDisk.get(DISK_IDLE).asLong())
                                    / agentDisk.get(DISK_TOTAL).asLong()));
            ag.put(
                    "fileDescriptorUsage",
                    String.format(
                            "%.6f",
                            (double) agentCpu.get(FILE_OPEN_DESCRIPTOR).asLong()
                                    / agentCpu.get(FILE_TOTAL_DESCRIPTOR).asLong()));
            ag.put("diskReadCur", agentDiskIO.get(DISK_READ).asLong());
            ag.put("diskWriteCur", agentDiskIO.get(DISK_WRITE).asLong());

            // interval
            ag.set("cpuUsage", ProxyUtils.array2node(agentsCpuUsage));
            ag.set("systemLoad1", ProxyUtils.array2node(agentsCpuLoad1));
            ag.set("systemLoad2", ProxyUtils.array2node(agentsCpuLoad2));
            ag.set("systemLoad3", ProxyUtils.array2node(agentsCpuLoad3));
            ag.set("memoryUsage", ProxyUtils.array2node(agentMemIdle, agentMemTotal));
            ag.set("diskRead", ProxyUtils.array2node(agentDiskRead));
            ag.set("diskWrite", ProxyUtils.array2node(agentDiskWrite));

            return ag;
        }
        return objectMapper.createObjectNode();
    }
    /**
     * query clusters info interval
     */
    public JsonNode queryClustersInfo(List<String> agentIpv4s, String interval) {
        int agentsNum = agentIpv4s.size(); // change to agentsNum
        ObjectMapper objectMapper = new ObjectMapper();
        if (agentsNum > 0) {
            int totalPhysicalCores = 0;
            long totalMemSpace = 0L, totalMemIdle = 0L;
            double instantCpuUsage = 0.0;
            double[][] agentsCpuUsage = new double[agentsNum][6];
            double[][] agentsCpuLoad1 = new double[agentsNum][6];
            double[][] agentsCpuLoad2 = new double[agentsNum][6];
            double[][] agentsCpuLoad3 = new double[agentsNum][6];
            long[][] agentMemIdle = new long[agentsNum][6];
            long[][] agentMemTotal = new long[agentsNum][6];

            int agentIndex = 0;
            ObjectNode clusterInfo = objectMapper.createObjectNode();
            for (String agentIpv4 : agentIpv4s) {
                JsonNode agentCpu = retrieveAgentCpu(agentIpv4);
                instantCpuUsage += agentCpu.get("cpuUsage").asDouble()
                        * agentCpu.get(PHYSICAL_CORES).asInt();
                JsonNode agentMem = retrieveAgentMemory(agentIpv4);
                totalMemIdle += agentMem.get("memIdle").asLong();
                totalMemSpace += agentMem.get(("memTotal")).asLong();
                JsonNode agentCpuStep = retrieveAgentCpu(agentIpv4, interval);
                JsonNode agentMemStep = retrieveAgentMemory(agentIpv4, interval);
                int agentPhysicalCores = agentCpu.get(PHYSICAL_CORES).asInt();
                totalPhysicalCores += agentPhysicalCores;

                for (int i = 0; i < 6; i++) {
                    // CPU
                    agentsCpuUsage[agentIndex][i] =
                            ProxyUtils.getDoubleSafely(agentCpuStep, CPU_USAGE, i) * agentPhysicalCores;
                    agentsCpuLoad1[agentIndex][i] =
                            ProxyUtils.getDoubleSafely(agentCpuStep, CPU_LOAD_AVG_MIN_1, i) * agentPhysicalCores;
                    agentsCpuLoad2[agentIndex][i] =
                            ProxyUtils.getDoubleSafely(agentCpuStep, CPU_LOAD_AVG_MIN_5, i) * agentPhysicalCores;
                    agentsCpuLoad3[agentIndex][i] =
                            ProxyUtils.getDoubleSafely(agentCpuStep, CPU_LOAD_AVG_MIN_15, i) * agentPhysicalCores;

                    // MEM
                    agentMemIdle[agentIndex][i] = ProxyUtils.getLongSafely(agentMemStep, MEM_IDLE, i);
                    agentMemTotal[agentIndex][i] = ProxyUtils.getLongSafely(agentMemStep, MEM_TOTAL, i);
                }

                agentIndex++; // loop of agents
            }
            // cur
            clusterInfo.put("cpuUsageCur", String.format("%.6f", instantCpuUsage / totalPhysicalCores));
            clusterInfo.put(
                    "memoryUsageCur", String.format("%.6f", (double) (totalMemSpace - totalMemIdle) / totalMemSpace));

            // interval
            clusterInfo.set("cpuUsage", ProxyUtils.array2node(agentsCpuUsage, totalPhysicalCores, agentsNum));
            clusterInfo.set("systemLoad1", ProxyUtils.array2node(agentsCpuLoad1, totalPhysicalCores, agentsNum));
            clusterInfo.set("systemLoad2", ProxyUtils.array2node(agentsCpuLoad2, totalPhysicalCores, agentsNum));
            clusterInfo.set("systemLoad3", ProxyUtils.array2node(agentsCpuLoad3, totalPhysicalCores, agentsNum));
            clusterInfo.set("memoryUsage", ProxyUtils.array2node(agentMemIdle, agentMemTotal, agentsNum));

            return clusterInfo;
        }
        return objectMapper.createObjectNode();
    }

    /**
     * retrieve cpu
     */
    public JsonNode retrieveAgentCpu(String iPv4addr) {
        String params = String.format("agent_host_monitoring_cpu{iPv4addr=\"%s\"}", iPv4addr);
        JsonNode result = query(params);
        ObjectMapper objectMapper = new ObjectMapper();
        if (result != null) {
            JsonNode agentCpus = result.get("data").get("result");
            if (agentCpus.isArray() && !agentCpus.isEmpty()) {
                // metric
                JsonNode agentCpuMetric = agentCpus.get(0).get("metric");
                ObjectNode agentInfo = objectMapper.createObjectNode();
                agentInfo.put("hostname", agentCpuMetric.get("hostname").asText());
                agentInfo.put(
                        "cpuInfo",
                        agentCpuMetric.get("cpu_info") == null
                                ? ""
                                : agentCpuMetric.get("cpu_info").asText());
                agentInfo.put("iPv4addr", agentCpuMetric.get("iPv4addr").asText());
                agentInfo.put("os", agentCpuMetric.get("os").asText());
                agentInfo.put("architecture", agentCpuMetric.get("arch").asText());
                agentInfo.put(PHYSICAL_CORES, agentCpuMetric.get(PHYSICAL_CORES).asText());
                agentInfo.put(
                        FILE_OPEN_DESCRIPTOR,
                        agentCpuMetric.get(FILE_OPEN_DESCRIPTOR).asLong());
                agentInfo.put(
                        FILE_TOTAL_DESCRIPTOR,
                        agentCpuMetric.get(FILE_TOTAL_DESCRIPTOR).asLong());

                // value
                for (JsonNode agent : agentCpus) {
                    agentInfo.put(
                            agent.get("metric").get("cpuUsage").asText(),
                            agent.get("value").get(1).asDouble());
                }
                return agentInfo;
            }
        }
        return objectMapper.createObjectNode();
    }
    /**
     * retrieve cpu interval
     */
    public JsonNode retrieveAgentCpu(String iPv4addr, String interval) {
        String params = String.format("agent_host_monitoring_cpu{iPv4addr=\"%s\"}", iPv4addr);
        ArrayList<Long> timeStampsList = ProxyUtils.getTimeStampsList(ProxyUtils.processInternal(interval));
        JsonNode result = queryRange(
                params,
                timeStampsList.get(timeStampsList.size() - 1),
                timeStampsList.get(0),
                ProxyUtils.Number2Param(ProxyUtils.processInternal(interval))); // end start
        ObjectMapper objectMapper = new ObjectMapper();
        if (result != null) {
            JsonNode agentCpu = result.get("data").get("result");
            if (agentCpu.isArray() && !agentCpu.isEmpty()) {
                ObjectNode agentCpuInfo = objectMapper.createObjectNode();
                // metric
                JsonNode agentCpuMetrics = agentCpu.get(0).get("metric");
                agentCpuInfo.put("hostname", agentCpuMetrics.get("hostname").asText());
                agentCpuInfo.put(
                        "cpuInfo",
                        agentCpuInfo.get("cpu_info") == null
                                ? ""
                                : agentCpuInfo.get("cpu_info").asText());
                agentCpuInfo.put("iPv4addr", agentCpuMetrics.get("iPv4addr").asText());
                agentCpuInfo.put("os", agentCpuMetrics.get("os").asText());
                agentCpuInfo.put("architecture", agentCpuMetrics.get("arch").asText());
                agentCpuInfo.put(
                        PHYSICAL_CORES, agentCpuMetrics.get(PHYSICAL_CORES).asInt());
                agentCpuInfo.put(
                        FILE_OPEN_DESCRIPTOR,
                        agentCpuMetrics.get(FILE_OPEN_DESCRIPTOR).asLong());
                agentCpuInfo.put(
                        FILE_TOTAL_DESCRIPTOR,
                        agentCpuMetrics.get(FILE_TOTAL_DESCRIPTOR).asLong());

                // value
                for (JsonNode cpuType : agentCpu) {
                    JsonNode agentCpuValues = cpuType.get("values");
                    ArrayNode cpuValues = objectMapper.createArrayNode();
                    for (JsonNode stepValue : agentCpuValues) {
                        cpuValues.add(stepValue.get(1).asDouble());
                    }
                    agentCpuInfo.set(cpuType.get("metric").get("cpuUsage").asText(), cpuValues);
                }
                return agentCpuInfo;
            }
        }
        return objectMapper.createObjectNode();
    }

    /**
     * retrieve memory
     */
    public JsonNode retrieveAgentMemory(String iPv4addr) {
        String query = String.format("agent_host_monitoring_mem{iPv4addr=\"%s\"}", iPv4addr);
        JsonNode result = query(query);
        ObjectMapper objectMapper = new ObjectMapper();
        if (result != null) {
            JsonNode agentsMem = result.get("data").get("result");
            if (agentsMem.isArray() && !agentsMem.isEmpty()) {
                ObjectNode agentsMemInfo = objectMapper.createObjectNode();
                // metric
                JsonNode agentMemMetric = agentsMem.get(0).get("metric");
                agentsMemInfo.put("hostname", agentMemMetric.get("hostname").asText());
                agentsMemInfo.put("iPv4addr", agentMemMetric.get("iPv4addr").asText());
                for (JsonNode agent : agentsMem) {
                    agentsMemInfo.put(
                            agent.get("metric").get("memUsage").asText(),
                            agent.get("value").get(1).asLong()); // mem metric
                }
                return agentsMemInfo;
            }
        }
        return objectMapper.createObjectNode();
    }
    /**
     * retrieve memory interval
     */
    public JsonNode retrieveAgentMemory(String iPv4addr, String interval) {
        String params = String.format("agent_host_monitoring_mem{iPv4addr=\"%s\"}", iPv4addr);
        ArrayList<Long> timeStampsList = ProxyUtils.getTimeStampsList(ProxyUtils.processInternal(interval));
        JsonNode result = queryRange(
                params,
                timeStampsList.get(timeStampsList.size() - 1),
                timeStampsList.get(0),
                ProxyUtils.Number2Param(ProxyUtils.processInternal(interval)));
        ObjectMapper objectMapper = new ObjectMapper();
        if (result != null) {
            JsonNode agentMem = result.get("data").get("result");
            if (agentMem.isArray() && !agentMem.isEmpty()) {
                ObjectNode agentMemInfo = objectMapper.createObjectNode();
                // metric
                JsonNode agentMemMetrics = agentMem.get(0).get("metric");
                agentMemInfo.put("hostname", agentMemMetrics.get("hostname").asText());
                agentMemInfo.put("iPv4addr", agentMemMetrics.get("iPv4addr").asText());

                // value
                for (JsonNode stepAgent : agentMem) {
                    JsonNode agentMemValues = stepAgent.get("values");
                    ArrayNode memValues = objectMapper.createArrayNode();
                    for (JsonNode value : agentMemValues) {
                        memValues.add(value.get(1).asDouble());
                    }
                    agentMemInfo.set(stepAgent.get("metric").get("memUsage").asText(), memValues);
                }
                return agentMemInfo;
            }
        }
        return objectMapper.createObjectNode();
    }

    /**
     * retrieve diskInfo
     */
    public JsonNode retrieveAgentDisk(String iPv4addr) {
        String params = String.format("agent_host_monitoring_disk{iPv4addr=\"%s\"}", iPv4addr);
        JsonNode result = query(params);
        ObjectMapper objectMapper = new ObjectMapper();
        if (result != null) {
            JsonNode agentDisksResult = result.get("data").get("result");
            if (agentDisksResult.isArray() && !agentDisksResult.isEmpty()) {
                ObjectNode agentDiskInfo = objectMapper.createObjectNode();
                // metric
                JsonNode agentDisksMetric = agentDisksResult.get(0).get("metric");
                agentDiskInfo.put("hostname", agentDisksMetric.get("hostname").asText());
                agentDiskInfo.put("iPv4addr", agentDisksMetric.get("iPv4addr").asText());

                // value
                Long diskTotalSpace = 0L, diskFreeSpace = 0L;
                for (JsonNode disk : agentDisksResult) {
                    if (Objects.equals(disk.get("metric").get("diskUsage").asText(), DISK_IDLE)) {
                        diskFreeSpace += disk.get("value").get(1).asLong();
                    } else {
                        diskTotalSpace += disk.get("value").get(1).asLong();
                    }
                }
                agentDiskInfo.put(DISK_TOTAL, diskTotalSpace);
                agentDiskInfo.put(DISK_IDLE, diskFreeSpace);
                return agentDiskInfo;
            }
        }
        return objectMapper.createObjectNode();
    }

    /**
     * retrieve diskIO
     */
    public JsonNode retrieveAgentDiskIO(String iPv4addr) {
        String params = String.format("agent_host_monitoring_diskIO{iPv4addr=\"%s\"}", iPv4addr);
        JsonNode result = query(params);
        ObjectMapper objectMapper = new ObjectMapper();
        if (result != null) {
            JsonNode agentDisksResult = result.get("data").get("result");
            if (agentDisksResult.isArray() && !agentDisksResult.isEmpty()) {
                ObjectNode agentDiskIOInfo = objectMapper.createObjectNode();
                // metric
                JsonNode agentDisksMetric = agentDisksResult.get(0).get("metric");
                agentDiskIOInfo
                        .put("hostname", agentDisksMetric.get("hostname").asText())
                        .put("iPv4addr", agentDisksMetric.get("iPv4addr").asText());

                // value
                long diskWrite = 0L;
                long diskRead = 0L;
                for (JsonNode disk : agentDisksResult) {
                    if (Objects.equals(disk.get("metric").get("diskIO").asText(), DISK_WRITE)) {
                        diskWrite += disk.get("value").get(1).asLong();
                    } else {
                        diskRead += disk.get("value").get(1).asLong();
                    }
                }
                agentDiskIOInfo.put(DISK_WRITE, diskWrite);
                agentDiskIOInfo.put(DISK_READ, diskRead);
                return agentDiskIOInfo;
            }
        }
        return objectMapper.createObjectNode();
    }

    /**
     * retrieve diskIO interval
     */
    public JsonNode retrieveAgentDiskIO(String iPv4addr, String interval) {
        String params = String.format("agent_host_monitoring_diskIO{iPv4addr=\"%s\"}", iPv4addr);
        ArrayList<Long> timeStampsList = ProxyUtils.getTimeStampsList(ProxyUtils.processInternal(interval));
        JsonNode result = queryRange(
                params,
                timeStampsList.get(timeStampsList.size() - 1),
                timeStampsList.get(0),
                ProxyUtils.Number2Param(ProxyUtils.processInternal(interval)));
        ObjectMapper objectMapper = new ObjectMapper();
        if (result != null) {
            JsonNode agentDisksResult = result.get("data").get("result");
            if (agentDisksResult.isArray() && !agentDisksResult.isEmpty()) {
                ObjectNode agentDiskIOInfo = objectMapper.createObjectNode();
                // metric
                JsonNode agentDisksMetric = agentDisksResult.get(0).get("metric");
                agentDiskIOInfo
                        .put("hostname", agentDisksMetric.get("hostname").asText())
                        .put("iPv4addr", agentDisksMetric.get("iPv4addr").asText());

                // value
                long[] diskWrite = new long[6];
                long[] diskRead = new long[6];
                for (JsonNode disk : agentDisksResult) {
                    if (Objects.equals(disk.get("metric").get("diskIO").asText(), DISK_WRITE)) {
                        for (int i = 0; i < 6; i++) {
                            JsonNode listNode = disk.get("values");
                            if (listNode != null && listNode.isArray() && i < listNode.size())
                                diskWrite[i] += listNode.get(i).get(1).asLong();
                            else diskWrite[i] += 0L;
                        }
                    } else {
                        for (int i = 0; i < 6; i++) {
                            JsonNode listNode = disk.get("values");
                            if (listNode != null && listNode.isArray() && i < listNode.size())
                                diskRead[i] += listNode.get(i).get(1).asLong();
                            else diskRead[i] += 0L;
                        }
                    }
                }
                agentDiskIOInfo.set(DISK_WRITE, ProxyUtils.array2node(diskWrite));
                agentDiskIOInfo.set(DISK_READ, ProxyUtils.array2node(diskRead));
                return agentDiskIOInfo;
            }
        }
        return objectMapper.createObjectNode();
    }
}
