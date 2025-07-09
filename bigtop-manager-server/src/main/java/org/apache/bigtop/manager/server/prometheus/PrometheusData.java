package org.apache.bigtop.manager.server.prometheus;

import lombok.Data;

import java.util.List;

@Data
public class PrometheusData {

    // "matrix" | "vector" | "scalar" | "string",
    private String resultType;

    private List<PrometheusResult> result;
}
