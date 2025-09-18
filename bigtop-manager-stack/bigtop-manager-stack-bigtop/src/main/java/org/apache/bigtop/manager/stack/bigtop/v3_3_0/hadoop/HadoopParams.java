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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.hadoop;

import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.grpc.payload.ComponentCommandPayload;
import org.apache.bigtop.manager.stack.bigtop.param.BigtopParams;
import org.apache.bigtop.manager.stack.core.annotations.GlobalParams;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxOSUtils;

import org.apache.commons.lang3.StringUtils;

import com.google.auto.service.AutoService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
@Slf4j
@AutoService(Params.class)
@NoArgsConstructor
public class HadoopParams extends BigtopParams {

    private final String hadoopLogDir = "/var/log/hadoop";
    private final String hadoopPidDir = "/var/run/hadoop";

    // hadoop-${user}-${component}.pid
    private final String nameNodePidFile = hadoopPidDir + "/hadoop-hadoop-namenode.pid";
    private final String dataNodePidFile = hadoopPidDir + "/hadoop-hadoop-datanode.pid";
    private final String sNameNodePidFile = hadoopPidDir + "/hadoop-hadoop-secondarynamenode.pid";
    private final String journalNodePidFile = hadoopPidDir + "/hadoop-hadoop-journalnode.pid";
    private final String zkfcPidFile = hadoopPidDir + "/hadoop-hadoop-zkfc.pid";
    private final String resourceManagerPidFile = hadoopPidDir + "/hadoop-hadoop-resourcemanager.pid";
    private final String nodeManagerPidFile = hadoopPidDir + "/hadoop-hadoop-nodemanager.pid";
    private final String historyServerPidFile = hadoopPidDir + "/hadoop-hadoop-historyserver.pid";

    private String dfsDataDir;
    private String dfsNameNodeDir;
    private String dfsNameNodeCheckPointDir;
    private String dfsDomainSocketPathPrefix;

    private String nodeManagerLogDir = "/hadoop/yarn/log";
    private String nodeManagerLocalDir = "/hadoop/yarn/local";

    private List<String> nameNodeFormattedDirs;

    public HadoopParams(ComponentCommandPayload componentCommandPayload) {
        super(componentCommandPayload);
        globalParamsMap.put("hdfs_user", user());
        globalParamsMap.put("hdfs_group", group());
        globalParamsMap.put("datanode_hosts", LocalSettings.componentHosts("datanode"));
        globalParamsMap.put("java_home", javaHome());
        globalParamsMap.put("hadoop_home", serviceHome());
        globalParamsMap.put("hadoop_conf_dir", confDir());
        globalParamsMap.put("hadoop_libexec_dir", serviceHome() + "/libexec");
        globalParamsMap.put("exclude_hosts", new ArrayList<>());
    }

    public String hadoopLimits() {
        Map<String, Object> hadoopConf = LocalSettings.configurations(getServiceName(), "hadoop.conf");
        return (String) hadoopConf.get("content");
    }

    public String workers() {
        Map<String, Object> hdfsConf = LocalSettings.configurations(getServiceName(), "workers");
        return (String) hdfsConf.get("content");
    }

    @GlobalParams
    public Map<String, Object> hdfsLog4j() {
        return LocalSettings.configurations(getServiceName(), "hdfs-log4j");
    }

    @GlobalParams
    public Map<String, Object> coreSite() {
        Map<String, Object> coreSite = LocalSettings.configurations(getServiceName(), "core-site");
        List<String> namenodeList = LocalSettings.componentHosts("namenode");
        if (!namenodeList.isEmpty()) {
            coreSite.put(
                    "fs.defaultFS", ((String) coreSite.get("fs.defaultFS")).replace("localhost", namenodeList.get(0)));
        }
        return coreSite;
    }

    @GlobalParams
    public Map<String, Object> hadoopPolicy() {
        return LocalSettings.configurations(getServiceName(), "hadoop-policy");
    }

