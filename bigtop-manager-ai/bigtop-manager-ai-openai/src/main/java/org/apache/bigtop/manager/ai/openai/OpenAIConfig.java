package org.apache.bigtop.manager.ai.openai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;

/**
 * @Project: org.apache.bigtop.manager.ai.openai
 * @Author: pgthinker
 * @GitHub: https://github.com/ningning0111
 * @Date: 2024/8/10 19:24
 * @Description:
 */
@Data
@Builder
public class OpenAIConfig implements AIAssistantConfigProvider {
    private final static String BASE_URL = "https://api.openai.com";
    private final static String MODEL = OpenAIModels.GPT_3_5_TURBO.getValue();
    private int memoryLen;
    private String apiKey;
    private String baseUrl;
    private String modelName;

    public static OpenAIConfig withDefault(String apiKey){
        return OpenAIConfig.withDefault(apiKey,BASE_URL);
    }

    public static OpenAIConfig withDefault(String apiKey, String baseUrl){
        return OpenAIConfig.builder().apiKey(apiKey).baseUrl(baseUrl).memoryLen(30).modelName(MODEL).build();
    }

}
