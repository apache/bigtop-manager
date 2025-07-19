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

import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.constants.MessageConstants;
import org.apache.bigtop.manager.common.shell.ShellExecutor;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.common.utils.YamlUtils;
import org.apache.bigtop.manager.stack.core.enums.ConfigType;
import org.apache.bigtop.manager.stack.core.exception.StackException;
import org.apache.bigtop.manager.stack.core.utils.template.TemplateUtils;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Only support Linux
 */
@Slf4j
public class LinuxFileUtils {

    public static void toFile(
            ConfigType type, String filename, String owner, String group, String permissions, Object content) {
        toFile(type, filename, owner, group, permissions, content, null);
    }

    /**
     * Generate config file by ConfigType
     *
     * @param type        config file type
     * @param filename    file path
     * @param owner       owner
     * @param group       group
     * @param permissions permissions
     * @param content     content map
     * @param paramMap    paramMap
     */
    public static void toFile(
            ConfigType type,
            String filename,
            String owner,
            String group,
            String permissions,
            Object content,
            Object paramMap) {
        log.info("Generating file: [{}]", filename);
        if (type == null || StringUtils.isBlank(filename) || content == null) {
            log.error("type, filename, content must not be null");
            return;
        }

        String tmpPath = "/tmp/" + generateRandomFileName();
        switch (type) {
            case PROPERTIES, XML, ENV, CONTENT:
                TemplateUtils.map2Template(type, tmpPath, content, paramMap);
                break;
            case YAML:
                YamlUtils.writeYaml(tmpPath, content);
                break;
            case JSON:
                JsonUtils.writeToFile(tmpPath, content);
                break;
            case UNKNOWN:
                log.info("no need to write");
                break;
        }

        moveFile(tmpPath, filename);
        updateOwner(filename, owner, group, false);
        updatePermissions(filename, permissions, false);
    }

    public static void toFileByTemplate(
            String template, String filename, String owner, String group, String permissions, Object modelMap) {
        toFileByTemplate(template, filename, owner, group, permissions, modelMap, null);
    }

    /**
     * Generate file by template
     *
     * @param filename    file path
     * @param owner       owner
     * @param group       group
     * @param permissions permissions
     * @param modelMap    modelMap
     * @param template    template
     * @param paramMap    paramMap
     */
    public static void toFileByTemplate(
            String template,
            String filename,
            String owner,
            String group,
            String permissions,
            Object modelMap,
            Object paramMap) {
        log.info("Generating file: [{}]", filename);
        if (StringUtils.isBlank(filename) || modelMap == null || StringUtils.isEmpty(template)) {
            log.error("type, filename, content, template must not be null");
            return;
        }

        String tmpPath = "/tmp/" + generateRandomFileName();
        TemplateUtils.map2CustomTemplate(template, tmpPath, modelMap, paramMap);

        moveFile(tmpPath, filename);
        updateOwner(filename, owner, group, false);
        updatePermissions(filename, permissions, false);
    }

    public static String generateRandomFileName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String timestamp = dateFormat.format(new Date());

        Random random = new Random();
        int randomNumber = random.nextInt(900) + 100; // Generates a random number between 100 and 999