    @GlobalParams
    public Map<String, Object> hdfsSite() {
        Map<String, Object> hdfsSite = LocalSettings.configurations(getServiceName(), "hdfs-site");
        List<String> namenodeList = LocalSettings.componentHosts("namenode");
        if (!namenodeList.isEmpty()) {
            hdfsSite.put(
                    "dfs.namenode.rpc-address",
                    ((String) hdfsSite.get("dfs.namenode.rpc-address")).replace("0.0.0.0", namenodeList.get(0)));
            hdfsSite.put(
                    "dfs.datanode.https.address",
                    ((String) hdfsSite.get("dfs.datanode.https.address")).replace("0.0.0.0", namenodeList.get(0)));
            hdfsSite.put(
                    "dfs.namenode.https-address",
                    ((String) hdfsSite.get("dfs.namenode.https-address")).replace("0.0.0.0", namenodeList.get(0)));
        }

        // Configure native library dependent settings
        configureNativeLibraryDependentSettings(hdfsSite);

        dfsDataDir = (String) hdfsSite.get("dfs.datanode.data.dir");
        dfsNameNodeDir = (String) hdfsSite.get("dfs.namenode.name.dir");
        nameNodeFormattedDirs = Arrays.stream(dfsNameNodeDir.split(","))
                .map(x -> x + "/namenode-formatted/")
                .toList();

        String dfsDomainSocketPath = (String) hdfsSite.get("dfs.domain.socket.path");
        if (StringUtils.isNotBlank(dfsDomainSocketPath)) {
            dfsDomainSocketPathPrefix = dfsDomainSocketPath.replace("dn._PORT", "");
        }
        dfsNameNodeCheckPointDir = (String) hdfsSite.get("dfs.namenode.checkpoint.dir");
        return hdfsSite;
    }

    @GlobalParams
    public Map<String, Object> yarnLog4j() {
        return LocalSettings.configurations(getServiceName(), "yarn-log4j");
    }

    @GlobalParams
    public Map<String, Object> yarnSite() {
        Map<String, Object> yarnSite = LocalSettings.configurations(getServiceName(), "yarn-site");
        List<String> resourcemanagerList = LocalSettings.componentHosts("resourcemanager");
        if (!resourcemanagerList.isEmpty()) {
            yarnSite.put("yarn.resourcemanager.hostname", MessageFormat.format("{0}", resourcemanagerList.get(0)));
            yarnSite.put(
                    "yarn.resourcemanager.resource-tracker.address",
                    ((String) yarnSite.get("yarn.resourcemanager.resource-tracker.address"))
                            .replace("0.0.0.0", resourcemanagerList.get(0)));
            yarnSite.put(
                    "yarn.resourcemanager.scheduler.address",
                    ((String) yarnSite.get("yarn.resourcemanager.scheduler.address"))
                            .replace("0.0.0.0", resourcemanagerList.get(0)));
            yarnSite.put(
                    "yarn.resourcemanager.address",
                    ((String) yarnSite.get("yarn.resourcemanager.address"))
                            .replace("0.0.0.0", resourcemanagerList.get(0)));
            yarnSite.put(
                    "yarn.resourcemanager.admin.address",
                    ((String) yarnSite.get("yarn.resourcemanager.admin.address"))
                            .replace("0.0.0.0", resourcemanagerList.get(0)));
            yarnSite.put(
                    "yarn.resourcemanager.webapp.address",
                    ((String) yarnSite.get("yarn.resourcemanager.webapp.address"))
                            .replace("0.0.0.0", resourcemanagerList.get(0)));
            yarnSite.put(
                    "yarn.resourcemanager.webapp.https.address",
                    ((String) yarnSite.get("yarn.resourcemanager.webapp.https.address"))
                            .replace("0.0.0.0", resourcemanagerList.get(0)));
        }

        nodeManagerLogDir = (String) yarnSite.get("yarn.nodemanager.log-dirs");
        nodeManagerLocalDir = (String) yarnSite.get("yarn.nodemanager.local-dirs");
        return yarnSite;
    }

    @GlobalParams
    public Map<String, Object> mapredSite() {
        return LocalSettings.configurations(getServiceName(), "mapred-site");
    }

    @GlobalParams
    public Map<String, Object> hadoopEnv() {
        Map<String, Object> configurations = LocalSettings.configurations(getServiceName(), "hadoop-env");
        configurations.put("hadoop_log_dir", hadoopLogDir);
        configurations.put("hadoop_pid_dir", hadoopPidDir);
        return configurations;
    }

    @GlobalParams
    public Map<String, Object> yarnEnv() {
        return LocalSettings.configurations(getServiceName(), "yarn-env");
    }

    @GlobalParams
    public Map<String, Object> mapredEnv() {
        return LocalSettings.configurations(getServiceName(), "mapred-env");
    }

    @Override
    public String confDir() {
        return serviceHome() + "/etc/hadoop";
    }

