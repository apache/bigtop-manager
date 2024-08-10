package org.apache.bigtop.manager.ai.openai;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.apache.bigtop.manager.ai.core.AbstractAIAssistant;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;

/**
 * @Project: org.apache.bigtop.manager.ai.openapi
 * @Author: pgthinker
 * @GitHub: https://github.com/ningning0111
 * @Date: 2024/8/10 16:26
 * @Description:
 */
public class OpenAIAssistant extends AbstractAIAssistant {

    private final static String PLATFORM_NAME = "openai";
    private OpenAIAssistant(ChatLanguageModel chatLanguageModel, StreamingChatLanguageModel streamingChatLanguageModel, ChatMemory chatMemory) {
        super(chatLanguageModel, streamingChatLanguageModel, chatMemory);
    }

    @Override
    public String getPlatform() {
        return PLATFORM_NAME;
    }

    public static AIAssistant create(AIAssistantConfigProvider configProvider) {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(configProvider.getMemoryLen());
        ChatLanguageModel openAiChatModel = OpenAiChatModel.builder()
                .baseUrl(configProvider.getBaseUrl())
                .apiKey(configProvider.getApiKey())
                .modelName(configProvider.getModelName())
                .build();
        StreamingChatLanguageModel openAiStreamChatModel = OpenAiStreamingChatModel.builder()
                .baseUrl(configProvider.getBaseUrl())
                .apiKey(configProvider.getApiKey())
                .modelName(configProvider.getModelName())
                .build();
        return new OpenAIAssistant(openAiChatModel,openAiStreamChatModel,chatMemory);
    }
}
