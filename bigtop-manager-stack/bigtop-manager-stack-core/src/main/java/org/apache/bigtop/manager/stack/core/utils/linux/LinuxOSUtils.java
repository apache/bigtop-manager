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
package org.apache.bigtop.manager.stack.core.utils.linux;

import org.apache.bigtop.manager.common.shell.ShellExecutor;
import org.apache.bigtop.manager.common.shell.ShellResult;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static org.apache.bigtop.manager.common.constants.Constants.ROOT_USER;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LinuxOSUtils {

    /**
     * Execute command with root user
     *
     * @param command command
     * @return result of execute command
     * @throws IOException errors
     */
    public static ShellResult sudoExecCmd(String command) throws IOException {
        return sudoExecCmd(command, null);
    }

    /**
     * Execute the sudo command
     *
     * @param command command
     * @param tenant  Tenant User
     * @return result of execute command
     * @throws IOException errors
     */
    public static ShellResult sudoExecCmd(String command, String tenant) throws IOException {
        return execCmd(command, getTenant(tenant));
    }

    /**
     * get sudo command
     *
     * @param tenant Tenant User
     * @return result of sudo execute command
     */
    public static String getTenant(String tenant) {
        if (StringUtils.isBlank(tenant) || !LinuxAccountUtils.isUserExists(tenant)) {
            return ROOT_USER;
        }

        return tenant;
    }

    /**
     * support sudo command
     */
    public static ShellResult execCmd(String command, String tenant) throws IOException {
        List<String> builderParameters = new ArrayList<>();
        builderParameters.add("sudo");
        builderParameters.add("-u");
        builderParameters.add(tenant);
        builderParameters.add("sh");
        builderParameters.add("-c");
        builderParameters.add(command);
        log.info("Running command: [{}], user: [{}]", command, tenant);
        return ShellExecutor.execCommand(builderParameters);
    }

    /**
     * Execute the corresponding command of Linux or Windows
     *
     * @param command command
     * @return result of execute command
     * @throws IOException errors
     */
    public static ShellResult execCmd(String command) throws IOException {
        StringTokenizer st = new StringTokenizer(command);
        List<String> builderParameters = new ArrayList<>();
        while (st.hasMoreTokens()) {
            builderParameters.add(st.nextToken());
        }
        return ShellExecutor.execCommand(builderParameters);
    }

    public static ShellResult checkProcess(String filepath) {
        File file = new File(filepath);
        if (!file.exists() || !file.isFile()) {
            log.warn("Pid file {} is empty or does not exist", filepath);
            return new ShellResult(-1, "", "Component is not running");
        }
        int pid;
        try {
            pid = Integer.parseInt(LinuxFileUtils.readFile(filepath).replaceAll("\r|\n", ""));
        } catch (Exception e) {
            log.warn("Pid file {} does not exist or does not contain a process id number, error", filepath, e);
            return new ShellResult(-1, "", "Component is not running");
        }
        try {
            return execCmd("sudo kill -0 " + pid);
        } catch (IOException e) {
            log.warn("Process with pid {} is not running. Stale pid file at {}, error", pid, filepath, e);
            return new ShellResult(-1, "", "Component is not running");
        }
    }
}
