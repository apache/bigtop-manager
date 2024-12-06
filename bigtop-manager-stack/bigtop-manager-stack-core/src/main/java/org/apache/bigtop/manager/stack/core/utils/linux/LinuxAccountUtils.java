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

import org.apache.bigtop.manager.common.constants.MessageConstants;
import org.apache.bigtop.manager.common.shell.ShellExecutor;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.common.utils.FileUtils;
import org.apache.bigtop.manager.stack.core.exception.StackException;

import org.apache.commons.lang3.StringUtils;

import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
public class LinuxAccountUtils {

    private static final String GROUPADD = "/usr/sbin/groupadd";
    private static final String GROUPDEL = "/usr/sbin/groupdel";
    private static final String GROUPMOD = "/usr/sbin/groupmod";

    private static final String USERADD = "/usr/sbin/useradd";
    private static final String USERDEL = "/usr/sbin/userdel";
    private static final String USERMOD = "/usr/sbin/usermod";

    /**
     * Delete user
     *
     * @param user User Name
     */
    public static void userDel(String user) {
        Objects.requireNonNull(user);

        List<String> builderParameters = new ArrayList<>();

        if (isUserExists(user)) {
            builderParameters.add(USERDEL);
        } else {
            return;
        }

        builderParameters.add(user);

        try {
            ShellResult shellResult = sudoExecCmd(builderParameters);
            if (shellResult.getExitCode() != MessageConstants.SUCCESS_CODE) {
                throw new StackException(shellResult.getErrMsg());
            }
        } catch (IOException e) {
            throw new StackException(e);
        }
    }

    /**
     * Add user
     * useradd [options] LOGIN
     */
    public static void userAdd(String user, String group) {
        userAdd(user, group, null, null, null, null, null, false);
    }

    /**
     * Add user
     * useradd [options] LOGIN
     *
     * @param user     User Name
     * @param group    Primary user group
     * @param uid      user id
     * @param groups   group list
     * @param home     user home directory
     * @param comment  user comment
     * @param password user password
     */
    public static void userAdd(
            String user,
            String group,
            String uid,
            Collection<String> groups,
            String home,
            String comment,
            String password,
            Boolean system) {
        Objects.requireNonNull(user);
        Objects.requireNonNull(group);

        if (!isGroupExists(group)) {
            groupAdd(group);
        }

        if (!CollectionUtils.isEmpty(groups)) {
            for (String g : groups) {
                groupAdd(g);
            }
        }

        List<String> builderParameters = new ArrayList<>();

        if (isUserExists(user)) {
            builderParameters.add(USERMOD);
        } else {
            builderParameters.add(USERADD);
            builderParameters.add("-m");
        }

        if (StringUtils.isNoneBlank(home)) {
            builderParameters.add("-d");
            builderParameters.add(home);
        }
        if (StringUtils.isNoneBlank(password)) {
            builderParameters.add("-p");
            builderParameters.add(password);
        }
        if (StringUtils.isNoneBlank(comment)) {
            builderParameters.add("-c");
            builderParameters.add(comment);
        }
        if (system) {
            builderParameters.add("--system");
        }
        if (StringUtils.isNoneBlank(uid)) {
            builderParameters.add("-u");
            builderParameters.add(uid);
        }

        builderParameters.add("-g");
        builderParameters.add(group);

        if (!CollectionUtils.isEmpty(groups)) {
            builderParameters.add("-G");
            builderParameters.add(String.join(",", groups));
        }
        builderParameters.add(user);

        try {
            ShellResult shellResult = sudoExecCmd(builderParameters);
            if (shellResult.getExitCode() != MessageConstants.SUCCESS_CODE) {
                throw new StackException(shellResult.getErrMsg());
            }
        } catch (IOException e) {
            throw new StackException(e);
        }
    }

