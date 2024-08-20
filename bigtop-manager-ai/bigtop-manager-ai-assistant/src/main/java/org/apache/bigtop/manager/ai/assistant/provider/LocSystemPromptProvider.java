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

import dev.langchain4j.data.message.SystemMessage;
import org.apache.bigtop.manager.ai.core.provider.SystemPromptProvider;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

public class LocSystemPromptProvider implements SystemPromptProvider {

    public final static String DEFAULT = "default";
    @Override
    public SystemMessage getSystemPrompt(Object id) {
        if(Objects.equals(id.toString(), DEFAULT)){
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
