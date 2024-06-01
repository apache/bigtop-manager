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

import org.apache.bigtop.manager.common.enums.OSArchType;
import org.apache.bigtop.manager.common.enums.OSType;
import org.apache.bigtop.manager.common.shell.ShellExecutor;
import org.apache.bigtop.manager.common.shell.ShellResult;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

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
            String os = getOSType().toLowerCase() + getOSVersion().toLowerCase();
            ifSupportedOS(os);
            return os;
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

    public static String getArch() {
        if (SystemUtils.IS_OS_LINUX) {
            return getOSArch();
        } else {
            return System.getProperty("os.arch");
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

        String osType = regexOS(ID_PATTERN, output);

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
        List<String> builderParameters = new ArrayList<>();
        builderParameters.add("arch");

        try {
            ShellResult shellResult = ShellExecutor.execCommand(builderParameters);
            String output = shellResult.getOutput().replace("\n", "");
            log.debug("getArch: {}", output);

            ifSupportedArch(output);
            return output;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void ifSupportedOS(String os) {
        if (!EnumUtils.isValidEnumIgnoreCase(OSType.class, os)) {
            throw new RuntimeException("Unsupported OS: [" + os + "]");
        }
        log.debug("OS [{}] is Supported", os);
    }

    private static void ifSupportedArch(String arch) {
        if (!EnumUtils.isValidEnumIgnoreCase(OSArchType.class, arch)) {
            throw new RuntimeException("Unsupported Arch: [" + arch + "]");
        }
        log.debug("Arch [{}] is Supported", arch);
    }
}
