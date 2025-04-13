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
package org.apache.bigtop.manager.ai.assistant.config;

import org.apache.bigtop.manager.ai.core.config.AIAssistantConfig;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
public class GeneralAssistantConfig implements AIAssistantConfig {

    private final Long id;
    private final String model;
    private final String language;
    private final PlatformType platformType;
    private final Map<String, String> credentials;
    private final Map<String, String> configs;

    private GeneralAssistantConfig(Builder builder) {
        this.model = Objects.requireNonNull(builder.model);
        this.credentials = Objects.requireNonNull(builder.credentials);
        this.platformType = Objects.requireNonNull(builder.platformType);
        this.language = builder.language;
        this.id = builder.id;
        this.configs = builder.configs;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public Map<String, String> getCredentials() {
        return credentials;
    }

    @Override
    public Map<String, String> getConfigs() {
        return configs;
    }

    public static class Builder {
        private Long id;
        private String model;
        private String language;
        private PlatformType platformType;
        private final Map<String, String> credentials = new HashMap<>();
        private final Map<String, String> configs = new HashMap<>();

        public Builder setModel(String model) {
            this.model = model;
            return this;
        }

        public Builder setPlatformType(PlatformType platformType) {
            this.platformType = platformType;
            return this;
        }

        public Builder setId(Long id) {
            this.id = id;
            return this;
        }

        public Builder addCredential(String key, String value) {
            credentials.put(key, value);
            return this;
        }

        public Builder addCredentials(Map<String, String> credentialMap) {
            credentials.putAll(credentialMap);
            return this;
        }

        public Builder setLanguage(String language) {
            this.language = language;
            return this;
        }

        public Builder addConfig(String key, String value) {
            configs.put(key, value);
            return this;
        }

        public Builder addConfigs(Map<String, String> configMap) {
            if (configMap != null) {
                configs.putAll(configMap);
            }
            return this;
        }

        public GeneralAssistantConfig build() {
            return new GeneralAssistantConfig(this);
        }
    }
}
