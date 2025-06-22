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

import org.apache.bigtop.manager.server.service.ClusterService;
import org.apache.bigtop.manager.server.service.CommandService;
import org.apache.bigtop.manager.server.service.HostService;
import org.apache.bigtop.manager.server.service.LLMConfigService;
import org.apache.bigtop.manager.server.service.StackService;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication(
        scanBasePackages = {
            "org.apache.bigtop.manager.server",
            "org.apache.bigtop.manager.common",
            "org.apache.bigtop.manager.ai"
        })
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider mcpTools(
            LLMConfigService llmConfigService,
            ClusterService clusterService,
            StackService stackService,
            CommandService commandService,
            HostService hostService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(llmConfigService, clusterService, stackService, commandService, hostService)
                .build();
    }
}
