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
package org.apache.bigtop.manager.agent.executor;

import org.apache.bigtop.manager.common.constants.MessageConstants;
import org.apache.bigtop.manager.common.message.entity.payload.CacheMessagePayload;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.common.utils.ProjectPathUtils;
import org.apache.bigtop.manager.grpc.generated.CommandType;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

import static org.apache.bigtop.manager.common.constants.CacheFiles.CLUSTER_INFO;
import static org.apache.bigtop.manager.common.constants.CacheFiles.COMPONENTS_INFO;
import static org.apache.bigtop.manager.common.constants.CacheFiles.CONFIGURATIONS_INFO;
import static org.apache.bigtop.manager.common.constants.CacheFiles.HOSTS_INFO;
import static org.apache.bigtop.manager.common.constants.CacheFiles.REPOS_INFO;
import static org.apache.bigtop.manager.common.constants.CacheFiles.SETTINGS_INFO;
import static org.apache.bigtop.manager.common.constants.CacheFiles.USERS_INFO;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CacheFileUpdateCommandExecutor extends AbstractCommandExecutor {

    @Override
    public CommandType getCommandType() {
        return CommandType.UPDATE_CACHE_FILES;
    }

    @Override
    public void doExecute() {
        CacheMessagePayload cacheMessagePayload =
                JsonUtils.readFromString(commandRequest.getPayload(), CacheMessagePayload.class);
        String cacheDir = ProjectPathUtils.getAgentCachePath();
        Path p = Paths.get(cacheDir);
        if (!Files.exists(p)) {
            try {
                Files.createDirectories(p);
            } catch (Exception e) {
                log.error("Create directory failed: {}", cacheDir, e);
                throw new RuntimeException("Create directory failed: " + cacheDir, e);
            }
        }

        JsonUtils.writeToFile(cacheDir + SETTINGS_INFO, cacheMessagePayload.getSettings());
        JsonUtils.writeToFile(cacheDir + CONFIGURATIONS_INFO, cacheMessagePayload.getConfigurations());
        JsonUtils.writeToFile(cacheDir + HOSTS_INFO, cacheMessagePayload.getClusterHostInfo());
        JsonUtils.writeToFile(cacheDir + USERS_INFO, cacheMessagePayload.getUserInfo());
        JsonUtils.writeToFile(cacheDir + COMPONENTS_INFO, cacheMessagePayload.getComponentInfo());
        JsonUtils.writeToFile(cacheDir + REPOS_INFO, cacheMessagePayload.getRepoInfo());
        JsonUtils.writeToFile(cacheDir + CLUSTER_INFO, cacheMessagePayload.getClusterInfo());

        commandReplyBuilder.setCode(MessageConstants.SUCCESS_CODE);
        commandReplyBuilder.setResult(
                MessageFormat.format("Host [{0}] cached successful!!!", commandRequest.getHostname()));
    }
}
