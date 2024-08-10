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
package org.apache.bigtop.manager.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ServerApplicationTests {

    @Test
    void contextLoads() {}

    @Test
    public void queryMonitoring() {
        WebClient webClient =
                WebClient.builder().baseUrl("http://localhost:9090").build();
        Mono<String> body = webClient
                .get()
                .uri("/api/v1/query?query=absent(up{job=bm-agent-host}==1)", "")
                .retrieve()
                .bodyToMono(String.class);
        System.out.println(body.block());
    }

    @Test
    public void AiAssistant(){
        new AIAssistantFactory();
    }
}
