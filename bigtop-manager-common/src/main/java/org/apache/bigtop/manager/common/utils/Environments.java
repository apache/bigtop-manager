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

public class Environments {

    /**
     * Indicates whether the application is running in development mode, which is disabled by default.
     * In development mode, only NOP stacks are available and no real shell commands will be executed on the agent side.
     */
    public static Boolean isDevMode() {
        String devMode = System.getenv("DEV_MODE");
        return StringUtils.isNotBlank(devMode) && devMode.equals("true");
    }
}
