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

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Slf4j
public class ProjectPathUtils {

    public static String getLogFilePath(Long taskId) {
        return getProjectBaseDir() + File.separator + "tasklogs" + File.separator + "task-" + taskId + ".log";
    }

    public static String getKeyStorePath() {
        return getProjectStoreDir() + File.separator + "keys";
    }

    public static String getServerStackPath() {
        return getProjectResourcesDir() + File.separator + "stacks";
    }

    public static String getServerScriptPath() {
        return getProjectResourcesDir() + File.separator + "scripts";
    }

    public static String getAgentCachePath() {
        return getProjectStoreDir() + File.separator + "agent-caches";
    }

    public static String getPromptsPath() {
        return getProjectResourcesDir() + File.separator + "prompts";
    }

    private static String getProjectResourcesDir() {
        if (Environments.isDevMode()) {
            return Objects.requireNonNull(ProjectPathUtils.class.getResource("/"))
                    .getPath();
        } else {
            File file = new File(ProjectPathUtils.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath());
            return file.getParentFile().getParentFile().getPath();
        }
    }

    private static String getProjectBaseDir() {
        if (Environments.isDevMode()) {
            return SystemUtils.getUserDir().getPath();
        } else {
            File file = new File(ProjectPathUtils.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath());
            return file.getParentFile().getParentFile().getPath();
        }
    }

    protected static String getProjectStoreDir() {
        String path = SystemUtils.getUserHome().getPath() + File.separator + ".bigtop-manager";
        Path p = Paths.get(path);
        if (!Files.exists(p)) {
            try {
                Files.createDirectories(p);
            } catch (Exception e) {
                log.error("Create directory failed: {}", path, e);
                throw new RuntimeException("Create directory failed: " + path, e);
            }
        }

        return path;
    }
}
