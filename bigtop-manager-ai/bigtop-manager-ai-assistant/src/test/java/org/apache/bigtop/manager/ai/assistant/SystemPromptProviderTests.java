package org.apache.bigtop.manager.ai.assistant;

import org.apache.bigtop.manager.ai.assistant.provider.LocSystemPromptProvider;
import org.apache.bigtop.manager.ai.core.provider.SystemPromptProvider;
import org.junit.Test;

/**
 * @Project: org.apache.bigtop.manager.ai.assistant
 * @Author: pgthinker
 * @GitHub: https://github.com/ningning0111
 * @Date: 2024/8/13 22:43
 * @Description:
 */
public class SystemPromptProviderTests {

    private SystemPromptProvider systemPromptProvider = new LocSystemPromptProvider();

    @Test
    public void loadSystemPromptTest(){
        System.out.println(systemPromptProvider.getSystemPrompt());
    }
}
