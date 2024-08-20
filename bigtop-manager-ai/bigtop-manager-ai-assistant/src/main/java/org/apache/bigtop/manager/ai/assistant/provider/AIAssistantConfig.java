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
    private final Map<String, String> configMap;

    private AIAssistantConfig(Map<String, String> configMap){
        this.configMap = configMap;
    }

    public static Builder builder(){
        return new Builder();
    }

    public static Builder withDefault(String baseUrl, String apiKey){
        Builder builder = new Builder();
        return builder.set("baseUrl", baseUrl).set("apiKey", apiKey);
    }

    @Override
    public Map<String, String> configs() {

        return configMap;
    }

    public static class Builder{
        private final Map<String,String> configs;
        public Builder(){
            configs = new HashMap<>();
            configs.put("memoryLen", "30");
        }

        public Builder set(String key, String value){
            configs.put(key,value);
            return this;
        }

        public AIAssistantConfig build(){
            return new AIAssistantConfig(configs);
        }
    }

}
