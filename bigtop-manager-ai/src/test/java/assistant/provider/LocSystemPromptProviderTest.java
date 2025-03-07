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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class LocSystemPromptProviderTest {

    private LocSystemPromptProvider promptProvider;

    @BeforeEach
    public void setUp() {
        promptProvider = new LocSystemPromptProvider();
    }

    @Test
    public void testGetSystemMessageWithDefaultPrompt() {
        try (MockedStatic<ResourceUtils> resourceUtilsMock = Mockito.mockStatic(ResourceUtils.class)) {
            resourceUtilsMock
                    .when(() -> ResourceUtils.getFile(anyString()))
                    .thenThrow(new NullPointerException("File not found"));

            String result = promptProvider.getSystemMessage(SystemPrompt.DEFAULT_PROMPT);

            assertEquals("You are a helpful assistant.", result);

            result = promptProvider.getSystemMessage();
            assertEquals("You are a helpful assistant.", result);
        }
    }

    @Test
    public void testGetSystemMessageWithCustomPrompt() {
        try (MockedStatic<ResourceUtils> resourceUtilsMock = Mockito.mockStatic(ResourceUtils.class);
                MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {

            File mockFile = mock(File.class);
            resourceUtilsMock.when(() -> ResourceUtils.getFile(anyString())).thenReturn(mockFile);
            filesMock
                    .when(() -> Files.readString(any(), eq(StandardCharsets.UTF_8)))
                    .thenReturn("Custom System Prompt");

            String result = promptProvider.getSystemMessage(SystemPrompt.BIGDATA_PROFESSOR);

            assertEquals("Custom System Prompt", result);
        }
    }

    @Test
    public void testGetSystemMessageWithFileNotFound() {
        try (MockedStatic<ResourceUtils> resourceUtilsMock = Mockito.mockStatic(ResourceUtils.class)) {
            resourceUtilsMock
                    .when(() -> ResourceUtils.getFile(anyString()))
                    .thenThrow(new NullPointerException("File not found"));

            String result = promptProvider.getSystemMessage(SystemPrompt.BIGDATA_PROFESSOR);

            assertEquals("You are a helpful assistant.", result);
        }
    }

    @Test
    public void testGetLanguagePromptWithValidLocale() {
        try (MockedStatic<ResourceUtils> resourceUtilsMock = Mockito.mockStatic(ResourceUtils.class);
                MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {

            File mockFile = mock(File.class);
            resourceUtilsMock.when(() -> ResourceUtils.getFile(anyString())).thenReturn(mockFile);
            filesMock
                    .when(() -> Files.readString(any(), eq(StandardCharsets.UTF_8)))
                    .thenReturn("Answer in English");

            String result = promptProvider.getLanguagePrompt("en_US");

            assertEquals("Answer in English", result);
        }
    }

    @Test
    public void testGetLanguagePromptWithMissingFile() {
        try (MockedStatic<ResourceUtils> resourceUtilsMock = Mockito.mockStatic(ResourceUtils.class)) {
            resourceUtilsMock
                    .when(() -> ResourceUtils.getFile(anyString()))
                    .thenThrow(new NullPointerException("File not found"));

            String result = promptProvider.getLanguagePrompt("fr");

            assertEquals("Answer in fr", result);
        }
    }
}
