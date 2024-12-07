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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    public PrometheusProxy(
            WebClient.Builder webClientBuilder, @Value("${monitoring.prometheus-host}") String prometheusHost) {
        this.webClient = webClientBuilder.baseUrl(prometheusHost).build();
    }
    /**
     * Retrieve current data in real-time
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
        JsonNode result = query("up{job=\"%s\"}".formatted(agentHostJobName));
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
     * query agents ipv4 list
     */
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
                ObjectNode node = objectMapper.createObjectNode();
                node.put("agentsNum", iPv4addrArray.size());
                node.set("iPv4addr", iPv4addrArray);
                return node; // iPv4
            }
        }
        return objectMapper.createObjectNode();
    }

    /**
     * query agents static info
     */
    public JsonNode queryAgentsInfo() {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode agentsInfo = objectMapper.createArrayNode();
        JsonNode agents = queryAgents().get("iPv4addr"); // get all host
        for (JsonNode agent : agents) {
            JsonNode cpuResult = retrieveAgentCpu(agent.asText());
            JsonNode memResult = retrieveAgentMemory(agent.asText());
            JsonNode diskResult = queryAgentDisk(agent.asText());
            ObjectNode temp = objectMapper.createObjectNode();
            // HOST
            temp.put("hostname", cpuResult.get("hostname").asText());
            temp.put("iPv4addr", cpuResult.get("iPv4addr").asText());
            temp.put("cpuInfo", cpuResult.get("cpuInfo").asText().strip());
            // temp.put("iPv6addr", cpuResult.get("iPv6addr").asText());
            temp.put("os", cpuResult.get("os").asText());
            temp.put("architecture", cpuResult.get("architecture").asText());
            temp.put(PHYSICAL_CORES, cpuResult.get(PHYSICAL_CORES).asText());
            // MEM
            temp.put(MEM_TOTAL, memResult.get(MEM_TOTAL).asLong());
            // DISK
            temp.set(DISK_TOTAL, diskResult.get(DISK_TOTAL));
            agentsInfo.add(temp);
        }
        return agentsInfo;
    }

    /**
     * query agents dynamic info
     */
    public JsonNode queryAgentsInfo(String pace) {
        JsonNode agents = queryAgents();
        JsonNode agentsIpv4 = agents.get("iPv4addr");
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode agentsInfo = mapper.createArrayNode();
        for (JsonNode agentIpv4 : agentsIpv4) {
            ObjectNode ag = mapper.createObjectNode();
            double[] agentsCpuUsage = new double[6];
            double[] agentsCpuLoad1 = new double[6];
            double[] agentsCpuLoad2 = new double[6];
            double[] agentsCpuLoad3 = new double[6];
            long[] agentMemIdle = new long[6];
            long[] agentMemTotal = new long[6];
            long[] agentDiskRead = new long[6];
            long[] agentDiskWrite = new long[6];
            // real-time cpuUsage
            JsonNode agentCpu = retrieveAgentCpu(agentIpv4.asText());
            // real-time mem
            JsonNode agentMem = retrieveAgentMemory(agentIpv4.asText());
            // real-time disk
            JsonNode agentDisk = queryAgentDisk(agentIpv4.asText());
            // real-time diskIO
            JsonNode agentDiskIO = queryAgentDiskIO(agentIpv4.asText());

            // dynamic
            JsonNode agentCpuInterval = retrieveAgentCpu(agentIpv4.asText(), pace);
            JsonNode agentMemInterval = retrieveAgentMemory(agentIpv4.asText(), pace);
            JsonNode agentDiskIOInterval = queryAgentDiskIO(agentIpv4.asText(), pace);
            for (int i = 0; i < 6; i++) {
                // CPU
                System.out.println(agentCpuInterval);
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
            ag.put("cpu_usage_cur", agentCpu.get(CPU_USAGE).asDouble());
            ag.put(
                    "memory_usage_cur",
                    (double) (agentMem.get(MEM_TOTAL).asLong()
                                    - agentMem.get(MEM_IDLE).asLong())
                            / agentMem.get(MEM_TOTAL).asLong());
            ag.put(
                    "disk_usage_cur",
                    (double) (agentDisk.get(DISK_TOTAL).asLong()
                                    - agentDisk.get(DISK_IDLE).asLong())
                            / agentDisk.get(DISK_TOTAL).asLong());
            ag.put(
                    "file_descriptor_usage",
                    (double) agentCpu.get(FILE_OPEN_DESCRIPTOR).asLong()
                            / agentCpu.get(FILE_TOTAL_DESCRIPTOR).asLong());
            ag.put("disk_read", agentDiskIO.get(DISK_READ).asLong());
            ag.put("disk_read", agentDiskIO.get(DISK_WRITE).asLong());
            // cpu
            ag.set("cpu_usage", ProxyUtils.array2node(agentsCpuUsage));
            ag.set("system_load1", ProxyUtils.array2node(agentsCpuLoad1));
            ag.set("system_load2", ProxyUtils.array2node(agentsCpuLoad2));
            ag.set("system_load3", ProxyUtils.array2node(agentsCpuLoad3));
            // mem
            ag.set("memory_usage", ProxyUtils.array2node(agentMemIdle, agentMemTotal));
            // disk io
            ag.set("disk_read", ProxyUtils.array2node(agentDiskRead));
            ag.set("disk_write", ProxyUtils.array2node(agentDiskWrite));

            agentsInfo.add(ag);
        }
        return agentsInfo;
    }
    /**
     * query cluster info
     */
    public JsonNode queryClusterInfo(String clusterId, String pace) {
        JsonNode agents = bigtopProxy.queryClusterAgentsList(clusterId).get("agents"); // cluster's agents
        int agentsNum =
                bigtopProxy.queryClusterAgentsList(clusterId).get("agentsNum").asInt(); // agentsNum
        ObjectMapper mapper = new ObjectMapper();
        int total_physical_cores = 0;
        long totalMemSpace = 0L;
        long totalDiskSpace = 0L;
        long totalMemIdle = 0L;
        double instantCpuUsage = 0.0;
        double[][] agentsCpuUsage = new double[agentsNum][6];
        double[][] agentsCpuLoad1 = new double[agentsNum][6];
        double[][] agentsCpuLoad2 = new double[agentsNum][6];
        double[][] agentsCpuLoad3 = new double[agentsNum][6];
        long[][] agentMemIdle = new long[agentsNum][6];
        long[][] agentMemTotal = new long[agentsNum][6];
        int agentIndex = 0;
        ObjectNode clusterInfo = mapper.createObjectNode();
        for (JsonNode agentIpv4 : agents) {
            // real-time cpuUsage
            JsonNode agentCpu = retrieveAgentCpu(agentIpv4.asText());
            instantCpuUsage += agentCpu.get("cpuUsage").asDouble()
                    * agentCpu.get(PHYSICAL_CORES).asInt();
            // real-time mem
            JsonNode agentMem = retrieveAgentMemory(agents.asText());
            totalMemIdle += agentMem.get("memIdle").asLong();
            totalMemSpace += agentMem.get(("memTotal")).asLong();
            // real-time disk
            JsonNode agentDisk = queryAgentDisk(agentIpv4.asText());
            totalDiskSpace += agentDisk.get(DISK_TOTAL).asLong();

            // avg time
            JsonNode agentCpuStep = retrieveAgentCpu(agentIpv4.asText(), pace);
            JsonNode agentMemStep = retrieveAgentMemory(agents.asText(), pace);
            int agent_physical_cores = agentCpuStep.get(PHYSICAL_CORES).asInt();
            total_physical_cores += agent_physical_cores;
            for (int i = 0; i < 6; i++) {
                // CPU
                agentsCpuUsage[agentIndex][i] =
                        ProxyUtils.getDoubleSafely(agentCpuStep, CPU_USAGE, i) * agent_physical_cores;
                agentsCpuLoad1[agentIndex][i] =
                        ProxyUtils.getDoubleSafely(agentCpuStep, CPU_LOAD_AVG_MIN_1, i) * agent_physical_cores;
                agentsCpuLoad2[agentIndex][i] =
                        ProxyUtils.getDoubleSafely(agentCpuStep, CPU_LOAD_AVG_MIN_5, i) * agent_physical_cores;
                agentsCpuLoad3[agentIndex][i] =
                        ProxyUtils.getDoubleSafely(agentCpuStep, CPU_LOAD_AVG_MIN_15, i) * agent_physical_cores;

                // MEM
                agentMemIdle[agentIndex][i] = ProxyUtils.getLongSafely(agentMemStep, MEM_IDLE, i);
                agentMemTotal[agentIndex][i] = ProxyUtils.getLongSafely(agentMemStep, MEM_TOTAL, i);
            }
        }
        // static
        clusterInfo.put("total_physical_cores", total_physical_cores);
        clusterInfo.put("total_memory", totalMemSpace);
        clusterInfo.put("total_disk", totalDiskSpace);

        // cur
        clusterInfo.put("cpu_usage_cur", instantCpuUsage / total_physical_cores);
        clusterInfo.put("memory_usage_cur", (double) (totalMemSpace - totalMemIdle) / totalMemSpace);

        // cpu
        clusterInfo.set("cpu_usage", ProxyUtils.array2node(agentsCpuUsage, total_physical_cores, agentsNum));
        clusterInfo.set("system_load1", ProxyUtils.array2node(agentsCpuLoad1, total_physical_cores, agentsNum));
        clusterInfo.set("system_load2", ProxyUtils.array2node(agentsCpuLoad2, total_physical_cores, agentsNum));
        clusterInfo.set("system_load3", ProxyUtils.array2node(agentsCpuLoad3, total_physical_cores, agentsNum));

        // mem
        clusterInfo.set("memory_usage", ProxyUtils.array2node(agentMemIdle, agentMemTotal, agentsNum));
        return clusterInfo;
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
                agentInfo.put("cpuInfo", agentCpuMetric.get("cpu_info").asText());
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
     * retrieve cpu internal
     */
    public JsonNode retrieveAgentCpu(String iPv4addr, String pace) {
        String params = String.format("agent_host_monitoring_cpu{iPv4addr=\"%s\"}", iPv4addr);
        ArrayList<Long> timeStampsList = ProxyUtils.getTimeStampsList(Integer.parseInt(pace));
        JsonNode result = queryRange(
                params,
                timeStampsList.get(timeStampsList.size() - 1),
                timeStampsList.get(0),
                ProxyUtils.Number2Param(Integer.parseInt(pace))); // end start
        ObjectMapper objectMapper = new ObjectMapper();
        if (result != null) {
            JsonNode agentCpu = result.get("data").get("result");
            if (agentCpu.isArray() && !agentCpu.isEmpty()) {
                ObjectNode agentCpuInfo = objectMapper.createObjectNode();
                // metric
                JsonNode agentCpuMetrics = agentCpu.get(0).get("metric");
                agentCpuInfo.put("hostname", agentCpuMetrics.get("hostname").asText());
                agentCpuInfo.put("cpuInfo", agentCpuMetrics.get("cpu_info").asText());
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
     * retrieve memory internal
     */
    public JsonNode retrieveAgentMemory(String iPv4addr, String pace) {
        String params = String.format("agent_host_monitoring_mem{iPv4addr=\"%s\"}", iPv4addr);
        ArrayList<Long> timeStampsList = ProxyUtils.getTimeStampsList(Integer.parseInt(pace)); // sum 8 and between 7
        JsonNode result = queryRange(
                params,
                timeStampsList.get(timeStampsList.size() - 1),
                timeStampsList.get(0),
                ProxyUtils.Number2Param(Integer.parseInt(pace))); // end start
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
    public JsonNode queryAgentDisk(String iPv4addr) {
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
                for (JsonNode agent : agentDisksResult) {
                    if (Objects.equals(agent.get("metric").get("diskUsage").asText(), DISK_IDLE)) {
                        diskFreeSpace += agent.get("value").get(1).asLong();
                    } else {
                        diskTotalSpace += agent.get("value").get(1).asLong();
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
    public JsonNode queryAgentDiskIO(String iPv4addr) {
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
     * retrieve diskIO internal
     */
    public JsonNode queryAgentDiskIO(String iPv4addr, String step) {
        String params = String.format("agent_host_monitoring_diskIO{iPv4addr=\"%s\"}", iPv4addr);
        ArrayList<Long> timeStampsList = ProxyUtils.getTimeStampsList(Integer.parseInt(step));
        JsonNode result = queryRange(
                params,
                timeStampsList.get(timeStampsList.size() - 1),
                timeStampsList.get(0),
                ProxyUtils.Number2Param(Integer.parseInt(step)));
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
                        for (int i = 0; i < 6; i++)
                            diskWrite[i] += disk.get("values").get(i).get(1).asLong();
                    } else {
                        for (int i = 0; i < 6; i++)
                            diskRead[i] += disk.get("values").get(i).get(1).asLong();
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
