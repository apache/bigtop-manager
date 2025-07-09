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
package org.apache.bigtop.manager.server.prometheus;

import org.apache.bigtop.manager.server.model.vo.ClusterMetricsVO;
import org.apache.bigtop.manager.server.model.vo.HostMetricsVO;

import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public PrometheusResponse query(String params) {
        return webClient
                .post()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/query").build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("query", params).with("timeout", "10"))
                .retrieve()
                .bodyToMono(PrometheusResponse.class)
                .block();
    }

    public PrometheusResponse queryRange(String query, long start, long end, String step) {
        return webClient
                .post()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/query_range").build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("query", query)
                        .with("timeout", "10")
                        .with("start", String.valueOf(start))
                        .with("end", String.valueOf(end))
                        .with("step", step))
                .retrieve()
                .bodyToMono(PrometheusResponse.class)
                .block();
    }

    public HostMetricsVO queryAgentsInfo(String agentIpv4, String interval) {
        List<Long> timestamps = getTimeStampsList(processInternal(interval));

        HostMetricsVO res = new HostMetricsVO();
        if (!agentIpv4.isBlank()) {
            // Instant metrics
            Map<String, BigDecimal> agentCpu = retrieveAgentCpu(agentIpv4);
            Map<String, BigDecimal> agentMem = retrieveAgentMemory(agentIpv4);
            Map<String, BigDecimal> agentDisk = retrieveAgentDisk(agentIpv4);
            Map<String, BigDecimal> agentDiskIO = retrieveAgentDiskIO(agentIpv4);

            // Use physical cores to check if the metrics is starting collect
            if (!agentCpu.containsKey(PHYSICAL_CORES)) {
                return res;
            }

            res.setCpuUsageCur(
                    agentCpu.get(CPU_USAGE).multiply(new BigDecimal("100")).toString());
            res.setMemoryUsageCur((agentMem.get(MEM_TOTAL).subtract(agentMem.get(MEM_IDLE)))
                    .divide(agentMem.get(MEM_TOTAL), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .toString());
            res.setDiskUsageCur(agentDisk
                    .get(DISK_TOTAL)
                    .subtract(agentDisk.get(DISK_IDLE))
                    .divide(agentDisk.get(DISK_TOTAL), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .toString());
            res.setFileDescriptorUsage(agentCpu.get(FILE_OPEN_DESCRIPTOR)
                    .divide(agentCpu.get(FILE_TOTAL_DESCRIPTOR), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .toString());
            res.setDiskReadCur(agentDiskIO.get(DISK_READ).toString());
            res.setDiskWriteCur(agentDiskIO.get(DISK_WRITE).toString());

            // Range metrics
            Map<String, Map<String, BigDecimal>> agentCpuInterval = retrieveAgentCpu(agentIpv4, interval, timestamps);
            Map<String, Map<String, BigDecimal>> agentMemInterval =
                    retrieveAgentMemory(agentIpv4, interval, timestamps);
            Map<String, Map<String, BigDecimal>> agentDiskIOInterval =
                    retrieveAgentDiskIO(agentIpv4, interval, timestamps);

            res.setCpuUsage(extractMap(agentCpuInterval.get(CPU_USAGE), 100));
            res.setSystemLoad1(extractMap(agentCpuInterval.get(CPU_LOAD_AVG_MIN_1)));
            res.setSystemLoad5(extractMap(agentCpuInterval.get(CPU_LOAD_AVG_MIN_5)));
            res.setSystemLoad15(extractMap(agentCpuInterval.get(CPU_LOAD_AVG_MIN_15)));
            res.setMemoryUsage(extractMap(agentMemInterval.get("memUsage"), 100));
            res.setDiskRead(extractMap(agentDiskIOInterval.get(DISK_READ)));
            res.setDiskWrite(extractMap(agentDiskIOInterval.get(DISK_WRITE)));
        }

        return res;
    }

    public ClusterMetricsVO queryClustersInfo(List<String> agentIpv4s, String interval) {
        List<Long> timestamps = getTimeStampsList(processInternal(interval));

        ClusterMetricsVO res = new ClusterMetricsVO();
        if (!agentIpv4s.isEmpty()) {
            BigDecimal totalPhysicalCores = new BigDecimal("0.0");
            BigDecimal totalMemSpace = new BigDecimal("0.0");

            BigDecimal usedPhysicalCores = new BigDecimal("0.0");
            BigDecimal totalMemIdle = new BigDecimal("0.0");

            Map<String, BigDecimal> timeUsedCores = new HashMap<>();
            Map<String, BigDecimal> timeMemIdle = new HashMap<>();

            for (String agentIpv4 : agentIpv4s) {
                // Instant Metrics
                Map<String, BigDecimal> agentCpu = retrieveAgentCpu(agentIpv4);
                Map<String, BigDecimal> agentMem = retrieveAgentMemory(agentIpv4);

                // Use physical cores to check if the metrics is starting collect
                if (!agentCpu.containsKey(PHYSICAL_CORES)) {
                    return res;
                }

                BigDecimal cpuUsage = agentCpu.get(CPU_USAGE);
                BigDecimal physicalCores = agentCpu.get(PHYSICAL_CORES);
                BigDecimal usedCores = cpuUsage.multiply(physicalCores);
                BigDecimal memIdle = agentMem.get(MEM_IDLE);
                BigDecimal memTotal = agentMem.get(MEM_TOTAL);

                totalPhysicalCores = totalPhysicalCores.add(physicalCores);
                usedPhysicalCores = usedPhysicalCores.add(usedCores);
                totalMemIdle = totalMemIdle.add(memIdle);
                totalMemSpace = totalMemSpace.add(memTotal);

                // Range Metrics
                Map<String, BigDecimal> cpuUsageInterval =
                        retrieveAgentCpu(agentIpv4, interval, timestamps).get(CPU_USAGE);
                for (Map.Entry<String, BigDecimal> entry : cpuUsageInterval.entrySet()) {
                    String timestamp = entry.getKey();
                    BigDecimal usedCoresAtTime = entry.getValue().multiply(physicalCores);
                    timeUsedCores.merge(timestamp, usedCoresAtTime, BigDecimal::add);
                }

                Map<String, BigDecimal> memIdleInterval =
                        retrieveAgentMemory(agentIpv4, interval, timestamps).get(MEM_IDLE);
                for (Map.Entry<String, BigDecimal> entry : memIdleInterval.entrySet()) {
                    String timestamp = entry.getKey();
                    BigDecimal memIdleAtTime = entry.getValue();
                    timeMemIdle.merge(timestamp, memIdleAtTime, BigDecimal::add);
                }
            }

            // Instant Metrics
            res.setCpuUsageCur(usedPhysicalCores
                    .divide(totalPhysicalCores, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .toString());
            res.setMemoryUsageCur(totalMemSpace
                    .subtract(totalMemIdle)
                    .divide(totalMemSpace, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .toString());

            // Range Metrics
            Map<String, String> cpuUsageMap = new HashMap<>();
            Map<String, String> memUsageMap = new HashMap<>();

            for (Map.Entry<String, BigDecimal> entry : timeUsedCores.entrySet()) {
                String timestamp = entry.getKey();
                BigDecimal usedCores = entry.getValue();
                BigDecimal cpuUsage = usedCores.divide(totalPhysicalCores, 4, RoundingMode.HALF_UP);
                cpuUsageMap.put(
                        timestamp, cpuUsage.multiply(new BigDecimal("100")).toString());
            }

            for (Map.Entry<String, BigDecimal> entry : timeMemIdle.entrySet()) {
                String timestamp = entry.getKey();
                BigDecimal memIdle = entry.getValue();
                BigDecimal memUsage = totalMemSpace.subtract(memIdle).divide(totalMemSpace, 4, RoundingMode.HALF_UP);
                memUsageMap.put(
                        timestamp, memUsage.multiply(new BigDecimal("100")).toString());
            }

            res.setCpuUsage(cpuUsageMap);
            res.setMemoryUsage(memUsageMap);
        }

        return res;
    }

    public Map<String, BigDecimal> retrieveAgentCpu(String iPv4addr) {
        Map<String, BigDecimal> map = new HashMap<>();
        String params = String.format("agent_host_monitoring_cpu{iPv4addr=\"%s\"}", iPv4addr);
        PrometheusResponse response = query(params);
        for (PrometheusResult result : response.getData().getResult()) {
            String key = result.getMetric().get("cpuUsage");
            map.put(key, new BigDecimal(result.getValue().get(1)));
        }

        // Get common metrics
        if (!CollectionUtils.isEmpty(response.getData().getResult())) {
            Map<String, String> metric = response.getData().getResult().get(0).getMetric();
            map.put(PHYSICAL_CORES, new BigDecimal(metric.get(PHYSICAL_CORES)));
            map.put(FILE_OPEN_DESCRIPTOR, new BigDecimal(metric.get(FILE_OPEN_DESCRIPTOR)));
            map.put(FILE_TOTAL_DESCRIPTOR, new BigDecimal(metric.get(FILE_TOTAL_DESCRIPTOR)));
        }

        return map;
    }

    public Map<String, Map<String, BigDecimal>> retrieveAgentCpu(
            String iPv4addr, String interval, List<Long> timestamps) {
        Map<String, Map<String, BigDecimal>> map = new HashMap<>();
        String params = String.format("agent_host_monitoring_cpu{iPv4addr=\"%s\"}", iPv4addr);
        PrometheusResponse response = queryRange(
                params,
                timestamps.get(timestamps.size() - 1),
                timestamps.get(0),
                number2Param(processInternal(interval)));

        for (PrometheusResult result : response.getData().getResult()) {
            String key = result.getMetric().get("cpuUsage");
            Map<String, BigDecimal> innerMap = map.computeIfAbsent(key, k -> new HashMap<>());
            for (List<String> value : result.getValues()) {
                innerMap.put(value.get(0), new BigDecimal(value.get(1)));
            }
        }

        return map;
    }

    public Map<String, BigDecimal> retrieveAgentMemory(String iPv4addr) {
        Map<String, BigDecimal> map = new HashMap<>();
        String params = String.format("agent_host_monitoring_mem{iPv4addr=\"%s\"}", iPv4addr);
        PrometheusResponse response = query(params);
        for (PrometheusResult result : response.getData().getResult()) {
            String key = result.getMetric().get("memUsage");
            map.put(key, new BigDecimal(result.getValue().get(1)));
        }

        return map;
    }

    public Map<String, Map<String, BigDecimal>> retrieveAgentMemory(
            String iPv4addr, String interval, List<Long> timestamps) {
        Map<String, Map<String, BigDecimal>> map = new HashMap<>();
        String params = String.format("agent_host_monitoring_mem{iPv4addr=\"%s\"}", iPv4addr);
        PrometheusResponse response = queryRange(
                params,
                timestamps.get(timestamps.size() - 1),
                timestamps.get(0),
                number2Param(processInternal(interval)));

        for (PrometheusResult result : response.getData().getResult()) {
            String key = result.getMetric().get("memUsage");
            Map<String, BigDecimal> innerMap = map.computeIfAbsent(key, k -> new HashMap<>());
            for (List<String> value : result.getValues()) {
                innerMap.put(value.get(0), new BigDecimal(value.get(1)));
            }
        }

        Map<String, BigDecimal> memTotalMap = map.get(MEM_TOTAL);
        Map<String, BigDecimal> memIdleMap = map.get(MEM_IDLE);
        Map<String, BigDecimal> memUsageMap = new HashMap<>();

        for (Map.Entry<String, BigDecimal> entry : memTotalMap.entrySet()) {
            String timestamp = entry.getKey();
            BigDecimal memTotal = entry.getValue();
            BigDecimal memIdle = memIdleMap.get(timestamp);
            if (memIdle != null) {
                BigDecimal memUsage = memTotal.subtract(memIdle).divide(memTotal, 4, RoundingMode.HALF_UP);
                memUsageMap.put(timestamp, memUsage);
            }
        }

        map.put("memUsage", memUsageMap);

        return map;
    }

    public Map<String, BigDecimal> retrieveAgentDisk(String iPv4addr) {
        Map<String, BigDecimal> map = new HashMap<>();
        String params = String.format("agent_host_monitoring_disk{iPv4addr=\"%s\"}", iPv4addr);
        PrometheusResponse response = query(params);
        BigDecimal diskTotalSpace = new BigDecimal("0.0");
        BigDecimal diskFreeSpace = new BigDecimal("0.0");
        for (PrometheusResult result : response.getData().getResult()) {
            String key = result.getMetric().get("diskUsage");
            if (Objects.equals(key, DISK_IDLE)) {
                diskFreeSpace =
                        diskFreeSpace.add(new BigDecimal(result.getValue().get(1)));
            } else {
                diskTotalSpace =
                        diskTotalSpace.add(new BigDecimal(result.getValue().get(1)));
            }
        }

        map.put(DISK_IDLE, diskFreeSpace);
        map.put(DISK_TOTAL, diskTotalSpace);
        return map;
    }

    public Map<String, BigDecimal> retrieveAgentDiskIO(String iPv4addr) {
        Map<String, BigDecimal> map = new HashMap<>();
        String params = String.format("agent_host_monitoring_diskIO{iPv4addr=\"%s\"}", iPv4addr);
        PrometheusResponse response = query(params);
        BigDecimal diskWrite = new BigDecimal("0.0");
        BigDecimal diskRead = new BigDecimal("0.0");
        for (PrometheusResult result : response.getData().getResult()) {
            String key = result.getMetric().get("diskIO");
            if (Objects.equals(key, DISK_WRITE)) {
                diskWrite = diskWrite.add(new BigDecimal(result.getValue().get(1)));
            } else {
                diskRead = diskRead.add(new BigDecimal(result.getValue().get(1)));
            }
        }

        map.put(DISK_WRITE, diskWrite);
        map.put(DISK_READ, diskRead);
        return map;
    }

    public Map<String, Map<String, BigDecimal>> retrieveAgentDiskIO(
            String iPv4addr, String interval, List<Long> timestamps) {
        Map<String, Map<String, BigDecimal>> map = new HashMap<>();
        String params = String.format("agent_host_monitoring_diskIO{iPv4addr=\"%s\"}", iPv4addr);
        PrometheusResponse response = queryRange(
                params,
                timestamps.get(timestamps.size() - 1),
                timestamps.get(0),
                number2Param(processInternal(interval)));

        Map<String, BigDecimal> diskWriteMap = new HashMap<>();
        Map<String, BigDecimal> diskReadMap = new HashMap<>();

        for (PrometheusResult result : response.getData().getResult()) {
            String key = result.getMetric().get("diskIO");
            for (List<String> value : result.getValues()) {
                if (Objects.equals(key, DISK_WRITE)) {
                    BigDecimal w = diskWriteMap.computeIfAbsent(value.get(0), k -> new BigDecimal("0.0"));
                    w = w.add(new BigDecimal(value.get(1)));
                    diskWriteMap.put(value.get(0), w);
                } else {
                    BigDecimal r = diskReadMap.computeIfAbsent(value.get(0), k -> new BigDecimal("0.0"));
                    r = r.add(new BigDecimal(value.get(1)));
                    diskReadMap.put(value.get(0), r);
                }
            }
        }

        map.put(DISK_WRITE, diskWriteMap);
        map.put(DISK_READ, diskReadMap);
        return map;
    }

    private Map<String, String> extractMap(Map<String, BigDecimal> map) {
        return extractMap(map, 1);
    }

    private Map<String, String> extractMap(Map<String, BigDecimal> map, Integer multiply) {
        Map<String, String> resultMap = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : map.entrySet()) {
            resultMap.put(
                    entry.getKey(),
                    entry.getValue().multiply(new BigDecimal(multiply)).toString());
        }
        return resultMap;
    }

    private static List<Long> getTimeStampsList(int step) {
        // format
        String currentTimeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String currentDateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime currentDateTime = LocalDateTime.parse(currentDateStr + " " + currentTimeStr, formatter);
        // get 8 point
        List<Long> timestamps = new ArrayList<>();
        ZoneId zid = ZoneId.systemDefault();
        for (int i = 0; i < 7; i++) {
            LocalDateTime pastTime = currentDateTime.minus(Duration.ofSeconds((long) step * i));
            long timestamp = pastTime.atZone(zid).toInstant().toEpochMilli() / 1000L;
            timestamps.add(timestamp);
        }
        return timestamps;
    }

    private String number2Param(int step) {
        return String.format("%ss", step);
    }

    private int processInternal(String internal) {
        int inter = Integer.parseInt(internal.substring(0, internal.length() - 1));
        if (internal.endsWith("m")) return inter * 60;
        else if (internal.endsWith("h")) {
            return inter * 60 * 60;
        }
        return inter;
    }

    public static void main(String[] args) {
        String ipv4addr = "172.18.0.4";
        String interval = "1m";
        PrometheusProxy proxy = new PrometheusProxy("localhost", 19090);

        //        proxy.retrieveAgentCpu(ipv4addr);
        Object o = proxy.queryAgentsInfo(ipv4addr, interval);
        //        Map<String, Object> res = proxy.queryAgentsInfo("172.18.0.3", "1m");
        System.out.println();
    }
}
