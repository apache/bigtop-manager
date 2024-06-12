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

import org.apache.bigtop.manager.agent.enums.AgentExceptionStatus;
import org.apache.bigtop.manager.agent.exception.AgentException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.NetworkParams;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class AgentHostMonitoring {

    public static final String AGENT_BASE_INFO = "agentBaseInfo";
    public static final String BOOT_TIME = "bootTime";
    public static final String MEM_IDLE = "memIdle";
    public static final String MEM_TOTAL = "memTotal";
    public static final String DISKS_BASE_INFO = "disksBaseInfo";
    public static final String DISK_NAME = "diskName";
    public static final String DISK_IDLE = "diskFreeSpace";
    public static final String DISK_TOTAL = "diskTotalSpace";
    public static final String CPU_LOAD_AVG_MIN_1 = "cpuLoadAvgMin_1";
    public static final String CPU_LOAD_AVG_MIN_5 = "cpuLoadAvgMin_5";
    public static final String CPU_LOAD_AVG_MIN_15 = "cpuLoadAvgMin_15";
    public static final String CPU_USAGE = "cpuUsage";

    private static boolean sameSubnet(String ipAddress, String subnetMask, String gateway) throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getByName(ipAddress);
        InetAddress subnetAddress = InetAddress.getByName(subnetMask);
        InetAddress gatewayAddress = InetAddress.getByName(gateway);
        byte[] ipBytes = inetAddress.getAddress();
        byte[] subnetBytes = subnetAddress.getAddress();
        byte[] gatewayBytes = gatewayAddress.getAddress();
        for (int i = 0; i < ipBytes.length; i++) {
            if ((ipBytes[i] & subnetBytes[i]) != (gatewayBytes[i] & subnetBytes[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * get Host Info
     * hostname, ip,gateway, os info, cpu arch etc.
     *
     * @return JsonNode
     */
    public static JsonNode getHostInfo() throws UnknownHostException {
        ObjectMapper json = new ObjectMapper();
        ObjectNode objectNode = json.createObjectNode();
        SystemInfo si = new SystemInfo();
        OperatingSystem operatingSystem = si.getOperatingSystem();
        HardwareAbstractionLayer hal = si.getHardware();

        NetworkParams networkParams = operatingSystem.getNetworkParams();
        String hostName = networkParams.getHostName();
        String ipv4DefaultGateway = networkParams.getIpv4DefaultGateway();

        // Agent Host Base Info
        ObjectNode hostInfoNode = json.createObjectNode();
        hostInfoNode
                .put("hostname", hostName)
                .put("os", operatingSystem.toString())
                .put("ipv4Gateway", ipv4DefaultGateway)
                .put("cpu_info", hal.getProcessor().getProcessorIdentifier().getMicroarchitecture())
                .put("logical_cores", hal.getProcessor().getLogicalProcessorCount())
                .put("physical_cores", hal.getProcessor().getPhysicalProcessorCount())
                .put("iPv4addr", getAgentHostIPv4addr(hal, ipv4DefaultGateway));
        objectNode.set(AGENT_BASE_INFO, hostInfoNode);

        objectNode.put(BOOT_TIME, operatingSystem.getSystemBootTime());
        // .put("upTimeSeconds", operatingSystem.getSystemUptime())

        // MEM
        GlobalMemory memory = hal.getMemory();
        objectNode.put(MEM_IDLE, memory.getAvailable()).put(MEM_TOTAL, memory.getTotal());
        // DISK
        List<OSFileStore> fileStores = operatingSystem.getFileSystem().getFileStores(true);
        ArrayNode diskArrayNode = json.createArrayNode();
        for (OSFileStore fileStore : fileStores) {
            if (fileStore.getTotalSpace() <= 1024 * 1024 * 1024) {
                continue;
            }
            ObjectNode disk = json.createObjectNode();
            disk.put(DISK_NAME, fileStore.getVolume());
            disk.put(DISK_TOTAL, fileStore.getTotalSpace());
            disk.put(DISK_IDLE, fileStore.getFreeSpace());
            diskArrayNode.add(disk);
        }
        objectNode.set(DISKS_BASE_INFO, diskArrayNode);
        return objectNode;
    }

    private static String getAgentHostIPv4addr(HardwareAbstractionLayer hal, String ipv4DefaultGateway)
            throws UnknownHostException {
        for (NetworkIF networkIF : hal.getNetworkIFs()) {
            String[] iPv4addr = networkIF.getIPv4addr();
            if (null == iPv4addr || iPv4addr.length == 0) {
                continue;
            }
            short subnetMaskLen = networkIF.getSubnetMasks()[0];
            String subnetMask = "255.255.255.0";
            if (subnetMaskLen == 8) {
                subnetMask = "255.0.0.0";
            } else if (subnetMaskLen == 16) {
                subnetMask = "255.255.0.0";
            }
            List<InterfaceAddress> interfaceAddresses =
                    networkIF.queryNetworkInterface().getInterfaceAddresses();
            for (InterfaceAddress ifaddr : interfaceAddresses) {
                if (null != ifaddr.getBroadcast() && sameSubnet(iPv4addr[0], subnetMask, ipv4DefaultGateway)) {
                    return iPv4addr[0];
                }
            }
        }
        return "0.0.0.0";
    }

    @Getter
    private static class BaseAgentGauge {
        private final ArrayList<String> labels;
        private final ArrayList<String> labelsValues;

        public BaseAgentGauge(JsonNode agentMonitoring) {
            JsonNode agentHostInfo = agentMonitoring.get(AgentHostMonitoring.AGENT_BASE_INFO);
            this.labels = new ArrayList<>();
            this.labelsValues = new ArrayList<>();
            Iterator<String> fieldNames = agentHostInfo.fieldNames();
            while (fieldNames.hasNext()) {
                String field = fieldNames.next();
                this.labels.add(field);
                this.labelsValues.add(agentHostInfo.get(field).asText());
            }
        }
    }

    public static Map<ArrayList<String>, Map<ArrayList<String>, Double>> getDiskGauge(JsonNode agentMonitoring) {
        BaseAgentGauge gaugeBaseInfo = new BaseAgentGauge(agentMonitoring);
        ArrayList<String> diskGaugeLabels = gaugeBaseInfo.getLabels();
        ArrayList<String> diskGaugeLabelsValues = gaugeBaseInfo.getLabelsValues();
        diskGaugeLabels.add(AgentHostMonitoring.DISK_NAME);
        diskGaugeLabels.add("diskUsage");

        Map<ArrayList<String>, Double> labelValues = new HashMap<>();
        ArrayNode disksInfo = (ArrayNode) agentMonitoring.get(AgentHostMonitoring.DISKS_BASE_INFO);
        disksInfo.forEach(diskJsonNode -> {
            // Disk Idle
            ArrayList<String> diskIdleLabelValues = new ArrayList<>(diskGaugeLabelsValues);
            diskIdleLabelValues.add(
                    diskJsonNode.get(AgentHostMonitoring.DISK_NAME).asText());
            diskIdleLabelValues.add(AgentHostMonitoring.DISK_IDLE);
            labelValues.put(
                    diskIdleLabelValues,
                    diskJsonNode.get(AgentHostMonitoring.DISK_IDLE).asDouble());

            // Disk Total
            ArrayList<String> diskTotalLabelValues = new ArrayList<>(diskGaugeLabelsValues);
            diskTotalLabelValues.add(
                    diskJsonNode.get(AgentHostMonitoring.DISK_NAME).asText());
            diskTotalLabelValues.add(AgentHostMonitoring.DISK_TOTAL);
            labelValues.put(
                    diskTotalLabelValues,
                    diskJsonNode.get(AgentHostMonitoring.DISK_TOTAL).asDouble());
        });
        Map<ArrayList<String>, Map<ArrayList<String>, Double>> diskGauge = new HashMap<>();
        diskGauge.put(diskGaugeLabels, labelValues);
        return diskGauge;
    }

    public static Map<ArrayList<String>, Map<ArrayList<String>, Double>> getCPUGauge(JsonNode agentMonitoring) {

        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        // CPU
        CentralProcessor cpu = hal.getProcessor();
        double[] systemLoadAverage = cpu.getSystemLoadAverage(3);
        long[] systemCpuLoadTicks1 = cpu.getSystemCpuLoadTicks();
        Util.sleep(3000);
        ((ObjectNode) agentMonitoring)
                .put(CPU_LOAD_AVG_MIN_1, systemLoadAverage[0])
                .put(CPU_LOAD_AVG_MIN_5, systemLoadAverage[1])
                .put(CPU_LOAD_AVG_MIN_15, systemLoadAverage[2])
                .put(CPU_USAGE, cpu.getSystemCpuLoadBetweenTicks(systemCpuLoadTicks1));

        BaseAgentGauge gaugeBaseInfo = new BaseAgentGauge(agentMonitoring);
        ArrayList<String> cpuGaugeLabels = gaugeBaseInfo.getLabels();
        ArrayList<String> cpuGaugeLabelsValues = gaugeBaseInfo.getLabelsValues();

        cpuGaugeLabels.add(AgentHostMonitoring.CPU_USAGE);
        Map<ArrayList<String>, Double> labelValues = new HashMap<>();
        ArrayList<String> cpuUsageLabelValues = new ArrayList<>(cpuGaugeLabelsValues);
        cpuUsageLabelValues.add(AgentHostMonitoring.CPU_USAGE);
        ArrayList<String> cpuLoadAvgMin_1_LabelValues = new ArrayList<>(cpuGaugeLabelsValues);
        cpuLoadAvgMin_1_LabelValues.add(AgentHostMonitoring.CPU_LOAD_AVG_MIN_1);
        ArrayList<String> cpuLoadAvgMin_5_LabelValues = new ArrayList<>(cpuGaugeLabelsValues);
        cpuLoadAvgMin_5_LabelValues.add(AgentHostMonitoring.CPU_LOAD_AVG_MIN_5);
        ArrayList<String> cpuLoadAvgMin_15_LabelValues = new ArrayList<>(cpuGaugeLabelsValues);
        cpuLoadAvgMin_15_LabelValues.add(AgentHostMonitoring.CPU_LOAD_AVG_MIN_15);

        labelValues.put(
                cpuUsageLabelValues,
                agentMonitoring.get(AgentHostMonitoring.CPU_USAGE).asDouble());
        labelValues.put(
                cpuLoadAvgMin_1_LabelValues,
                agentMonitoring.get(AgentHostMonitoring.CPU_LOAD_AVG_MIN_1).asDouble());
        labelValues.put(
                cpuLoadAvgMin_5_LabelValues,
                agentMonitoring.get(AgentHostMonitoring.CPU_LOAD_AVG_MIN_5).asDouble());
        labelValues.put(
                cpuLoadAvgMin_15_LabelValues,
                agentMonitoring.get(AgentHostMonitoring.CPU_LOAD_AVG_MIN_15).asDouble());
        Map<ArrayList<String>, Map<ArrayList<String>, Double>> cpuGauge = new HashMap<>();
        cpuGauge.put(cpuGaugeLabels, labelValues);
        return cpuGauge;
    }

    public static Map<ArrayList<String>, Map<ArrayList<String>, Double>> getMEMGauge(JsonNode agentMonitoring) {
        BaseAgentGauge gaugeBaseInfo = new BaseAgentGauge(agentMonitoring);
        ArrayList<String> memGaugeLabels = gaugeBaseInfo.getLabels();
        ArrayList<String> memGaugeLabelsValues = gaugeBaseInfo.getLabelsValues();
        memGaugeLabels.add("memUsage");

        Map<ArrayList<String>, Double> labelValues = new HashMap<>();
        ArrayList<String> memIdleLabelValues = new ArrayList<>(memGaugeLabelsValues);
        memIdleLabelValues.add(AgentHostMonitoring.MEM_IDLE);

        ArrayList<String> memTotalLabelValues = new ArrayList<>(memGaugeLabelsValues);
        memTotalLabelValues.add(AgentHostMonitoring.MEM_TOTAL);

        labelValues.put(
                memIdleLabelValues,
                agentMonitoring.get(AgentHostMonitoring.MEM_IDLE).asDouble());
        labelValues.put(
                memTotalLabelValues,
                agentMonitoring.get(AgentHostMonitoring.MEM_TOTAL).asDouble());
        Map<ArrayList<String>, Map<ArrayList<String>, Double>> memGauge = new HashMap<>();
        memGauge.put(memGaugeLabels, labelValues);
        return memGauge;
    }

    public static MultiGauge newDiskMultiGauge(MeterRegistry registry) {
        return MultiGauge.builder("agent_host_monitoring")
                .description("BigTop Manager Agent Host Monitoring, Disk Monitoring")
                .baseUnit("disk")
                .register(registry);
    }

    public static MultiGauge newMemMultiGauge(MeterRegistry registry) {
        return MultiGauge.builder("agent_host_monitoring")
                .description("BigTop Manager Agent Host Monitoring, Memory Monitoring")
                .baseUnit("mem")
                .register(registry);
    }

    public static MultiGauge newCPUMultiGauge(MeterRegistry registry) {
        return MultiGauge.builder("agent_host_monitoring")
                .description("BigTop Manager Agent Host Monitoring, CPU Monitoring")
                .baseUnit("cpu")
                .register(registry);
    }

    public static void multiGaugeUpdateData(
            MultiGauge multiGauge, Map<ArrayList<String>, Map<ArrayList<String>, Double>> gaugeData) {
        ArrayList<String> tagKeys = null;
        Map<ArrayList<String>, Double> tagValues = null;
        for (Map.Entry<ArrayList<String>, Map<ArrayList<String>, Double>> entry : gaugeData.entrySet()) {
            tagKeys = entry.getKey();
            tagValues = entry.getValue();
        }
        if (null == tagKeys || null == tagValues) {
            return;
        }
        ArrayList<String> finalTagKeys = tagKeys;
        multiGauge.register(
                tagValues.entrySet().stream()
                        .map(item -> {
                            List<Tag> tags = new ArrayList<>();
                            ArrayList<String> labelKeyValues = item.getKey();
                            for (int i = 0; i < labelKeyValues.size(); i++) {
                                tags.add(Tag.of(finalTagKeys.get(i), labelKeyValues.get(i)));
                            }
                            return MultiGauge.Row.of(Tags.of(tags), item.getValue());
                        })
                        .collect(Collectors.toList()),
                true);
    }

    public static void diskMultiGaugeUpdateData(MultiGauge diskMultiGauge) {
        try {
            Map<ArrayList<String>, Map<ArrayList<String>, Double>> diskGauge = getDiskGauge(getHostInfo());
            multiGaugeUpdateData(diskMultiGauge, diskGauge);
        } catch (UnknownHostException e) {
            throw new AgentException(AgentExceptionStatus.AGENT_MONITORING_ERROR);
        }
    }

    public static void memMultiGaugeUpdateData(MultiGauge memMultiGauge) {
        try {
            Map<ArrayList<String>, Map<ArrayList<String>, Double>> diskGauge = getMEMGauge(getHostInfo());
            multiGaugeUpdateData(memMultiGauge, diskGauge);
        } catch (UnknownHostException e) {
            throw new AgentException(AgentExceptionStatus.AGENT_MONITORING_ERROR);
        }
    }

    public static void cpuMultiGaugeUpdateData(MultiGauge cpuMultiGauge) {
        try {
            Map<ArrayList<String>, Map<ArrayList<String>, Double>> diskGauge = getCPUGauge(getHostInfo());
            multiGaugeUpdateData(cpuMultiGauge, diskGauge);
        } catch (UnknownHostException e) {
            throw new AgentException(AgentExceptionStatus.AGENT_MONITORING_ERROR);
        }
    }
}
