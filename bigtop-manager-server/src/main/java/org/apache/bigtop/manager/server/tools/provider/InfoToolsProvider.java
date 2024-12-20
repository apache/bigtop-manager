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
package org.apache.bigtop.manager.server.tools.provider;

import org.apache.bigtop.manager.server.tools.functions.ClusterInfoFunctions;
import org.apache.bigtop.manager.server.tools.functions.HostInfoFunctions;
import org.apache.bigtop.manager.server.tools.functions.StackInfoFunctions;

import org.springframework.stereotype.Component;

import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderRequest;
import dev.langchain4j.service.tool.ToolProviderResult;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;

@Component
@Slf4j
public class InfoToolsProvider implements ToolProvider {
    @Resource
    private ClusterInfoFunctions clusterInfoFunctions;

    @Resource
    private HostInfoFunctions hostInfoFunctions;

    @Resource
    private StackInfoFunctions stackInfoFunctions;

    @Override
    public ToolProviderResult provideTools(ToolProviderRequest toolProviderRequest) {
        return ToolProviderResult.builder()
                .addAll(clusterInfoFunctions.getAllFunctions())
                .addAll(hostInfoFunctions.getAllFunctions())
                .addAll(stackInfoFunctions.getAllFunctions())
                .build();
    }
}