    /**
     * Delete group
     *
     * @param group Group Name
     */
    public static void groupDel(String group) {
        Objects.requireNonNull(group);

        List<String> builderParameters = new ArrayList<>();

        if (isGroupExists(group)) {
            builderParameters.add(GROUPDEL);
        } else {
            return;
        }

        builderParameters.add(group);

        try {
            ShellResult shellResult = sudoExecCmd(builderParameters);
            if (shellResult.getExitCode() != MessageConstants.SUCCESS_CODE) {
                throw new StackException(shellResult.getErrMsg());
            }
        } catch (IOException e) {
            throw new StackException(e);
        }
    }

    /**
     * Add group
     *
     * @param group Group Name
     */
    public static void groupAdd(String group) {
        groupAdd(group, null, null);
    }

    /**
     * Add group
     * {@code groupadd [-g gid] [-p password] GROUP}
     * or
     * {@code groupmod [-g gid] [-p password] GROUP}
     *
     * @param group    Group Name
     * @param gid      groupId
     * @param password password
     */
    public static void groupAdd(String group, String gid, String password) {
        Objects.requireNonNull(group);

        List<String> builderParameters = new ArrayList<>();

        if (isGroupExists(group)) {
            builderParameters.add(GROUPMOD);
        } else {
            builderParameters.add(GROUPADD);
        }

        if (StringUtils.isNoneBlank(gid)) {
            builderParameters.add("-g");
            builderParameters.add(gid);
        }
        if (StringUtils.isNoneBlank(password)) {
            builderParameters.add("-p");
            builderParameters.add(password);
        }

        builderParameters.add(group);

        try {
            ShellResult shellResult = sudoExecCmd(builderParameters);
            if (shellResult.getExitCode() != MessageConstants.SUCCESS_CODE) {
                throw new StackException(shellResult.getErrMsg());
            }
        } catch (IOException e) {
            throw new StackException(e);
        }
    }

    /**
     * Check if exists group
     *
     * @param group Group Name
     * @return
     */
    public static boolean isGroupExists(String group) {
        Objects.requireNonNull(group);

        List<String> builderParameters = new ArrayList<>();

        builderParameters.add("sh");
        builderParameters.add("-c");
        builderParameters.add("awk -F':' '{print $1}' /etc/group | grep  " + group);

        try {
            ShellResult output = sudoExecCmd(builderParameters);
            return output.getExitCode() == 0;
        } catch (IOException e) {
            throw new StackException(e);
        }
    }

    /**
     * Check if exists user
     *
     * @param user User Name
     */
    public static boolean isUserExists(String user) {
        Objects.requireNonNull(user);

        List<String> builderParameters = new ArrayList<>();

        builderParameters.add("sh");
        builderParameters.add("-c");
        builderParameters.add("awk -F':' '{print $1}' /etc/passwd | grep  " + user);

        try {
            ShellResult output = sudoExecCmd(builderParameters);
            return output.getExitCode() == 0;
        } catch (IOException e) {
            throw new StackException(e);
        }
    }

    /**
     * Get user and it's primary group
     *
     * @param user username
     * @return primary group, if user not exists, return null
     */
    public static String getUserPrimaryGroup(String user) {
        if (!isUserExists(user)) {
            return null;
        }

        // Search for user's primary group id
        int groupId = 0;
        Stream<String> lines = FileUtils.readFile2Str(new File("/etc/passwd")).lines();
        for (String line : lines.toList()) {
            String[] split = line.split(":");
            if (split[0].equals(user)) {
                groupId = Integer.parseInt(split[3]);
            }
        }

        if (groupId == 0) {
            return null;
        }

        // Search for group name
        lines = FileUtils.readFile2Str(new File("/etc/group")).lines();
        for (String line : lines.toList()) {
            String[] split = line.split(":");
            if (Integer.parseInt(split[2]) == groupId) {
                return split[0];
            }
        }

        return null;
    }

    private static ShellResult sudoExecCmd(List<String> params) throws IOException {
        if ("root".equals(System.getProperty("user.name"))) {
            return ShellExecutor.execCommand(params);
        } else {
            List<String> sudoParams = new ArrayList<>();
            sudoParams.add("sudo");
            sudoParams.addAll(params);

            return ShellExecutor.execCommand(sudoParams);
        }
    }
}
