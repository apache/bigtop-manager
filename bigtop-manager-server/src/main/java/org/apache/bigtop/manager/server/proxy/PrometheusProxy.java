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
import java.util.Objects;

@Component
public class PrometheusProxy { // 该类就是为了和普罗米修斯进行对接的

    private final WebClient webClient;

    @Value("${monitoring.agent-host-job-name}")
    private String agentHostJobName;

    public PrometheusProxy(
            WebClient.Builder webClientBuilder, @Value("${monitoring.prometheus-host}") String prometheusHost) {
        this.webClient = webClientBuilder.baseUrl(prometheusHost).build();
    }

    // 状态(healthy已给出)
    public JsonNode queryAgentsHealthyStatus() {
        Mono<JsonNode> body = webClient
                .post()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/query").build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                //这个只是用来查询节点状态的，与正式的数据无关
                // 句子的意思是，查询指定job的指定节点的up状态与否
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
    // 集群名称(应该是系统赋予) 组件个数(?)
    public JsonNode queryAgentsInfo() {
        // host 对应 // 节点名称 操作系统 IPV4 IPV6 磁盘总量 内存总量 核心 架构
        // 最后返回一个[host{{1}{2}}]的json数据
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode agentsInfo = objectMapper.createArrayNode();
        JsonNode agents = queryAgentsHealthyStatus(); // 获取全主机

        for(JsonNode agent:agents){
            String ipv4 = agent.get("agentInfo").asText().split(":")[0];
            ObjectNode temp = objectMapper.createObjectNode();
            JsonNode cpuResult = queryAgentCpu(ipv4);
            JsonNode memResult = queryAgentMemory(ipv4);
            JsonNode diskResult = queryAgentDisk(ipv4);
            // {hostname,iPv4addr,os,architecture,physical_cores,type,time,cpuLoad}
            temp.put("hostname",cpuResult.get(0).get("hostname").asText());
            temp.put("iPv4addr", cpuResult.get("iPv4addr").asText());
            //temp.put("iPv6addr", cpuResult.get("iPv6addr").asText()); // todo IPV6地址( 待补充)
            temp.put("os", cpuResult.get("os").asText());
            temp.put("architecture", cpuResult.get("architecture").asText());
            temp.put("physical_cores", cpuResult.get("physical_cores").asText());
            temp.put("time", cpuResult.get("time").asText());
            temp.put("cpuLoadAvgMin_5", cpuResult.get("cpuLoadAvgMin_5").asDouble());
            temp.put("cpuLoadAvgMin_10", cpuResult.get("cpuLoadAvgMin_10").asDouble());
            temp.put("cpuLoadAvgMin_15", cpuResult.get("cpuLoadAvgMin_15").asDouble());
            temp.put("cpuUsage", cpuResult.get("cpuUsage").asDouble());
            // {hostname,ipv4addr,time,memIdle,memTotal}
            temp.put("memIdle",memResult.get("memIdle").asInt());
            temp.put("memTotal",memResult.get("memTotal").asInt());
            // hostname.iPv4addr,time,diskInfo:[{diskName,diskUsage,diskValue}]
            int totalSpace = 0;
            int freeSpace = 0;
            for(JsonNode disk:diskResult.get("diskInfo")){
                if(Objects.equals(disk.get("diskUsage").asText(), "diskTotalSpace")){
                    totalSpace += disk.get("diskValue").asInt();
                }else if(Objects.equals(disk.get("diskUsage").asText(), "diskFreeSpace")){
                    freeSpace += disk.get("diskValue").asInt();
                }
            }
            temp.put("diskFreeSpace",freeSpace);
            temp.put("diskTotalSpace",totalSpace);
            agentsInfo.add(temp);
        }

        return agentsInfo;
    }

    public JsonNode queryAgentsInstStatus() {
        //todo 文件句柄使用率? cpu使用率 内存使用率 系统负载 磁盘IO 用一个方法进行返回 待处理清洗数据

        // 最后返回一个[host{{1}{2}}]的json数据
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode agentsInfo = objectMapper.createArrayNode();
        JsonNode agents = queryAgentsHealthyStatus(); // 获取全主机

        for(JsonNode agent:agents){
            String ipv4 = agent.get("agentInfo").asText().split(":")[0];
            ObjectNode temp = objectMapper.createObjectNode();
            JsonNode cpuResult = queryAgentCpu(ipv4);
            JsonNode memResult = queryAgentMemory(ipv4);
            JsonNode diskResult = queryAgentDisk(ipv4);
            // {hostname,ipv4addr,os,architecture,physical_cores,type,time,cpuLoad}
            temp.put("hostname",cpuResult.get(0).get("hostname").asText());
            temp.put("iPv4addr", cpuResult.get("iPv4addr").asText());
            temp.put("time", cpuResult.get("time").asText());
            temp.put("cpuLoadAvgMin_5", cpuResult.get("cpuLoadAvgMin_5").asDouble());
            temp.put("cpuLoadAvgMin_10", cpuResult.get("cpuLoadAvgMin_10").asDouble());
            temp.put("cpuLoadAvgMin_15", cpuResult.get("cpuLoadAvgMin_15").asDouble());
            temp.put("cpuUsage", cpuResult.get("cpuUsage").asDouble());
            // {hostname,ipv4addr,time,memIdle,memTotal}
            temp.put("memUsage",(double) (memResult.get("memTotal").asInt() - memResult.get("memIdle").asInt() / memResult.get("memTotal").asInt()));
            // hostname.iPv4addr,time,diskInfo:[{diskName,diskUsage,diskValue}]
            int totalSpace = 0;
            int freeSpace = 0;
            for(JsonNode disk:diskResult.get("diskInfo")){
                if(Objects.equals(disk.get("diskUsage").asText(), "diskTotalSpace")){
                    totalSpace += disk.get("diskValue").asInt();
                }else if(Objects.equals(disk.get("diskUsage").asText(), "diskFreeSpace")){
                    freeSpace += disk.get("diskValue").asInt();
                }
            }
            temp.put("diskFreeSpace",freeSpace);
            temp.put("diskTotalSpace",totalSpace);
            agentsInfo.add(temp);
        }

        return agentsInfo;
    }

    // query方法
    private JsonNode query(String params){
        Mono<JsonNode> body = webClient
                .post()
                .uri("/api/v1/query")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("query", params)
                        .with("timeout", "10"))
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

    // 查询单主机CPU指标
    private JsonNode queryAgentCpu(String iPv4addr){
        /*
         *     # HELP agent_host_monitoring_cpu BigTop Manager Agent Host Monitoring, CPU Monitoring
         *     # TYPE agent_host_monitoring_cpu gauge
         *     agent_host_monitoring_cpu{cpuUsage="cpuLoadAvgMin_5",cpu_info="unknown",hostname="env",iPv4addr="192.168.139.100",ipv4Gateway="192.168.139.2",logical_cores="8",os="GNU/Linux openEuler 21.09 (unknown) build 5.10.0-5.10.1.25.oe1.x86_64",physical_cores="8",} 1.61
         *     agent_host_monitoring_cpu{cpuUsage="cpuUsage",cpu_info="unknown",hostname="env",iPv4addr="192.168.139.100",ipv4Gateway="192.168.139.2",logical_cores="8",os="GNU/Linux openEuler 21.09 (unknown) build 5.10.0-5.10.1.25.oe1.x86_64",physical_cores="8",} 0.08550662676357418
         *     agent_host_monitoring_cpu{cpuUsage="cpuLoadAvgMin_1",cpu_info="unknown",hostname="env",iPv4addr="192.168.139.100",ipv4Gateway="192.168.139.2",logical_cores="8",os="GNU/Linux openEuler 21.09 (unknown) build 5.10.0-5.10.1.25.oe1.x86_64",physical_cores="8",} 0.94
         *     agent_host_monitoring_cpu{cpuUsage="cpuLoadAvgMin_15",cpu_info="unknown",hostname="env",iPv4addr="192.168.139.100",ipv4Gateway="192.168.139.2",logical_cores="8",os="GNU/Linux openEuler 21.09 (unknown) build 5.10.0-5.10.1.25.oe1.x86_64",physical_cores="8",} 1.85
         */
        String params = String.format("agent_host_monitoring_cpu{iPv4addr=\"%s\"", iPv4addr);
        JsonNode result = query(params);
        ObjectMapper objectMapper = new ObjectMapper();
        if (result != null) {
            JsonNode agentCpus = result.get("data").get("result");
            JsonNode agentCpuMetric = agentCpus.get(0).get("metric");
            JsonNode agentCpuValue = agentCpus.get(0).get("value");
            ObjectNode agentInfo = objectMapper.createObjectNode();
            agentInfo.put("hostname", agentCpuMetric.get("hostname").asText());// 节点名称
            agentInfo.put("iPv4addr", agentCpuMetric.get("iPv4addr").asText());// IPV4地址
            //temp.put("iPv6addr", agentCpuMetric.get("iPv4addr").asText()); // todo IPV6地址( 待补充)
            int lastIndex = agentCpuMetric.get("os").asText().lastIndexOf('.'); // 获取下标
            String os = agentCpuMetric.get("os").asText().substring(0, lastIndex);
            String arch = agentCpuMetric.get("os").asText().substring(lastIndex + 1);
            agentInfo.put("os", os);// 操作系统
            agentInfo.put("architecture", arch);// 系统架构
            agentInfo.put("physical_cores", agentCpuMetric.get("physical_cores").asText());// 核心数
            LocalDateTime instant = Instant.ofEpochSecond(agentCpuValue.get(0).asLong())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            agentInfo.put("time", instant.toString()); // 抓取刷新时刻
            for (JsonNode agent : agentCpus) {
                agentInfo.put(agent.get("metric").get("cpuUsage").asText(), agent.get("value").get(1).asDouble()); // cpu 指标值
            }
            //{hostname,iPv4addr,os,architecture,physical_cores,time,cpuLoad,cpuLoad,cpuLoad,cpuLoad}
            return agentInfo;
        }
        return objectMapper.createObjectNode();
    }

    // 查询单主机内存指标
    private JsonNode queryAgentMemory(String iPv4addr){
        /*
         * # HELP agent_host_monitoring_mem BigTop Manager Agent Host Monitoring, Memory Monitoring
         * # TYPE agent_host_monitoring_mem gauge
         * agent_host_monitoring_mem{cpu_info="unknown",hostname="env",iPv4addr="192.168.139.100",ipv4Gateway="192.168.139.2",logical_cores="8",memUsage="memIdle",os="GNU/Linux openEuler 21.09 (unknown) build 5.10.0-5.10.1.25.oe1.x86_64",physical_cores="8",} 4.39965696E9
         * agent_host_monitoring_mem{cpu_info="unknown",hostname="env",iPv4addr="192.168.139.100",ipv4Gateway="192.168.139.2",logical_cores="8",memUsage="memTotal",os="GNU/Linux openEuler 21.09 (unknown) build 5.10.0-5.10.1.25.oe1.x86_64",physical_cores="8",} 7.774588928E9
         */
        ObjectMapper objectMapper = new ObjectMapper();
        String query = String.format("agent_host_monitoring_mem{iPv4addr=\"%s\"", iPv4addr);
        JsonNode result = query(query);
        if (result != null){
            JsonNode agentsMem = result.get("data").get("result");
            JsonNode agentMemMetric = agentsMem.get(0).get("metric");
            JsonNode agentMemValue = agentsMem.get(0).get("value");
            ObjectNode agentsInfo = objectMapper.createObjectNode();
            agentsInfo.put("hostname", agentMemMetric.get("hostname").asText());
            agentsInfo.put("iPv4addr", agentMemMetric.get("iPv4addr").asText());
            LocalDateTime instant = Instant.ofEpochSecond(agentMemValue.get(0).asLong())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            agentsInfo.put("time", instant.toString());
            for (JsonNode agent : agentsMem) {
                agentsInfo.put(agent.get("metric").get("memUsage").asText(), agent.get("value").get(1).asInt()); // mem
            }
            // {hostname,iPv4addr,time,memIdle,memTotal}
            return agentsInfo;
        }
        return objectMapper.createObjectNode();
    }

    // 查询单主机硬盘指标
    private JsonNode queryAgentDisk(String iPv4addr){
        /*
         * # HELP agent_host_monitoring_disk BigTop Manager Agent Host Monitoring, Disk Monitoring
         * # TYPE agent_host_monitoring_disk gauge
         * agent_host_monitoring_disk{cpu_info="unknown",diskName="tmpfs",diskUsage="diskTotalSpace",hostname="env",iPv4addr="192.168.139.100",ipv4Gateway="192.168.139.2",logical_cores="8",os="GNU/Linux openEuler 21.09 (unknown) build 5.10.0-5.10.1.25.oe1.x86_64",physical_cores="8",} 3.887296512E9
         * agent_host_monitoring_disk{cpu_info="unknown",diskName="/dev/mapper/openeuler-root",diskUsage="diskFreeSpace",hostname="env",iPv4addr="192.168.139.100",ipv4Gateway="192.168.139.2",logical_cores="8",os="GNU/Linux openEuler 21.09 (unknown) build 5.10.0-5.10.1.25.oe1.x86_64",physical_cores="8",} 2.7496419328E10
         * agent_host_monitoring_disk{cpu_info="unknown",diskName="tmpfs",diskUsage="diskFreeSpace",hostname="env",iPv4addr="192.168.139.100",ipv4Gateway="192.168.139.2",logical_cores="8",os="GNU/Linux openEuler 21.09 (unknown) build 5.10.0-5.10.1.25.oe1.x86_64",physical_cores="8",} 3.886923776E9
         * agent_host_monitoring_disk{cpu_info="unknown",diskName="/dev/mapper/openeuler-root",diskUsage="diskTotalSpace",hostname="env",iPv4addr="192.168.139.100",ipv4Gateway="192.168.139.2",logical_cores="8",os="GNU/Linux openEuler 21.09 (unknown) build 5.10.0-5.10.1.25.oe1.x86_64",physical_cores="8",} 3.6718542848E10
         */
        ObjectMapper objectMapper = new ObjectMapper();
        String params = String.format("agent_host_monitoring_disk{iPv4addr=\"%s\"}",iPv4addr);
        JsonNode result = query(params);
        if (result != null){
            JsonNode agentDisksResult = result.get("data").get("result");
            JsonNode agentDisksMetric = agentDisksResult.get(0).get("metric");
            JsonNode agentDisksValue = agentDisksResult.get(0).get("value");
            ObjectNode agentDiskInfo = objectMapper.createObjectNode();
            agentDiskInfo.put("hostname", agentDisksMetric.get("hostname").asText());
            agentDiskInfo.put("iPv4addr", agentDisksMetric.get("iPv4addr").asText());
            LocalDateTime instant = Instant.ofEpochSecond(agentDisksValue.get(0).asLong())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            agentDiskInfo.put("time", instant.toString()); // 抓取刷新时刻
            // 磁盘总容量 空间之和
            ArrayNode tempDiskInfo = objectMapper.createArrayNode();
             for (JsonNode agent : agentDisksResult) {
                JsonNode agentDisk = agent.get("metric");
                ObjectNode temp = objectMapper.createObjectNode();
                temp.put("diskName",agentDisk.get("diskUsage").asText());
                temp.put("diskUsage", agentDisk.get("diskUsage").asText());
                temp.put("diskValue", agent.get("value").get(1).asInt());
                tempDiskInfo.add(temp);
            }
             agentDiskInfo.set("diskInfo",tempDiskInfo);
            return agentDiskInfo;
        }
        return objectMapper.createObjectNode();
    }

}
