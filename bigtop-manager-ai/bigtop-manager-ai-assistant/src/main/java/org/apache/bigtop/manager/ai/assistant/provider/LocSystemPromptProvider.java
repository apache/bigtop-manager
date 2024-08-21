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

import org.apache.bigtop.manager.ai.core.provider.SystemPromptProvider;

import org.springframework.util.ResourceUtils;

import dev.langchain4j.data.message.SystemMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

@Slf4j
public class LocSystemPromptProvider implements SystemPromptProvider {

    public static final String DEFAULT = "default";
    private static final String SYSTEM_PROMPT_PATH = "src/main/resources/";
    private static final String DEFAULT_NAME = "big-data-professor.st";

    @Override
    public SystemMessage getSystemPrompt(Object id) {
        if (Objects.equals(id.toString(), DEFAULT)) {
            return getSystemPrompt();
        } else {
            return loadPromptFromFile(id.toString());
        }
    }

    @Override
    public SystemMessage getSystemPrompt() {
        return loadPromptFromFile(DEFAULT_NAME);
    }

    private SystemMessage loadPromptFromFile(String fileName) {
        final String filePath = SYSTEM_PROMPT_PATH + fileName;
        try {
            File file = ResourceUtils.getFile(filePath);
            String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            return SystemMessage.from(text);
        } catch (IOException e) {
            //
            log.error(
                    "Exception occurred while loading SystemPrompt from local. Here is some information:{}",
                    e.getMessage());
            return SystemMessage.from("");
        }
    }
}
