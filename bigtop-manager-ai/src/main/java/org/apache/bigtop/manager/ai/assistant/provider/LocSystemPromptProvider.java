/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.bigtop.manager.ai.assistant.provider;

import org.apache.bigtop.manager.ai.core.enums.SystemPrompt;
import org.apache.bigtop.manager.ai.core.provider.SystemPromptProvider;

import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class LocSystemPromptProvider implements SystemPromptProvider {

    @Override
    public String getSystemMessage(SystemPrompt systemPrompt) {
        if (systemPrompt == SystemPrompt.DEFAULT_PROMPT) {
            systemPrompt = SystemPrompt.BIGDATA_PROFESSOR;
        }

        return loadPromptFromFile(systemPrompt.getValue());
    }

    @Override
    public String getSystemMessage() {
        return getSystemMessage(SystemPrompt.DEFAULT_PROMPT);
    }

    private String loadTextFromFile(String fileName) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            String fullPath =
                    Objects.requireNonNull(classLoader.getResource(fileName)).getPath();
            File file = ResourceUtils.getFile(fullPath);
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException | NullPointerException e) {
            log.error(
                    "Exception occurred while loading SystemPrompt from local. Here is some information:{}",
                    e.getMessage());

            return null;
        }
    }

    private String loadPromptFromFile(String fileName) {
        final String filePath = fileName + ".st";
        String text = loadTextFromFile(filePath);
        return Objects.requireNonNullElse(text, "You are a helpful assistant.");
    }

    @Override
    public String getLanguagePrompt(String locale) {
        final String filePath = SystemPrompt.LANGUAGE_PROMPT.getValue() + '-' + locale + ".st";
        String text = loadTextFromFile(filePath);
        return Objects.requireNonNullElseGet(text, () -> "Answer in " + locale);
    }

    @Override
    public String getSystemMessages(List<String> systemPrompts) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < systemPrompts.size(); i++) {
            stringBuilder.append(String.format("prompt%d: %s\n", i + 1, systemPrompts.get(i)));
        }
        return stringBuilder.toString();
    }
}
