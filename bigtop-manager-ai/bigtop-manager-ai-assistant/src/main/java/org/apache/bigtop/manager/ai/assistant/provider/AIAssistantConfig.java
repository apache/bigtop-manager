package org.apache.bigtop.manager.ai.assistant.provider;

import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * @Project: org.apache.bigtop.manager.ai.core.provider
 * @Author: pgthinker
 * @GitHub: https://github.com/ningning0111
 * @Date: 2024/8/11 12:54
 * @Description:
 */
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
