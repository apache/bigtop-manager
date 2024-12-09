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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import reactor.core.publisher.Mono;

@Component
public class BigtopProxy {

    private final WebClient webClient;

    public BigtopProxy(WebClient.Builder webClientBuilder, @Value("${monitoring.bigtop-host}") String bigTopHost) {
        this.webClient = webClientBuilder.baseUrl(bigTopHost).build();
    }

    private MultiValueMap<String, String> createFormData(Integer pageNum) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("pageNum", String.valueOf(pageNum));
        formData.add("pageSize", "20");
        formData.add("orderBy", "id");
        formData.add("sort", "asc");
        return formData;
    }

    public JsonNode queryHosts(int pageNum) {
        Mono<JsonNode> body = webClient
                .post()
                .uri(uriBuilder -> uriBuilder.path("/api/hosts").build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(createFormData(pageNum)))
                .retrieve()
                .bodyToMono(JsonNode.class);
        JsonNode result = body.block();
        if (result == null || result.isEmpty() || !(result.get("failed").asBoolean())) {
            return null;
        }
        return result;
    }

    public JsonNode queryClusterAgents(String clusterId) {
        ObjectMapper objectMapper = new ObjectMapper();
        // query cluster name
        Mono<JsonNode> body =
                webClient.get().uri("/api/clusters/{id}", clusterId).retrieve().bodyToMono(JsonNode.class);
        JsonNode result = body.block();
        if (result == null || result.isEmpty() || !(result.get("failed").asBoolean())) return null;
        String clusterName = result.get("data").get("name").asText();
        ObjectNode clusterAgents = objectMapper.createObjectNode();
        ArrayNode arrayNode = objectMapper.createArrayNode();
        int pageNum = 1;
        while (true) {
            JsonNode agentsPage = queryHosts(pageNum++);
            if (agentsPage == null) break;
            JsonNode agentsContent = agentsPage.get("data").get("content");
            agentsContent.forEach(agent -> {
                if (agent.get("clusterName").asText().equals(clusterName))
                    arrayNode.add(agent.get("ipv4").asText());
            });
        }
        clusterAgents.put("agentsNum", arrayNode.size());
        clusterAgents.set("agents", arrayNode);
        return clusterAgents;
    }
}
