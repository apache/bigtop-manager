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
package org.apache.bigtop.manager.ai.assistant.provider;

import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;

import java.util.HashMap;
import java.util.Map;

public class AIAssistantConfig implements AIAssistantConfigProvider {

    /**
     * Model name for platform that we want to use
     */
    private final String model;

    /**
     * Credentials for different platforms
     */
    private final Map<String, String> credentials;

    /**
     * Platform extra configs are put here
     */
    private final Map<String, String> configs;

    private AIAssistantConfig(String model, Map<String, String> credentials, Map<String, String> configMap) {
        this.model = model;
        this.credentials = credentials;
        this.configs = configMap;
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
        private String model;

        private final Map<String, String> credentials = new HashMap<>();

        private final Map<String, String> configs = new HashMap<>();

        public Builder() {}

        public Builder setModel(String model) {
            this.model = model;
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

        public Builder addConfig(String key, String value) {
            configs.put(key, value);
            return this;
        }

        public Builder addConfigs(Map<String, String> configMap) {
            configs.putAll(configMap);
            return this;
        }

        public AIAssistantConfig build() {
            return new AIAssistantConfig(model, credentials, configs);
        }
    }
}
