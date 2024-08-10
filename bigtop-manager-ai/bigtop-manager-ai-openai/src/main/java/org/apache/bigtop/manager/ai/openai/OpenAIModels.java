package org.apache.bigtop.manager.ai.openai;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Project: org.apache.bigtop.manager.ai.openai
 * @Author: pgthinker
 * @GitHub: https://github.com/ningning0111
 * @Date: 2024/8/10 19:35
 * @Description:
 */
@Getter
@AllArgsConstructor
public enum OpenAIModels {
    GPT_3_5_TURBO("gpt-3.5-turbo");

    private final String value;

}
