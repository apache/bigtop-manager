package org.apache.bigtop.manager.ai.assistant;

import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.factory.InAIAssistantFactory;
import org.apache.bigtop.manager.ai.core.factory.ToolBox;
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;
import org.apache.bigtop.manager.ai.openai.OpenAIAssistant;

import java.util.Objects;

/**
 * @Project: org.apache.bigtop.manager.ai.core
 * @Author: pgthinker
 * @GitHub: https://github.com/ningning0111
 * @Date: 2024/8/10 16:37
 * @Description:
 */

public class AIAssistantFactory implements InAIAssistantFactory {


    @Override
    public AIAssistant create(PlatformType platformType, AIAssistantConfigProvider configProvider) {
        if (Objects.requireNonNull(platformType) == PlatformType.OPENAI) {
            return OpenAIAssistant.create(configProvider);
        }
        return null;
    }

    @Override
    public ToolBox createToolBox(PlatformType platformType) {
        return null;
    }
}
