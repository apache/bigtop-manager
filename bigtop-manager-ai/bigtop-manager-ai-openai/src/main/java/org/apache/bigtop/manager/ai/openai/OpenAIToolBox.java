package org.apache.bigtop.manager.ai.openai;

import org.apache.bigtop.manager.ai.core.factory.ToolBox;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @Project: org.apache.bigtop.manager.ai.openai
 * @Author: pgthinker
 * @GitHub: https://github.com/ningning0111
 * @Date: 2024/8/10 16:33
 * @Description:
 */
public class OpenAIToolBox implements ToolBox {
    @Override
    public List<String> getTools() {
        return null;
    }

    @Override
    public String invoke(String toolName) {
        return null;
    }


    @Override
    public Flux<String> streamInvoke(String toolName) {
        return null;
    }
}
