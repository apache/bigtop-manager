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
package org.apache.bigtop.manager.ai.core.factory;

import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.exception.PlatformNotFoundException;
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;

import java.util.Objects;
import java.util.UUID;

public interface AIAssistantFactory {

    AIAssistant createWithPrompt(
            PlatformType platformType, AIAssistantConfigProvider assistantConfig, Object id, Object promptId);

    AIAssistant create(PlatformType platformType, AIAssistantConfigProvider assistantConfig, Object id);

    ToolBox createToolBox(PlatformType platformType);

    default AIAssistant createWithPrompt(
            PlatformType platformType, AIAssistantConfigProvider assistantConfig, Object prompt) {
        return createWithPrompt(platformType, assistantConfig, UUID.randomUUID().toString(), prompt);
    }

    default AIAssistant create(String platform, AIAssistantConfigProvider assistantConfigProvider, Object id) {
        PlatformType platformType = PlatformType.getPlatformType(platform);
        if (Objects.isNull(platformType)) {
            throw new PlatformNotFoundException(platform);
        }
        return create(platformType, assistantConfigProvider, id);
    }

    default AIAssistant create(PlatformType platformType, AIAssistantConfigProvider assistantConfigProvider) {
        return create(platformType, assistantConfigProvider, UUID.randomUUID().toString());
    }

    default AIAssistant create(String platform, AIAssistantConfigProvider assistantConfig) {
        PlatformType platformType = PlatformType.getPlatformType(platform);
        if (Objects.isNull(platformType)) {
            throw new PlatformNotFoundException(platform);
        }
        return create(platformType, assistantConfig);
    }

    default ToolBox createToolBox(String platform) {
        PlatformType platformType = PlatformType.getPlatformType(platform);
        if (Objects.isNull(platformType)) {
            throw new PlatformNotFoundException(platform);
        }
        return createToolBox(platformType);
    }
}