    public String binDir() {
        return serviceHome() + "/bin";
    }

    @Override
    public String getServiceName() {
        return "hadoop";
    }

    /**
     * Configure native library dependent settings for HDFS.
     * This method intelligently detects libhadoop native library availability
     * and automatically configures short-circuit reads and UNIX domain socket settings.
     * <p>
     * Short-circuit read optimization explanation:
     * - When client and DataNode are on the same node, network layer can be bypassed
     * to read local data blocks directly
     * - Requires glibc version >= 2.34 to ensure native library compatibility
     * - Uses UNIX domain sockets for inter-process communication to improve performance
     *
     * @param hdfsSite The HDFS site configuration map to be modified
     */
    private void configureNativeLibraryDependentSettings(Map<String, Object> hdfsSite) {
        try {
            // Detect system glibc version to determine native library support
            boolean enableShortCircuit = isGlibcVersionCompatible();
            String domainSocketPath = null;

            if (enableShortCircuit) {
                log.info("Detected glibc version >= 2.34, enabling short-circuit read optimization");

                // Get recommended domain socket path and append port placeholder
                domainSocketPath = (String) hdfsSite.get("dfs.domain.socket.path");
                if (domainSocketPath != null) {
                    // _PORT placeholder will be replaced with actual port number by DataNode at runtime
                    if (!domainSocketPath.endsWith("dn._PORT")) {
                        domainSocketPath = domainSocketPath + "/dn._PORT";
                    }
                    log.info("Enabling short-circuit reads with domain socket path: {}", domainSocketPath);
                }
            } else {
                log.info("glibc version < 2.34 or detection failed, disabling short-circuit reads for compatibility");
            }

            // Apply short-circuit read configuration
            applyShortCircuitConfiguration(hdfsSite, enableShortCircuit, domainSocketPath);

        } catch (Exception e) {
            log.error("Error occurred during glibc version detection, disabling short-circuit reads for safety", e);
            applyShortCircuitConfiguration(hdfsSite, false, null);
        }
    }

    /**
     * Check if glibc version is >= 2.34 to determine native library support.
     * <p>
     * Detection logic:
     * 1. First attempt to use 'ldd --version' command to get glibc version
     * 2. If failed, try 'getconf GNU_LIBC_VERSION' as fallback method
     * 3. Parse version number and compare with minimum required version (2.34)
     *
     * @return true if glibc version >= 2.34, false otherwise
     */
    private boolean isGlibcVersionCompatible() {
        try {
            // Method 1: Use ldd command to detect glibc version
            ShellResult result = LinuxOSUtils.execCmd("ldd --version");
            if (result.getExitCode() == 0) {
                String output = result.getOutput();
                String[] lines = output.split("\n");
                for (String line : lines) {
                    // Look for lines containing glibc version information
                    if (line.contains("GNU libc") || line.contains("GLIBC")) {
                        String version = extractGlibcVersionFromLine(line);
                        if (version != null) {
                            boolean supported = compareVersionStrings(version, "2.34") >= 0;
                            log.info("Detected glibc version via ldd: {}, supported: {}", version, supported);
                            return supported;
                        }
                    }
                }
            } else {
                log.info("ldd --version command failed with exit code: {}", result.getExitCode());
            }

            // Method 2: Try getconf as fallback detection method
            return detectGlibcVersionViaGetconf();

        } catch (Exception e) {
            log.info("Exception during glibc version detection: {}", e.getMessage());
            return detectGlibcVersionViaGetconf();
        }
    }

    /**
     * Alternative method using getconf command to detect glibc version.
     *
     * @return true if detected version >= 2.34, false otherwise
     */
    private boolean detectGlibcVersionViaGetconf() {
        try {
            ShellResult result = LinuxOSUtils.execCmd("getconf GNU_LIBC_VERSION");
            if (result.getExitCode() == 0) {
                String output = result.getOutput().trim();
                if (output.startsWith("glibc ")) {
                    String version = output.substring(6).trim();
                    boolean supported = compareVersionStrings(version, "2.34") >= 0;
                    log.info("Detected glibc version via getconf: {}, supported: {}", version, supported);
                    return supported;
                }
            }
        } catch (Exception e) {
            log.info("getconf method detection failed: {}", e.getMessage());
        }

        // Default to false for safety
        log.warn("Could not determine glibc version, defaulting to disable short-circuit reads");
        return false;
    }

