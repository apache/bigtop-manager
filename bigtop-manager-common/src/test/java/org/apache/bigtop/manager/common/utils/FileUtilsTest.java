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
package org.apache.bigtop.manager.common.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUtilsTest {
    private File testFile;

    @BeforeEach
    void setUp() throws IOException {
        // Create a test file
        testFile = new File("testFile.txt");
        try (FileWriter writer = new FileWriter(testFile, StandardCharsets.UTF_8)) {
            writer.write("test file content");
        }
    }

    @AfterEach
    void tearDown() {
        // Delete test file
        if (testFile.exists()) {
            testFile.delete();
        }
    }

    @Test
    void readFile2StrNormalCase() {
        // Test normal case
        String result = FileUtils.readFile2Str(testFile);
        assertEquals("test file content", result);
    }

    @Test
    void readFile2StrEmptyFile() throws IOException {
        // Test empty file
        try (FileWriter writer = new FileWriter(testFile, StandardCharsets.UTF_8)) {
            writer.write("");
        }
        String result = FileUtils.readFile2Str(testFile);
        assertEquals("", result);
    }

    @Test
    void readFile2StrFileDoesNotExist() {
        // Test file does not exist case
        File nonExistentFile = new File("nonExistentFile.txt");
        Exception exception = assertThrows(RuntimeException.class, () -> {
            FileUtils.readFile2Str(nonExistentFile);
        });
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("nonExistentFile.txt"));
    }

    @Test
    void readFile2StrFileContainsSpecialCharacters() throws IOException {
        // Test file containing special characters
        try (FileWriter writer = new FileWriter(testFile, StandardCharsets.UTF_8)) {
            writer.write("test file content\nwith newline\tand tab");
        }
        String result = FileUtils.readFile2Str(testFile);
        assertEquals("test file content\nwith newline\tand tab", result);
    }

    @Test
    void readFile2StrFileContainsChineseCharacters() throws IOException {
        // Test file containing Chinese characters
        try (FileWriter writer = new FileWriter(testFile, StandardCharsets.UTF_8)) {
            writer.write("测试文件内容");
        }
        String result = FileUtils.readFile2Str(testFile);
        assertEquals("测试文件内容", result);
    }
}
