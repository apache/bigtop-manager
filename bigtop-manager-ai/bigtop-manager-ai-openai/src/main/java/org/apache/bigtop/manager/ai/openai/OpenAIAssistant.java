package org.apache.bigtop.manager.ai.openai;

import dev.langchain4j.internal.ValidationUtils;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.apache.bigtop.manager.ai.core.AbstractAIAssistant;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;
import org.springframework.util.NumberUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Project: org.apache.bigtop.manager.ai.openapi
 * @Author: pgthinker
 * @GitHub: https://github.com/ningning0111
 * @Date: 2024/8/10 16:26
 * @Description:
 */
public class OpenAIAssistant extends AbstractAIAssistant {

    private final static String PLATFORM_NAME = "openai";
    private final static String BASE_URL = "https://api.openai.com/v1";
    private final static String MODEL_NAME = "gpt-3.5-turbo";

    private OpenAIAssistant(ChatLanguageModel chatLanguageModel, StreamingChatLanguageModel streamingChatLanguageModel, ChatMemory chatMemory) {
        super(chatLanguageModel, streamingChatLanguageModel,chatMemory);
    }


    @Override
    public String getPlatform() {
        return PLATFORM_NAME;
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder{
        private Object id;

        private Map<String,String> configs = new HashMap<>();

        public Builder(){
            configs.put("baseUrl",BASE_URL);
            configs.put("modelName", MODEL_NAME);
        }

        public Builder withConfigProvider(AIAssistantConfigProvider configProvider){
            this.configs = configProvider.configs();
            return this;
        }

        public Builder id(Object id){
            this.id = id;
            return this;
        }

        public AIAssistant build(){
            ValidationUtils.ensureNotNull(id, "id");
            String baseUrl = configs.get("baseUrl");
            String modelName = configs.get("modelName");
            String apiKey = ValidationUtils.ensureNotNull(configs.get("apiKey"), "apiKey");
            Integer memoryLen = ValidationUtils.ensureNotNull(NumberUtils.parseNumber(configs.get("memoryLen"), Integer.class), "memoryLen not a number.");
            ChatLanguageModel openAiChatModel = OpenAiChatModel.builder().apiKey(apiKey).baseUrl(baseUrl).modelName(modelName).build();
            StreamingChatLanguageModel openaiStreamChatModel = OpenAiStreamingChatModel.builder().apiKey(apiKey).baseUrl(baseUrl).modelName(modelName).build();
            MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder().id(id).maxMessages(memoryLen).build();
            return new OpenAIAssistant(openAiChatModel,openaiStreamChatModel,chatMemory);
        }

    }


}
