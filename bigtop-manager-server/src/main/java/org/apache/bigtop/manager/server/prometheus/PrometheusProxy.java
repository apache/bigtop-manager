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

import org.apache.bigtop.manager.server.model.dto.ServiceChartDTO;
import org.apache.bigtop.manager.server.model.vo.ClusterMetricsVO;
import org.apache.bigtop.manager.server.model.vo.HostMetricsVO;
import org.apache.bigtop.manager.server.model.vo.ServiceMetricsChartVO;
import org.apache.bigtop.manager.server.model.vo.ServiceMetricsSeriesVO;
import org.apache.bigtop.manager.server.model.vo.ServiceMetricsVO;
import org.apache.bigtop.manager.server.utils.StackUtils;

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
import java.util.Collections;
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

    private static final ThreadLocal<List<String>> timestampCache = ThreadLocal.withInitial(ArrayList::new);

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

    public PrometheusResponse queryRange(String query, String start, String end, String step) {
        return webClient
                .post()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/query_range").build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("query", query)
                        .with("timeout", "10")
                        .with("start", start)
                        .with("end", end)
                        .with("step", step))
                .retrieve()
                .bodyToMono(PrometheusResponse.class)
                .block();
    }

    public HostMetricsVO queryHostMetrics(String agentIpv4, String interval) {
        timestampCache.set(getTimestampsList(processInternal(interval)));

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
            Map<String, List<BigDecimal>> agentCpuInterval = retrieveAgentCpu(agentIpv4, interval);
            Map<String, List<BigDecimal>> agentMemInterval = retrieveAgentMemory(agentIpv4, interval);
            Map<String, List<BigDecimal>> agentDiskIOInterval = retrieveAgentDiskIO(agentIpv4, interval);

            res.setCpuUsage(convertList(agentCpuInterval.get(CPU_USAGE), 100));
            res.setSystemLoad1(convertList(agentCpuInterval.get(CPU_LOAD_AVG_MIN_1)));
            res.setSystemLoad5(convertList(agentCpuInterval.get(CPU_LOAD_AVG_MIN_5)));
            res.setSystemLoad15(convertList(agentCpuInterval.get(CPU_LOAD_AVG_MIN_15)));
            res.setMemoryUsage(convertList(agentMemInterval.get("memUsage"), 100));
            res.setDiskRead(convertList(agentDiskIOInterval.get(DISK_READ)));
            res.setDiskWrite(convertList(agentDiskIOInterval.get(DISK_WRITE)));
        }

        res.setTimestamps(timestampCache.get());
        timestampCache.remove();
        return res;
    }

    public ClusterMetricsVO queryClusterMetrics(List<String> agentIpv4s, String interval) {
        timestampCache.set(getTimestampsList(processInternal(interval)));

        ClusterMetricsVO res = new ClusterMetricsVO();
        if (!agentIpv4s.isEmpty()) {
            BigDecimal totalPhysicalCores = new BigDecimal("0.0");
            BigDecimal totalMemSpace = new BigDecimal("0.0");

            BigDecimal usedPhysicalCores = new BigDecimal("0.0");
            BigDecimal totalMemIdle = new BigDecimal("0.0");

            List<BigDecimal> timeUsedCores = getEmptyList();
            List<BigDecimal> timeMemIdle = getEmptyList();

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
                List<BigDecimal> cpuUsageInterval =
                        retrieveAgentCpu(agentIpv4, interval).get(CPU_USAGE);
                for (int i = 0; i < cpuUsageInterval.size(); i++) {
                    BigDecimal c = cpuUsageInterval.get(i);
                    if (c != null) {
                        c = c.multiply(physicalCores);
                        BigDecimal b = timeUsedCores.get(i);
                        if (b == null) {
                            b = new BigDecimal("0.0");
                        }

                        timeUsedCores.set(i, c.add(b));
                    }
                }

                List<BigDecimal> memIdleInterval =
                        retrieveAgentMemory(agentIpv4, interval).get(MEM_IDLE);
                for (int i = 0; i < memIdleInterval.size(); i++) {
                    BigDecimal m = memIdleInterval.get(i);
                    if (m != null) {
                        BigDecimal b = timeMemIdle.get(i);
                        if (b == null) {
                            b = new BigDecimal("0.0");
                        }

                        timeMemIdle.set(i, m.add(b));
                    }
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
            List<BigDecimal> cpuUsageList = getEmptyList();
            List<BigDecimal> memUsageList = getEmptyList();

            for (int i = 0; i < timeUsedCores.size(); i++) {
                BigDecimal usedCores = timeUsedCores.get(i);
                if (usedCores != null) {
                    usedCores = usedCores.divide(totalPhysicalCores, 4, RoundingMode.HALF_UP);
                    cpuUsageList.set(i, usedCores);
                }
            }

            for (int i = 0; i < timeMemIdle.size(); i++) {
                BigDecimal memIdle = timeMemIdle.get(i);
                if (memIdle != null) {
                    memIdle = totalMemSpace.subtract(memIdle).divide(totalMemSpace, 4, RoundingMode.HALF_UP);
                    memUsageList.set(i, memIdle);
                }
            }

            res.setCpuUsage(convertList(cpuUsageList, 100));
            res.setMemoryUsage(convertList(memUsageList, 100));
        }

        res.setTimestamps(timestampCache.get());
        timestampCache.remove();
        return res;
    }

    public ServiceMetricsVO queryServiceMetrics(String clusterName, String serviceName, String interval) {
        List<ServiceChartDTO> charts = StackUtils.SERVICE_CHARTS_MAP.get(serviceName);
        if (CollectionUtils.isEmpty(charts)) {
            return new ServiceMetricsVO();
        }

        List<String> timestamps = getTimestampsList(processInternal(interval));
        ServiceMetricsVO res = new ServiceMetricsVO();
        List<ServiceMetricsChartVO> resultCharts = new ArrayList<>();
        for (ServiceChartDTO chart : charts) {
            String params = chart.getDataExpression().replace("$cluster", clusterName);
            PrometheusResponse response = queryRange(
                    params,
                    timestamps.get(0),
                    timestamps.get(timestamps.size() - 1),
                    number2Param(processInternal(interval)));

            ServiceMetricsChartVO metrics = new ServiceMetricsChartVO();
            List<ServiceMetricsSeriesVO> series = new ArrayList<>();
            for (PrometheusResult result : response.getData().getResult()) {
                List<String> emptyList = new ArrayList<>(Collections.nCopies(timestamps.size(), null));
                String key = result.getMetric().get("instance");
                for (List<String> value : result.getValues()) {
                    String timestamp = value.get(0);
                    int index = timestamps.indexOf(timestamp);
                    String roundValue = new BigDecimal(value.get(1))
                            .setScale(chart.getDataScale(), RoundingMode.HALF_UP)
                            .toString();
                    emptyList.set(index, roundValue);
                }

                ServiceMetricsSeriesVO seriesItem = new ServiceMetricsSeriesVO();
                seriesItem.setName(key);
                seriesItem.setData(emptyList);
                seriesItem.setType(chart.getType());
                series.add(seriesItem);
            }

            metrics.setSeries(series);
            metrics.setTitle(chart.getTitle());
            metrics.setValueType(chart.getValueType());
            resultCharts.add(metrics);
        }

        res.setCharts(resultCharts);
        res.setTimestamps(timestamps);
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

    public Map<String, List<BigDecimal>> retrieveAgentCpu(String iPv4addr, String interval) {
        List<String> timestamps = timestampCache.get();
        Map<String, List<BigDecimal>> map = new HashMap<>();
        String params = String.format("agent_host_monitoring_cpu{iPv4addr=\"%s\"}", iPv4addr);
        PrometheusResponse response = queryRange(
                params,
                timestamps.get(0),
                timestamps.get(timestamps.size() - 1),
                number2Param(processInternal(interval)));

        for (PrometheusResult result : response.getData().getResult()) {
            String key = result.getMetric().get("cpuUsage");
            List<BigDecimal> list = map.computeIfAbsent(key, k -> getEmptyList());
            for (List<String> value : result.getValues()) {
                String timestamp = value.get(0);
                int index = timestamps.indexOf(timestamp);
                list.set(index, new BigDecimal(value.get(1)));
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

    public Map<String, List<BigDecimal>> retrieveAgentMemory(String iPv4addr, String interval) {
        List<String> timestamps = timestampCache.get();
        Map<String, List<BigDecimal>> map = new HashMap<>();
        String params = String.format("agent_host_monitoring_mem{iPv4addr=\"%s\"}", iPv4addr);
        PrometheusResponse response = queryRange(
                params,
                timestamps.get(0),
                timestamps.get(timestamps.size() - 1),
                number2Param(processInternal(interval)));

        for (PrometheusResult result : response.getData().getResult()) {
            String key = result.getMetric().get("memUsage");
            List<BigDecimal> list = map.computeIfAbsent(key, k -> getEmptyList());
            for (List<String> value : result.getValues()) {
                String timestamp = value.get(0);
                int index = timestamps.indexOf(timestamp);
                list.set(index, new BigDecimal(value.get(1)));
            }
        }

        List<BigDecimal> memTotalList = map.get(MEM_TOTAL) == null ? getEmptyList() : map.get(MEM_TOTAL);
        List<BigDecimal> memIdleList = map.get(MEM_IDLE) == null ? getEmptyList() : map.get(MEM_IDLE);
        List<BigDecimal> memUsageList = getEmptyList();

        for (int i = 0; i < memTotalList.size(); i++) {
            BigDecimal memTotal = memTotalList.get(i);
            BigDecimal memIdle = memIdleList.get(i);
            if (memTotal != null && memIdle != null) {
                BigDecimal memUsage = memTotal.subtract(memIdle).divide(memTotal, 4, RoundingMode.HALF_UP);
                memUsageList.set(i, memUsage);
            }
        }

        map.put("memUsage", memUsageList);
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

    public Map<String, List<BigDecimal>> retrieveAgentDiskIO(String iPv4addr, String interval) {
        List<String> timestamps = timestampCache.get();
        Map<String, List<BigDecimal>> map = new HashMap<>();
        String params = String.format("agent_host_monitoring_diskIO{iPv4addr=\"%s\"}", iPv4addr);
        PrometheusResponse response = queryRange(
                params,
                timestamps.get(0),
                timestamps.get(timestamps.size() - 1),
                number2Param(processInternal(interval)));

        List<BigDecimal> diskWriteList = getEmptyList();
        List<BigDecimal> diskReadList = getEmptyList();

        for (PrometheusResult result : response.getData().getResult()) {
            String key = result.getMetric().get("diskIO");
            for (List<String> value : result.getValues()) {
                String timestamp = value.get(0);
                int index = timestamps.indexOf(timestamp);
                if (Objects.equals(key, DISK_WRITE)) {
                    BigDecimal w = diskWriteList.get(index);
                    if (w == null) {
                        w = new BigDecimal("0.0");
                    }

                    w = w.add(new BigDecimal(value.get(1)));
                    diskWriteList.set(index, w);
                } else {
                    BigDecimal r = diskReadList.get(index);
                    if (r == null) {
                        r = new BigDecimal("0.0");
                    }

                    r = r.add(new BigDecimal(value.get(1)));
                    diskReadList.set(index, r);
                }
            }
        }

        map.put(DISK_WRITE, diskWriteList);
        map.put(DISK_READ, diskReadList);
        return map;
    }

    private List<String> convertList(List<BigDecimal> list) {
        return convertList(list, 1);
    }

    private List<String> convertList(List<BigDecimal> list, Integer multiply) {
        List<String> resultList = getEmptyList();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                BigDecimal value = list.get(i);
                if (value != null) {
                    resultList.set(i, value.multiply(new BigDecimal(multiply)).toString());
                }
            }
        }

        return resultList;
    }

    private static List<String> getTimestampsList(int step) {
        // format
        String currentTimeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String currentDateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime currentDateTime = LocalDateTime.parse(currentDateStr + " " + currentTimeStr, formatter);
        // get 8 point
        List<String> timestamps = new ArrayList<>();
        ZoneId zid = ZoneId.systemDefault();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime pastTime = currentDateTime.minus(Duration.ofSeconds((long) step * i));
            long timestamp = pastTime.atZone(zid).toInstant().toEpochMilli() / 1000L;
            timestamps.add(String.valueOf(timestamp));
        }

        return timestamps;
    }

    private String number2Param(int step) {
        return String.format("%ss", step);
    }

    private static int processInternal(String internal) {
        int inter = Integer.parseInt(internal.substring(0, internal.length() - 1));
        if (internal.endsWith("m")) return inter * 60;
        else if (internal.endsWith("h")) {
            return inter * 60 * 60;
        }
        return inter;
    }

    private <T> List<T> getEmptyList() {
        int size = timestampCache.get().size();
        return new ArrayList<>(Collections.nCopies(size, null));
    }
}
