package org.apache.bigtop.manager.ai.assistant;

import dev.langchain4j.data.message.SystemMessage;
import org.apache.bigtop.manager.ai.assistant.provider.LocSystemPromptProvider;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.factory.AIAssistantAbstractFactory;
import org.apache.bigtop.manager.ai.core.factory.ToolBox;
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;
import org.apache.bigtop.manager.ai.core.provider.SystemPromptProvider;
import org.apache.bigtop.manager.ai.openai.OpenAIAssistant;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Project: org.apache.bigtop.manager.ai.core
 * @Author: pgthinker
 * @GitHub: https://github.com/ningning0111
 * @Date: 2024/8/10 16:37
 * @Description:
 */

public class AIAssistantFactory implements AIAssistantAbstractFactory {

    private SystemPromptProvider systemPromptProvider = new LocSystemPromptProvider();

    public AIAssistantFactory(){

    }

    private AIAssistantFactory(SystemPromptProvider systemPromptProvider){
        this.systemPromptProvider = systemPromptProvider;
    }


    @Override
    public AIAssistant createWithPrompt(PlatformType platformType, AIAssistantConfigProvider assistantConfig, Object id, Object promptId) {
        AIAssistant aiAssistant = create(platformType, assistantConfig, id);
        SystemMessage systemPrompt = systemPromptProvider.getSystemPrompt(promptId);
        aiAssistant.setSystemPrompt(systemPrompt);
        return aiAssistant;
    }

    @Override
    public AIAssistant create(PlatformType platformType, AIAssistantConfigProvider assistantConfig, Object id) {
        if (Objects.requireNonNull(platformType) == PlatformType.OPENAI) {
            AIAssistant aiAssistant = OpenAIAssistant.builder()
                    .id(id)
                    .withConfigProvider(assistantConfig)
                    .build();
            aiAssistant.setSystemPrompt(systemPromptProvider.getSystemPrompt());
            return aiAssistant;
        }
        return null;
    }

    @Override
    public ToolBox createToolBox(PlatformType platformType) {
        return null;
    }

}
