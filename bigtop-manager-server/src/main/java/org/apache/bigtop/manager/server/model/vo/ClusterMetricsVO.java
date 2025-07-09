package org.apache.bigtop.manager.server.model.vo;

import lombok.Data;

import java.util.Map;

@Data
public class ClusterMetricsVO {

    private String cpuUsageCur;
    private String memoryUsageCur;

    // timestamp - value pairs for various metrics
    private Map<String, String> cpuUsage;
    private Map<String, String> memoryUsage;
}
