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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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

@Component
public class PrometheusProxy {

    private final WebClient webClient;

    @Value("${monitoring.agent-host-job-name}")
    private String agentHostJobName;

    public PrometheusProxy(WebClient.Builder webClientBuilder,
                           @Value("${monitoring.prometheus-host}") String prometheusHost) {
        this.webClient = webClientBuilder.baseUrl(prometheusHost).build();
    }

    public JsonNode queryAgentsHealthyStatus() {
        Mono<JsonNode> body = webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/query").build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("query", "up{job=\"%s\"}".formatted(agentHostJobName)).with("timeout",
                        "10"))
                .retrieve().bodyToMono(JsonNode.class);
        JsonNode result = body.block();

        ObjectMapper objectMapper = new ObjectMapper();
        if (result == null || result.isEmpty() || !"success".equals(result.get("status").asText("failure"))) {
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
            LocalDateTime instant =
                    Instant.ofEpochSecond(status.get(0).asLong()).atZone(ZoneId.systemDefault()).toLocalDateTime();
            temp.put("time", instant.toString());
            temp.put("agentHealthyStatus", status.get(1).asInt() == 1 ? "running" : "down");
            agentsHealthyStatus.add(temp);
        }
        return agentsHealthyStatus;
    }

}
