package org.apache.bigtop.manager.server.prometheus;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PrometheusResult {

    private Map<String, String> metric;

    // For resultType "vector" only
    // Contains a single value of [timestamp, value]
    private List<String> value;

    // For resultType "matrix" only
    // Contains a list of values, each value is a list of [timestamp, value]
    private List<List<String>> values;
}
