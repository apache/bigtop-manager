package org.apache.bigtop.manager.ai.core.provider;

/**
 * @Project: org.apache.bigtop.manager.ai.core.provider
 * @Author: pgthinker
 * @GitHub: https://github.com/ningning0111
 * @Date: 2024/8/10 18:49
 * @Description:
 */
public interface AIAssistantConfigProvider {
    int getMemoryLen();
    String getApiKey();
    String getBaseUrl();
    String getModelName();
}