        return timestamp + randomNumber;
    }

    /**
     * create directories
     *
     * @param dirPath     directory path
     * @param owner       owner
     * @param group       group
     * @param permissions {@code rwxr--r--}
     * @param recursive   recursive
     */
    public static void createDirectories(
            String dirPath, String owner, String group, String permissions, boolean recursive) {
        log.info("Creating directory: [{}]", dirPath);
        if (StringUtils.isBlank(dirPath)) {
            log.error("dirPath must not be null");
            return;
        }

        Path path = Paths.get(dirPath);
        if (Files.isSymbolicLink(path)) {
            log.error("Directory is symbolic link: [{}]", dirPath);
            return;
        }

        List<String> builderParameters = new ArrayList<>();
        builderParameters.add("mkdir");
        builderParameters.add("-p");
        builderParameters.add(dirPath);

        try {
            ShellResult shellResult = sudoExecCmd(builderParameters);
            if (shellResult.getExitCode() != MessageConstants.SUCCESS_CODE) {
                log.error(shellResult.formatMessage("Failed to create directory"));
                throw new StackException(shellResult.getErrMsg());
            }
        } catch (IOException e) {
            throw new StackException(e);
        }

        updateOwner(dirPath, owner, group, recursive);
        updatePermissions(dirPath, permissions, recursive);
    }

    public static void removeDirectories(String dirPath) {
        if (StringUtils.isBlank(dirPath)) {
            log.error("dirPath must not be null");
            return;
        }

        List<String> builderParameters = new ArrayList<>();
        builderParameters.add("rm");
        builderParameters.add("-rf");
        builderParameters.add(dirPath);

        try {
            ShellResult shellResult = sudoExecCmd(builderParameters);
            if (shellResult.getExitCode() != MessageConstants.SUCCESS_CODE) {
                throw new StackException(shellResult.getErrMsg());
            }
        } catch (IOException e) {
            throw new StackException(e);
        }
    }

    public static void copyFile(String source, String dest) {
        if (StringUtils.isBlank(source) || StringUtils.isBlank(dest)) {
            log.error("source and dest must not be empty");
            return;
        }

        log.info("Copy file: [{}] to [{}]", source, dest);
        List<String> builderParameters = new ArrayList<>();
        builderParameters.add("cp");
        if (Files.exists(Path.of(source)) && Files.isDirectory(Paths.get(source))) {
            builderParameters.add("-r");
        }

        builderParameters.add(source);
        builderParameters.add(dest);

        try {
            ShellResult shellResult = sudoExecCmd(builderParameters);
            if (shellResult.getExitCode() != MessageConstants.SUCCESS_CODE) {
                throw new StackException(shellResult.getErrMsg());
            }
        } catch (IOException e) {
            throw new StackException(e);
        }
    }

    public static void moveFile(String source, String dest) {
        if (StringUtils.isBlank(source) || StringUtils.isBlank(dest)) {
            log.error("source and dest must not be empty");
            return;
        }

        List<String> builderParameters = new ArrayList<>();
        builderParameters.add("mv");
        builderParameters.add(source);
        builderParameters.add(dest);

        try {
            ShellResult shellResult = sudoExecCmd(builderParameters);
            if (shellResult.getExitCode() != MessageConstants.SUCCESS_CODE) {
                throw new StackException(shellResult.getErrMsg());
            }
        } catch (IOException e) {
            throw new StackException(e);
        }
    }

    public static String readFile(String source) {
        if (StringUtils.isBlank(source)) {
            throw new StackException("source must not be empty");
        }

        List<String> builderParameters = new ArrayList<>();
        builderParameters.add("cat");
        builderParameters.add(source);

        try {
            ShellResult shellResult = sudoExecCmd(builderParameters);
            if (shellResult.getExitCode() != MessageConstants.SUCCESS_CODE) {
                throw new StackException(shellResult.getErrMsg());
            }

            return shellResult.getOutput();
        } catch (IOException e) {
            throw new StackException(e);
        }
    }

    public static String writeFile(String source, String content) {
        if (StringUtils.isBlank(source)) {
            throw new StackException("source must not be empty");
        }

        List<String> builderParameters = new ArrayList<>();
        builderParameters.add("echo");
        builderParameters.add(content);
        builderParameters.add(">");
        builderParameters.add(source);

        try {
            ShellResult shellResult = sudoExecCmd(builderParameters);
            if (shellResult.getExitCode() != MessageConstants.SUCCESS_CODE) {
                throw new StackException(shellResult.getErrMsg());
            }

            return shellResult.getOutput();
        } catch (IOException e) {
            throw new StackException(e);
        }
    }

    /**
     * create symbolic link
     *
     * @param target target file
     * @param source source file
     */
    public static void createSymbolicLink(String target, String source) {
        if (StringUtils.isBlank(target) || StringUtils.isBlank(source)) {
            log.error("target, source must not be null");
            return;
        }

        Path targetPath = Paths.get(target);
        Path sourcePath = Paths.get(source);

        try {
            log.info("Creating symbolic link: [{}] -> [{}]", target, source);
            if (Files.exists(targetPath)) {
                if (Files.isSymbolicLink(targetPath)) {
                    Path existingSourcePath = Files.readSymbolicLink(targetPath);
                    if (!existingSourcePath.equals(sourcePath)) {
                        throw new IOException(
                                "Symbolic link already exists and points to a different source: " + existingSourcePath);
                    }
                } else {
                    throw new IOException("Target path exists and is not a symbolic link: " + target);
                }
            }

            Files.createSymbolicLink(targetPath, sourcePath);
        } catch (IOException e) {
            log.error("Error when create symbolic link", e);
            throw new StackException(e);
        }
    }

    /**
     * Update file Permissions
     *
     * @param dir         file path
     * @param permissions {@code rwxr--r--}
     * @param recursive   recursive
     */
    public static void updatePermissions(String dir, String permissions, boolean recursive) {
        if (StringUtils.isBlank(dir)) {
            log.error("dir must not be null");
            return;
        }

        permissions = StringUtils.isBlank(permissions) ? Constants.PERMISSION_644 : permissions;

        List<String> builderParameters = new ArrayList<>();
        builderParameters.add("chmod");
        if (recursive && Files.isDirectory(Paths.get(dir))) {
            builderParameters.add("-R");
        }
        builderParameters.add(permissions);
        builderParameters.add(dir);

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
     * Update file owner
     *
     * @param dir       file path
     * @param owner     owner
     * @param group     group
     * @param recursive recursive
     */
    public static void updateOwner(String dir, String owner, String group, boolean recursive) {
        if (StringUtils.isBlank(dir)) {
            log.error("dir must not be null");
            return;
        }

        owner = StringUtils.isBlank(owner) ? "root" : owner;
        group = StringUtils.isBlank(group) ? "root" : group;

        List<String> builderParameters = new ArrayList<>();

        builderParameters.add("chown");
        if (recursive && Files.isDirectory(Paths.get(dir))) {
            builderParameters.add("-R");
        }
        builderParameters.add(owner + ":" + group);
        builderParameters.add(dir);

        try {
            ShellResult shellResult = sudoExecCmd(builderParameters);
            if (shellResult.getExitCode() != MessageConstants.SUCCESS_CODE) {
                throw new StackException(shellResult.getErrMsg());
            }
        } catch (IOException e) {
            throw new StackException(e);
        }
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
