package org.apache.bigtop.manager.ai.assistant.provider;

import dev.langchain4j.data.message.SystemMessage;
import org.apache.bigtop.manager.ai.core.provider.SystemPromptProvider;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

/**
 * @Project: org.apache.bigtop.manager.ai.assistant.provider
 * @Author: pgthinker
 * @GitHub: https://github.com/ningning0111
 * @Date: 2024/8/13 22:15
 * @Description:
 */
public class LocSystemPromptProvider implements SystemPromptProvider {

    public final static String DEFAULT = "default";
    @Override
    public SystemMessage getSystemPrompt(Object id) {
        if(Objects.equals(id.toString(), "default")){
            return getSystemPrompt();
        }
        return null;
    }

    @Override
    public SystemMessage getSystemPrompt() {
        try {
            File file = ResourceUtils.getFile("src/main/resources/big-data-professor.st");
            String systemStr = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            return SystemMessage.from(systemStr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
