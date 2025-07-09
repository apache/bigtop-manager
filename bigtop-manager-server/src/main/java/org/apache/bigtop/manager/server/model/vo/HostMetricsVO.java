package org.apache.bigtop.manager.server.model.vo;

import lombok.Data;

import java.util.Map;

@Data
public class HostMetricsVO {

    private String cpuUsageCur;
    private String memoryUsageCur;
    private String diskUsageCur;
    private String fileDescriptorUsage;
    private String diskReadCur;
    private String diskWriteCur;

    // timestamp - value pairs for various metrics
    private Map<String, String> cpuUsage;
    private Map<String, String> systemLoad1;
    private Map<String, String> systemLoad5;
    private Map<String, String> systemLoad15;
    private Map<String, String> memoryUsage;
    private Map<String, String> diskRead;
    private Map<String, String> diskWrite;
}
