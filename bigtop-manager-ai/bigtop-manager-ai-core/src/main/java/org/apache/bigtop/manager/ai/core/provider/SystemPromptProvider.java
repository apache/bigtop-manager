package org.apache.bigtop.manager.ai.core.provider;

import dev.langchain4j.data.message.SystemMessage;

/**
 * @Project: org.apache.bigtop.manager.ai.core.provider
 * @Author: pgthinker
 * @GitHub: https://github.com/ningning0111
 * @Date: 2024/8/11 14:46
 * @Description:
 */
public interface SystemPromptProvider {
    SystemMessage getSystemPrompt(Object id);

    SystemMessage getSystemPrompt();
}
