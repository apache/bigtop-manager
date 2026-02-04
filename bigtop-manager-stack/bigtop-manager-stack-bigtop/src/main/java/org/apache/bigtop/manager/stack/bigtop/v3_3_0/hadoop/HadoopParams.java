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

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
@Slf4j
@AutoService(Params.class)
@NoArgsConstructor
public class HadoopParams extends BigtopParams {

    private String resolveNameService() {
        try {
            Map<String, Object> hdfsSite = LocalSettings.configurations(getServiceName(), "hdfs-site");
            Object ns = hdfsSite.get("dfs.nameservices");
            if (ns != null && StringUtils.isNotBlank(ns.toString())) {
                return ns.toString().split("\\s*,\\s*")[0].trim();
            }
        } catch (Exception e) {
            // ignore and fallback
        }
        return "nameservice1";
    }

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

    private String hadoopConfContent;

    private String dfsDataDir;
    private String dfsNameNodeDir;
    private String dfsNameNodeCheckPointDir;
    private String dfsDomainSocketPathPrefix;
    private String dfsJourNalNodeDir;
    private String dfsHttpPort;
    private String journalHttpPort;

    private String nodeManagerLogDir = "/hadoop/yarn/log";
    private String nodeManagerLocalDir = "/hadoop/yarn/local";

    private List<String> nameNodeFormattedDirs;

    public HadoopParams(ComponentCommandPayload componentCommandPayload) {
        super(componentCommandPayload);
        globalParamsMap.put("hadoop_user", user());
        globalParamsMap.put("hadoop_group", group());
        globalParamsMap.put("datanode_hosts", LocalSettings.componentHosts("datanode"));
        globalParamsMap.put("java_home", javaHome());
        globalParamsMap.put("hadoop_home", serviceHome());
        globalParamsMap.put("hadoop_conf_dir", confDir());
        globalParamsMap.put("hadoop_libexec_dir", serviceHome() + "/libexec");
        globalParamsMap.put("exclude_hosts", new ArrayList<>());
    }

