package org.apache.bigtop.manager.ai.core.factory;

import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @Project: org.apache.bigtop.manager.ai.core.factory
 * @Author: pgthinker
 * @GitHub: https://github.com/ningning0111
 * @Date: 2024/8/10 15:55
 * @Description:
 */
public interface ToolBox {

    List<String> getTools();

    String invoke(String toolName);

    Flux<String> streamInvoke(String toolName);
}
