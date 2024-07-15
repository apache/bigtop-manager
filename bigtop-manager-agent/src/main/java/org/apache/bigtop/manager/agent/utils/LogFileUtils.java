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
package org.apache.bigtop.manager.agent.utils;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;

public class LogFileUtils {

    public static String getLogFilePath(Long taskId) {
        String baseDir;
        if (SystemUtils.IS_OS_WINDOWS) {
            baseDir = SystemUtils.getUserDir().getPath();
        } else {
            File file = new File(LogFileUtils.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath());
            baseDir = file.getParentFile().getParentFile().getPath();
        }

        return baseDir + File.separator + "tasklogs" + File.separator + "task-" + taskId + ".log";
    }
}
