package org.apache.bigtop.manager.server.prometheus;

import lombok.Data;

/**
 * <a href="https://prometheus.io/docs/prometheus/latest/querying/api/">Prometheus HTTP API</a>
 */
@Data
public class PrometheusResponse {

    // "success" | "error"
    private String status;

    private PrometheusData data;

    // Only set if status is "error". The data field may still hold additional data.
    private String errorType;

    private String error;
}