    /**
     * Extract glibc version number from ldd output line.
     * <p>
     * Supported format examples:
     * - "ldd (GNU libc) 2.35"
     * - "ldd (Ubuntu GLIBC 2.35-0ubuntu3.1) 2.35"
     * - "ldd (GNU libc) 2.34"
     *
     * @param line Single line of text from ldd command output
     * @return Extracted version string like "2.35", or null if extraction failed
     */
    private String extractGlibcVersionFromLine(String line) {
        // Split line by whitespace and look for version pattern
        String[] parts = line.split("\\s+");
        for (String part : parts) {
            // Match version pattern like "2.35"
            if (part.matches("\\d+\\.\\d+.*")) {
                // Extract major.minor version numbers
                String cleanVersion = part.replaceAll("[^\\d.]", "");
                // Ensure only major and minor versions are kept
                String[] versionParts = cleanVersion.split("\\.");
                if (versionParts.length >= 2) {
                    return versionParts[0] + "." + versionParts[1];
                }
                return cleanVersion;
            }
        }
        return null;
    }

    /**
     * Compare two version strings (major.minor format).
     *
     * @param v1 First version string
     * @param v2 Second version string
     * @return negative if v1 < v2, zero if equal, positive if v1 > v2
     */
    private int compareVersionStrings(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int major1 = Integer.parseInt(parts1[0]);
        int minor1 = parts1.length > 1 ? Integer.parseInt(parts1[1]) : 0;

        int major2 = Integer.parseInt(parts2[0]);
        int minor2 = parts2.length > 1 ? Integer.parseInt(parts2[1]) : 0;

        // Compare major version first
        if (major1 != major2) {
            return major1 - major2;
        }
        // Compare minor version when major versions are equal
        return minor1 - minor2;
    }

    /**
     * Apply short-circuit read settings in HDFS site configuration.
     * <p>
     * Configuration properties explanation:
     * - dfs.client.read.shortcircuit: Whether to enable short-circuit reads
     * - dfs.domain.socket.path: UNIX domain socket path
     * - dfs.client.read.shortcircuit.streams.cache.size: Short-circuit read stream cache size
     *
     * @param hdfsSite           HDFS site configuration map
     * @param enableShortCircuit Whether to enable short-circuit reads
     * @param domainSocketPath   Domain socket path (null to disable domain socket)
     */
    private void applyShortCircuitConfiguration(
            Map<String, Object> hdfsSite, boolean enableShortCircuit, String domainSocketPath) {

        // Configure short-circuit read main switch
        hdfsSite.put("dfs.client.read.shortcircuit", String.valueOf(enableShortCircuit));

        if (enableShortCircuit && domainSocketPath != null) {
            // Enable UNIX domain socket for high-performance short-circuit reads
            hdfsSite.put("dfs.domain.socket.path", domainSocketPath);
            log.info("Short-circuit reads enabled with domain socket path: {}", domainSocketPath);
        } else {
            // Remove domain socket path configuration to prevent DataNode startup failures
            // This avoids startup errors due to libhadoop loading issues
            hdfsSite.remove("dfs.domain.socket.path");
            if (enableShortCircuit) {
                log.info("Short-circuit reads enabled (fallback mode, without domain socket)");
            } else {
                log.info("Short-circuit reads disabled");
            }
        }

        // Configure stream cache based on short-circuit read status
        configureShortCircuitStreamCache(hdfsSite, enableShortCircuit);
    }

    /**
     * Configure short-circuit read stream cache settings.
     *
     * @param hdfsSite           HDFS site configuration map
     * @param enableShortCircuit Whether short-circuit reads are enabled
     */
    private void configureShortCircuitStreamCache(Map<String, Object> hdfsSite, boolean enableShortCircuit) {
        if (enableShortCircuit) {
            // Optimize cache size when short-circuit reads are enabled for better performance
            Object currentCacheSize = hdfsSite.get("dfs.client.read.shortcircuit.streams.cache.size");
            if (currentCacheSize == null || "0".equals(currentCacheSize.toString())) {
                hdfsSite.put("dfs.client.read.shortcircuit.streams.cache.size", "4096");
                log.info("Configured short-circuit read stream cache size to 4096");
            }
        } else {
            // Set cache to 0 when short-circuit reads are disabled to save memory
            hdfsSite.put("dfs.client.read.shortcircuit.streams.cache.size", "0");
            log.info("Short-circuit read stream cache disabled");
        }
    }
}
