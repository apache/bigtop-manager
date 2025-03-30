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
package org.apache.bigtop.manager.common.utils.os;

import org.apache.bigtop.manager.common.shell.ShellExecutor;
import org.apache.bigtop.manager.common.shell.ShellResult;

import org.apache.commons.lang3.SystemUtils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used for detecting the type and architecture of the operating system.
 * <p />
 * Our agent should run on certain specific operating systems and architectures,
 * which are defined in the OSType and OSArchType enums.
 * <p>
 * However, for convenience in development and testing, we also support running the agent on other operating systems,
 * which is referred to as "develop mode".
 * <p>
 * In "develop mode", we do not check whether the operating system and architecture are in the supported list.
 */
@Slf4j
public class OSDetection {

    private static final Pattern ID_PATTERN = Pattern.compile("ID=\"?(\\w+)\"?");

    private static final Pattern VERSION_PATTERN = Pattern.compile("VERSION_ID=\"?(\\d+)([.\\d]*)\"?");

    public static String getOS() {
        if (SystemUtils.IS_OS_LINUX) {
            return getOSType() + getOSVersion().toLowerCase();
        } else {
            return System.getProperty("os.name");
        }
    }

    public static String getVersion() {
        if (SystemUtils.IS_OS_LINUX) {
            return getOSVersion();
        } else {
            return System.getProperty("os.version");
        }
    }

    private static boolean isRunningInContainer() {
        try {
            File cgroupFile = new File("/proc/self/cgroup");
            if (cgroupFile.exists()) {
                String content = new String(java.nio.file.Files.readAllBytes(cgroupFile.toPath()));
                if (content.contains("docker") || content.contains("kubepods")) {
                    return true;
                }
            }

            // Check for cgroup v2
            File cgroup2File = new File("/proc/1/cgroup");
            if (cgroup2File.exists()) {
                String content = new String(java.nio.file.Files.readAllBytes(cgroup2File.toPath()));
                if (content.contains("docker") || content.contains("kubepods")) {
                    return true;
                }
            }

            // Additional check for cgroup v2 controllers
            File cgroupControllersFile = new File("/sys/fs/cgroup/cgroup.controllers");
            if (cgroupControllersFile.exists()) {
                String content = new String(java.nio.file.Files.readAllBytes(cgroupControllersFile.toPath()));
                // These are common controllers used in containers
                return content.contains("cpuset") && content.contains("memory");
            }

            // Check for container environment variable
            File environFile = new File("/proc/1/environ");
            if (environFile.exists()) {
                String content = new String(java.nio.file.Files.readAllBytes(environFile.toPath()));
                return content.contains("container=docker");
            }

            return false;
        } catch (IOException e) {
            log.warn("Failed to detect container environment: {}", e.getMessage(), e);
            return false;
        }
    }

    public static String getArch() {
        if (isRunningInContainer()) {
            log.debug("Running in containerized environment, using fallback architecture detection");
            return System.getProperty("os.arch");
        }

        if (SystemUtils.IS_OS_LINUX) {
            try {
                String arch = getOSArch();
                log.debug("Detected Linux architecture: {}", arch);
                return arch;
            } catch (Exception e) {
                log.warn("Failed to get OS architecture using 'arch' command, falling back to os.arch", e);
                return System.getProperty("os.arch");
            }
        } else {
            String arch = System.getProperty("os.arch").toLowerCase();
            log.debug("Detected non-Linux architecture: {}", arch);
            // Standardize architecture names for consistency
            String standardizedArch = standardizeArch(arch);
            log.debug("Standardized architecture: {}", standardizedArch);
            return standardizedArch;
        }
    }

    /**
     * get disk usage
     *
     * @return disk free size, unit: B
     */
    public static long freeDisk() {
        File file = new File(".");
        return file.getFreeSpace();
    }

    /**
     * get total disk
     *
     * @return disk total size, unit: B
     */
    public static long totalDisk() {
        File file = new File(".");
        return file.getTotalSpace();
    }

    private static String getOSType() {
        String output = getOSRelease();

        String osType = regexOS(ID_PATTERN, output).toLowerCase();

        log.debug("osType: {}", osType);
        return osType;
    }

    private static String getOSVersion() {
        String output = getOSRelease();

        String osVersion = regexOS(VERSION_PATTERN, output);

        log.debug("osVersion: {}", osVersion);
        return osVersion;
    }

    private static String getOSRelease() {
        List<String> builderParameters = new ArrayList<>();
        builderParameters.add("cat");
        builderParameters.add("/etc/os-release");

        try {
            ShellResult shellResult = ShellExecutor.execCommand(builderParameters);
            String output = shellResult.getOutput();

            log.debug("getOSRelease: {}", output);
            return output;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String regexOS(Pattern pattern, String content) {
        for (String line : content.split("\\n", -1)) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }
        throw new RuntimeException("Unable to find OS: " + content);
    }

    private static String getOSArch() {
        try {
            List<String> builderParameters = new ArrayList<>();
            builderParameters.add("arch");
            ShellResult shellResult = ShellExecutor.execCommand(builderParameters);
            String output = shellResult.getOutput().replace("\n", "");
            log.debug("getArch: {}", output);
            return output;
        } catch (Exception e) {
            log.warn("Failed to execute 'arch' command, falling back to /proc/cpuinfo");
            try {
                String cpuInfo = new String(java.nio.file.Files.readAllBytes(new File("/proc/cpuinfo").toPath()));
                Pattern pattern = Pattern.compile("model name\\s*:.*?(\\w+)$", Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(cpuInfo);
                if (matcher.find()) {
                    String output = matcher.group(1).toLowerCase();
                    log.debug("getArch: {}", output);
                    return output;
                }
            } catch (IOException ex) {
                log.error("Failed to read /proc/cpuinfo: {}", ex.getMessage(), ex);
            }
            throw new RuntimeException("Unable to detect OS architecture");
        }
    }

    private static String standardizeArch(String arch) {
        if ("amd64".equals(arch)) {
            return "x86_64";
        } else if ("aarch64".equals(arch)) {
            return "arm64";
        } else if ("x86".equals(arch)) {
            return "i386";
        } else if ("arm".equals(arch)) {
            return "armv7l";
        } else if ("ppc64le".equals(arch)) {
            return "ppc64le";
        } else if ("s390x".equals(arch)) {
            return "s390x";
        } else if ("riscv64".equals(arch)) {
            return "riscv64";
        } else if ("mips".equals(arch)) {
            return "mips";
        } else if ("mips64".equals(arch)) {
            return "mips64";
        }
        log.warn("Detected unknown architecture: {}", arch);
        throw new UnsupportedOperationException("Unsupported architecture: " + arch);
    }
}
