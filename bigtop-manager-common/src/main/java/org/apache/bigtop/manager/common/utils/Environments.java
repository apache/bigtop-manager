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

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Environments {

    /**
     * Indicates whether the application is running in dev mode.
     * In dev mode, most tasks run on agent side will be proxied and return success by default.
     * This should help developers test framework functions without depending on the existence of big data components.
     *
     * @return true if the application is running in dev mode, false otherwise
     */
    public static Boolean isDevMode() {
        String devMode = System.getenv("DEV_MODE");
        return StringUtils.isNotBlank(devMode) && devMode.equals("true");
    }

    /**
     * Get java home from system environments or properties.
     *
     * @return java home string
     */
    public static String getJavaHome() {
        // Retrieve the JAVA_HOME environment variable
        String javaHome = System.getenv("JAVA_HOME");

        // Check if JAVA_HOME is set
        if (javaHome != null) {
            return javaHome;
        } else {
            // If JAVA_HOME is not set, use the java.home system property
            log.info("JAVA_HOME is not set, using java.home system property instead.");
            return System.getProperty("java.home");
        }
    }
}
