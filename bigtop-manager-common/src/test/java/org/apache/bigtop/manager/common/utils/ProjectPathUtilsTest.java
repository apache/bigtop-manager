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

import org.apache.commons.lang3.SystemUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

public class ProjectPathUtilsTest {

    private MockedStatic<SystemUtils> mockedSystemUtils;
    private MockedStatic<Environments> mockedEnvironments;

    @BeforeEach
    public void setup() {
        mockedSystemUtils = Mockito.mockStatic(SystemUtils.class);
        mockedEnvironments = Mockito.mockStatic(Environments.class);
        mockedEnvironments.when(Environments::isDevMode).thenReturn(true);
    }

    @AfterEach
    public void cleanup() {
        mockedSystemUtils.close();
        mockedEnvironments.close();
    }

    @Test
    public void testGetLogFilePath() {
        Long taskId = 123L;
        String expectedLogPath = "baseDir" + File.separator + "tasklogs" + File.separator + "task-" + taskId + ".log";

        mockedSystemUtils.when(SystemUtils::getUserDir).thenReturn(new File("baseDir"));

        String logFilePath = ProjectPathUtils.getLogFilePath(taskId);
        assertEquals(expectedLogPath, logFilePath);
    }

    @Test
    public void testGetKeyStorePath() {
        String expectedKeyStorePath = "storeDir" + File.separator + ".bigtop-manager" + File.separator + "keys";

        mockedSystemUtils.when(SystemUtils::getUserHome).thenReturn(new File("storeDir"));

        String keyStorePath = ProjectPathUtils.getKeyStorePath();
        assertEquals(expectedKeyStorePath, keyStorePath);
        Path storeDirPath = Paths.get("storeDir" + File.separator + ".bigtop-manager");
        try {
            Files.walk(storeDirPath)
                    .sorted((p1, p2) -> -p1.compareTo(p2))
                    .map(Path::toFile)
                    .forEach(File::delete);
            Files.delete(Paths.get("storeDir"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetAgentCachePath() {
        String expectedCachePath = "storeDir" + File.separator + ".bigtop-manager" + File.separator + "agent-caches";

        mockedSystemUtils.when(SystemUtils::getUserHome).thenReturn(new File("storeDir"));

        String agentCachePath = ProjectPathUtils.getAgentCachePath();
        assertEquals(expectedCachePath, agentCachePath);
        Path storeDirPath = Paths.get("storeDir" + File.separator + ".bigtop-manager");
        try {
            Files.walk(storeDirPath)
                    .sorted((p1, p2) -> -p1.compareTo(p2))
                    .map(Path::toFile)
                    .forEach(File::delete);
            Files.delete(Paths.get("storeDir"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetProjectStoreDir() {
        String storeDirPath = "userHome" + File.separator + ".bigtop-manager";
        Path storeDirPathObj = Paths.get(storeDirPath);

        // Mocking the system user home
        mockedSystemUtils.when(SystemUtils::getUserHome).thenReturn(new File("userHome"));

        // Mocking Files.exists() to return false (directory doesn't exist)
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(storeDirPathObj)).thenReturn(false);
            mockedFiles.when(() -> Files.createDirectories(storeDirPathObj)).thenReturn(storeDirPathObj);

            String projectStoreDir = ProjectPathUtils.getProjectStoreDir();
            assertEquals(storeDirPath, projectStoreDir);
        }
    }

    @Test
    public void testGetProjectStoreDirExists() {
        String storeDirPath = "userHome" + File.separator + ".bigtop-manager";
        Path storeDirPathObj = Paths.get(storeDirPath);

        // Mocking the system user home
        mockedSystemUtils.when(SystemUtils::getUserHome).thenReturn(new File("userHome"));

        // Mocking Files.exists() to return true (directory already exists)
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(storeDirPathObj)).thenReturn(true);

            String projectStoreDir = ProjectPathUtils.getProjectStoreDir();
            assertEquals(storeDirPath, projectStoreDir);
        }
    }
}