    @GlobalParams
    public Map<String, Object> hadoopLimits() {
        Map<String, Object> hadoopConf = LocalSettings.configurations(getServiceName(), "hadoop.conf");
        hadoopConfContent = hadoopConf.get("content").toString();
        return hadoopConf;
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
        List<String> zookeeperServerHosts = LocalSettings.componentHosts("zookeeper_server");
        Map<String, Object> ZKPort = LocalSettings.configurations("zookeeper", "zoo.cfg");
        String clientPort = (String) ZKPort.get("clientPort");
        StringBuilder zkString = new StringBuilder();
        for (int i = 0; i < zookeeperServerHosts.size(); i++) {
            String host = zookeeperServerHosts.get(i);
            if (host == null || host.trim().isEmpty()) {
                continue;
            }
            zkString.append(host.trim()).append(":").append(clientPort);
            if (i != zookeeperServerHosts.size() - 1) {
                zkString.append(",");
            }
        }
        if (!namenodeList.isEmpty() && namenodeList.size() == 1) {
            coreSite.put(
                    "fs.defaultFS", ((String) coreSite.get("fs.defaultFS")).replace("localhost", namenodeList.get(0)));
        } else if (!namenodeList.isEmpty() && namenodeList.size() == 2) {
            String nameservice = resolveNameService();
            coreSite.put(
                    "fs.defaultFS", ((String) coreSite.get("fs.defaultFS")).replace("localhost:8020", nameservice));
            coreSite.put("ha.zookeeper.quorum", zkString);
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
        List<String> journalNodeList = LocalSettings.componentHosts("journalnode");

        String nameservice = resolveNameService();
        boolean haByConfig = false;
        Object haNn = hdfsSite.get("dfs.ha.namenodes." + nameservice);
        Object haRpc1 = hdfsSite.get("dfs.namenode.rpc-address." + nameservice + ".nn1");
        Object haRpc2 = hdfsSite.get("dfs.namenode.rpc-address." + nameservice + ".nn2");
        if ((haNn != null && StringUtils.isNotBlank(haNn.toString()))
                || (haRpc1 != null && StringUtils.isNotBlank(haRpc1.toString()))
                || (haRpc2 != null && StringUtils.isNotBlank(haRpc2.toString()))) {
            haByConfig = true;
        }

        if (haByConfig) {
            // HA mode: do not rely on components.json namenode list, because it may be stale.
            // During enable-ha bootstrap, journalnode components may be in the process of being installed,
            // so allow falling back when journalnode list is not ready yet.
            if (journalNodeList == null || journalNodeList.size() < 3) {
                log.warn(
                        "JournalNode host list is not ready (size < 3), skip HA hdfs-site generation for now and fall back to non-HA config. journalNodeList={}",
                        journalNodeList);
                haByConfig = false;
            }

            List<String> filteredJournalNodes = journalNodeList.stream()
                    .filter(StringUtils::isNotBlank)
                    .map(String::trim)
                    .distinct()
                    .toList();
            if (filteredJournalNodes.size() < 3) {
                log.warn(
                        "JournalNode host list is invalid after filtering blanks (size < 3), skip HA hdfs-site generation for now and fall back to non-HA config. journalNodeList={} filteredJournalNodes={}",
                        journalNodeList,
                        filteredJournalNodes);
                haByConfig = false;
            }

            String journalQuorum =
                    filteredJournalNodes.stream().map(x -> x + ":8485").collect(Collectors.joining(";"));

            hdfsSite.remove("dfs.namenode.rpc-address");
            hdfsSite.remove("dfs.namenode.https-address");
            hdfsSite.remove("dfs.namenode.http-address");

            hdfsSite.put("dfs.ha.automatic-failover.enabled", "true");
            hdfsSite.put("dfs.nameservices", nameservice);
            if (hdfsSite.get("dfs.ha.namenodes." + nameservice) == null) {
                hdfsSite.put("dfs.ha.namenodes." + nameservice, "nn1,nn2");
            }
            hdfsSite.put("dfs.namenode.shared.edits.dir", "qjournal://" + journalQuorum + "/" + nameservice);

            // Ensure required HA keys exist (respect existing values if present)
            if (hdfsSite.get("dfs.namenode.rpc-address." + nameservice + ".nn1") == null
                    && namenodeList != null
                    && namenodeList.size() >= 1) {
                hdfsSite.put("dfs.namenode.rpc-address." + nameservice + ".nn1", namenodeList.get(0) + ":8020");
            }
            if (hdfsSite.get("dfs.namenode.rpc-address." + nameservice + ".nn2") == null
                    && namenodeList != null
                    && namenodeList.size() >= 2) {
                hdfsSite.put("dfs.namenode.rpc-address." + nameservice + ".nn2", namenodeList.get(1) + ":8020");
            }
            if (hdfsSite.get("dfs.namenode.http-address." + nameservice + ".nn1") == null
                    && namenodeList != null
                    && namenodeList.size() >= 1) {
                hdfsSite.put("dfs.namenode.http-address." + nameservice + ".nn1", namenodeList.get(0) + ":9870");
            }
            if (hdfsSite.get("dfs.namenode.http-address." + nameservice + ".nn2") == null
                    && namenodeList != null
                    && namenodeList.size() >= 2) {
                hdfsSite.put("dfs.namenode.http-address." + nameservice + ".nn2", namenodeList.get(1) + ":9870");
            }

            if (hdfsSite.get("dfs.client.failover.proxy.provider." + nameservice) == null) {
                hdfsSite.put(
                        "dfs.client.failover.proxy.provider." + nameservice,
                        "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
            }
            if (hdfsSite.get("dfs.ha.fencing.methods") == null) {
                hdfsSite.put("dfs.ha.fencing.methods", "shell(/bin/true)");
            }
            if (hdfsSite.get("dfs.replication") == null) {
                hdfsSite.put("dfs.replication", "3");
            }

        } else if (namenodeList != null && !namenodeList.isEmpty()) {
            // Single NN mode
            String nnHost = namenodeList.get(0);

            Object rpcAddr = hdfsSite.get("dfs.namenode.rpc-address");
            if (rpcAddr == null) {
                throw new IllegalArgumentException("Missing required hdfs-site key: dfs.namenode.rpc-address");
            }
            hdfsSite.put("dfs.namenode.rpc-address", rpcAddr.toString().replace("0.0.0.0", nnHost));

            Object dnHttpsAddr = hdfsSite.get("dfs.datanode.https.address");
            if (dnHttpsAddr == null) {
                throw new IllegalArgumentException("Missing required hdfs-site key: dfs.datanode.https.address");
            }
            hdfsSite.put("dfs.datanode.https.address", dnHttpsAddr.toString().replace("0.0.0.0", nnHost));

            Object nnHttpsAddr = hdfsSite.get("dfs.namenode.https-address");
            if (nnHttpsAddr == null) {
                throw new IllegalArgumentException("Missing required hdfs-site key: dfs.namenode.https-address");
            }
            hdfsSite.put("dfs.namenode.https-address", nnHttpsAddr.toString().replace("0.0.0.0", nnHost));
        }

        // Configure native library dependent settings
        configureNativeLibraryDependentSettings(hdfsSite);

        dfsDataDir = (String) hdfsSite.get("dfs.datanode.data.dir");
        dfsNameNodeDir = (String) hdfsSite.get("dfs.namenode.name.dir");
        if (StringUtils.isNotBlank(dfsNameNodeDir)) {
            nameNodeFormattedDirs = Arrays.stream(dfsNameNodeDir.split(","))
                    .map(x -> x + "/namenode-formatted/")
                    .toList();
        } else {
            nameNodeFormattedDirs = List.of();
            log.warn("dfs.namenode.name.dir is empty, skip namenode formatted dirs generation");
        }

        String resolvedNameService = resolveNameService();
        String dfsHttpAddress = (String) hdfsSite.get("dfs.namenode.http-address." + resolvedNameService + ".nn1");
        if (StringUtils.isBlank(dfsHttpAddress)) {
            // backward compatibility / older templates
            dfsHttpAddress = (String) hdfsSite.get("dfs.namenode.http-address.nameservice1.nn1");
        }
        if (dfsHttpAddress != null && dfsHttpAddress.contains(":")) {
            String[] parts = dfsHttpAddress.split(":");
            if (parts.length >= 2) {
                dfsHttpPort = parts[1].trim();
            }
        }
        String journalHttpAddress = (String) hdfsSite.get("dfs.namenode.shared.edits.dir");
        if (StringUtils.isNotBlank(journalHttpAddress)) {
            Pattern pattern = Pattern.compile(":(\\d{1,5})");
            Matcher matcher = pattern.matcher(journalHttpAddress);
            if (matcher.find()) {
                journalHttpPort = matcher.group(1);
                log.info("find jounalnode port: " + journalHttpPort);
            } else {
                log.warn("not found journalnode port!");
            }
        } else {
            log.warn("dfs.namenode.shared.edits.dir is empty, skip journalnode port parsing");
        }
        String dfsDomainSocketPath = (String) hdfsSite.get("dfs.domain.socket.path");
        if (StringUtils.isNotBlank(dfsDomainSocketPath)) {
            File file = new File(dfsDomainSocketPath);
            dfsDomainSocketPathPrefix = file.getParent();
            //            dfsDomainSocketPathPrefix = dfsDomainSocketPath.replace("dn._PORT", "");
        }
        dfsNameNodeCheckPointDir = (String) hdfsSite.get("dfs.namenode.checkpoint.dir");
        dfsJourNalNodeDir = (String) hdfsSite.get("dfs.journalnode.edits.dir");
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

        // YARN ResourceManager HA
        // When there are >= 2 RMs, or `yarn.resourcemanager.ha.enabled=true` is explicitly set,
        // enter HA mode. In HA mode, do not set single-RM keys like `yarn.resourcemanager.hostname`
        // to avoid conflicts.
        boolean haEnabledByConfig = false;
        Object haEnabledValue = yarnSite.get("yarn.resourcemanager.ha.enabled");
        if (haEnabledValue != null) {
            haEnabledByConfig =
                    "true".equalsIgnoreCase(haEnabledValue.toString().trim());
        }
        boolean haMode = (resourcemanagerList != null && resourcemanagerList.size() >= 2) || haEnabledByConfig;

        if (haMode && resourcemanagerList != null && resourcemanagerList.size() >= 2) {
            String rm1Host = resourcemanagerList.get(0);
            String rm2Host = resourcemanagerList.get(1);

            // rm-ids: Use existing config if present, otherwise default to rm1,rm2
            String rmIds = "rm1,rm2";
            Object rmIdsObj = yarnSite.get("yarn.resourcemanager.ha.rm-ids");
            if (rmIdsObj != null && StringUtils.isNotBlank(rmIdsObj.toString())) {
                rmIds = rmIdsObj.toString().trim();
            }
            String[] rmIdArr = rmIds.split("\\s*,\\s*");
            String rm1Id = rmIdArr.length > 0 && StringUtils.isNotBlank(rmIdArr[0]) ? rmIdArr[0] : "rm1";
            String rm2Id = rmIdArr.length > 1 && StringUtils.isNotBlank(rmIdArr[1]) ? rmIdArr[1] : "rm2";

            yarnSite.put("yarn.resourcemanager.ha.enabled", "true");
            yarnSite.put("yarn.resourcemanager.ha.rm-ids", rm1Id + "," + rm2Id);

            // cluster-id: Respect if set by server, otherwise provide a stable default
            if (yarnSite.get("yarn.resourcemanager.cluster-id") == null
                    || StringUtils.isBlank(
                            yarnSite.get("yarn.resourcemanager.cluster-id").toString())) {
                yarnSite.put("yarn.resourcemanager.cluster-id", "yarn-cluster");
            }

            // zk-address: Respect if set by server, otherwise auto-generate like in coreSite()
            Object zkAddr = yarnSite.get("yarn.resourcemanager.zk-address");
            if (zkAddr == null || StringUtils.isBlank(zkAddr.toString())) {
                try {
                    List<String> zookeeperServerHosts = LocalSettings.componentHosts("zookeeper_server");
                    Map<String, Object> ZKPort = LocalSettings.configurations("zookeeper", "zoo.cfg");
                    String clientPort = (String) ZKPort.get("clientPort");
                    StringBuilder zkString = new StringBuilder();
                    for (int i = 0; i < zookeeperServerHosts.size(); i++) {
                        String host = zookeeperServerHosts.get(i);
                        if (host == null || host.trim().isEmpty()) {
                            continue;
                        }
                        zkString.append(host.trim()).append(":").append(clientPort);
                        if (i != zookeeperServerHosts.size() - 1) {
                            zkString.append(",");
                        }
                    }
                    if (zkString.length() > 0) {
                        yarnSite.put("yarn.resourcemanager.zk-address", zkString.toString());
                    }
                } catch (Exception e) {
                    log.warn("Failed to auto-generate yarn.resourcemanager.zk-address", e);
                }
            }

            // Set hostname.rmX
            yarnSite.put("yarn.resourcemanager.hostname." + rm1Id, rm1Host);
            yarnSite.put("yarn.resourcemanager.hostname." + rm2Id, rm2Host);

            // webapp.address.rmX: Extract port from existing webapp.address, or default to 8088
            int webappPort = 8088;
            Object webappAddress = yarnSite.get("yarn.resourcemanager.webapp.address");
            if (webappAddress != null && webappAddress.toString().contains(":")) {
                try {
                    String portStr = webappAddress.toString().split(":")[1].trim();
                    webappPort = Integer.parseInt(portStr);
                } catch (Exception ignored) {
                }
            }
            yarnSite.put("yarn.resourcemanager.webapp.address." + rm1Id, rm1Host + ":" + webappPort);
            yarnSite.put("yarn.resourcemanager.webapp.address." + rm2Id, rm2Host + ":" + webappPort);

            // Auto-generate other HA addresses by extracting ports from single-node configs
            generateHaAddress(yarnSite, "yarn.resourcemanager.address", rm1Id, rm1Host, rm2Id, rm2Host, 8032);
            generateHaAddress(yarnSite, "yarn.resourcemanager.admin.address", rm1Id, rm1Host, rm2Id, rm2Host, 8033);
            generateHaAddress(
                    yarnSite, "yarn.resourcemanager.resource-tracker.address", rm1Id, rm1Host, rm2Id, rm2Host, 8031);
            generateHaAddress(yarnSite, "yarn.resourcemanager.scheduler.address", rm1Id, rm1Host, rm2Id, rm2Host, 8030);

            // Remove single-RM keys to avoid conflicts
            yarnSite.remove("yarn.resourcemanager.hostname");
            yarnSite.remove("yarn.resourcemanager.address");
            yarnSite.remove("yarn.resourcemanager.admin.address");
            yarnSite.remove("yarn.resourcemanager.resource-tracker.address");
            yarnSite.remove("yarn.resourcemanager.scheduler.address");
            yarnSite.remove("yarn.resourcemanager.webapp.address");
            yarnSite.remove("yarn.resourcemanager.webapp.https.address");

        } else {
            // Single ResourceManager
            if (resourcemanagerList != null && !resourcemanagerList.isEmpty()) {
                String rmHost = resourcemanagerList.get(0);
                yarnSite.put("yarn.resourcemanager.hostname", MessageFormat.format("{0}", rmHost));

                String rt = (String) yarnSite.get("yarn.resourcemanager.resource-tracker.address");
                if (rt != null) {
                    yarnSite.put("yarn.resourcemanager.resource-tracker.address", rt.replace("0.0.0.0", rmHost));
                }

                String scheduler = (String) yarnSite.get("yarn.resourcemanager.scheduler.address");
                if (scheduler != null) {
                    yarnSite.put("yarn.resourcemanager.scheduler.address", scheduler.replace("0.0.0.0", rmHost));
                }

                String addr = (String) yarnSite.get("yarn.resourcemanager.address");
                if (addr != null) {
                    yarnSite.put("yarn.resourcemanager.address", addr.replace("0.0.0.0", rmHost));
                }

                String admin = (String) yarnSite.get("yarn.resourcemanager.admin.address");
                if (admin != null) {
                    yarnSite.put("yarn.resourcemanager.admin.address", admin.replace("0.0.0.0", rmHost));
                }

                String webapp = (String) yarnSite.get("yarn.resourcemanager.webapp.address");
                if (webapp != null) {
                    yarnSite.put("yarn.resourcemanager.webapp.address", webapp.replace("0.0.0.0", rmHost));
                }

                String https = (String) yarnSite.get("yarn.resourcemanager.webapp.https.address");
                if (https != null) {
                    yarnSite.put("yarn.resourcemanager.webapp.https.address", https.replace("0.0.0.0", rmHost));
                }
            }
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
    private static void generateHaAddress(
            Map<String, Object> yarnSite,
            String baseKey,
            String rm1Id,
            String rm1Host,
            String rm2Id,
            String rm2Host,
            int defaultPort) {

        int port = defaultPort;
        Object base = yarnSite.get(baseKey);
        if (base != null && base.toString().contains(":")) {
            try {
                String portStr = base.toString().split(":")[1].trim();
                port = Integer.parseInt(portStr);
            } catch (Exception ignored) {
            }
        }

        yarnSite.put(baseKey + "." + rm1Id, rm1Host + ":" + port);
        yarnSite.put(baseKey + "." + rm2Id, rm2Host + ":" + port);
    }

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
