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
package org.apache.bigtop.manager.stack.core.utils;

import org.apache.bigtop.manager.common.constants.CacheFiles;
import org.apache.bigtop.manager.common.utils.FileUtils;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.common.utils.ProjectPathUtils;
import org.apache.bigtop.manager.common.utils.os.OSDetection;
import org.apache.bigtop.manager.grpc.pojo.ClusterInfo;
import org.apache.bigtop.manager.grpc.pojo.RepoInfo;
import org.apache.bigtop.manager.stack.core.exception.StackException;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class LocalSettings {

    public static Object configurations(String service, String type, String key, Object defaultValue) {
        Map<String, Object> configMap = configurations(service, type);
        return configMap.getOrDefault(key, defaultValue);
    }

    public static Map<String, Object> configurations(String service, String type) {
        Map<String, Object> configDataMap = new HashMap<>();
        File file = createFile(clusterCacheDir() + CacheFiles.CONFIGURATIONS_INFO);
        try {
            if (file.exists()) {
                Map<String, Map<String, Object>> configJson = JsonUtils.readFromFile(file, new TypeReference<>() {});
                Object configData =
                        configJson.getOrDefault(service, new HashMap<>()).get(type);
                if (configData != null) {
                    configDataMap = JsonUtils.readFromString(configData.toString(), new TypeReference<>() {});
                }
            }
        } catch (Exception e) {
            log.warn("{} parse error", CacheFiles.CONFIGURATIONS_INFO, e);
        }

        return configDataMap;
    }

    public static List<String> componentHosts(String componentName) {
        return componentHosts().getOrDefault(componentName, List.of());
    }

    public static Map<String, List<String>> componentHosts() {
        Map<String, List<String>> hostJson = new HashMap<>();
        File file = createFile(clusterCacheDir() + CacheFiles.COMPONENTS_INFO);
        if (file.exists()) {
            hostJson = JsonUtils.readFromFile(file, new TypeReference<>() {});
        }
        return hostJson;
    }

    public static List<String> clusterHosts() {
        List<String> hosts = new ArrayList<>();
        File file = createFile(clusterCacheDir() + CacheFiles.COMPONENTS_INFO);
        if (file.exists()) {
            hosts = JsonUtils.readFromFile(file, new TypeReference<>() {});
        }
        return hosts;
    }

    public static Map<String, Object> basicInfo() {
        Map<String, Object> settings = new HashMap<>();
        File file = createFile(clusterCacheDir() + CacheFiles.SETTINGS_INFO);
        if (file.exists()) {
            settings = JsonUtils.readFromFile(file, new TypeReference<>() {});
        }
        return settings;
    }

    public static Map<String, String> users() {
        Map<String, String> userMap = new HashMap<>();
        File file = createFile(clusterCacheDir() + CacheFiles.USERS_INFO);
        if (file.exists()) {
            userMap = JsonUtils.readFromFile(file, new TypeReference<>() {});
        }
        return userMap;
    }

    public static List<String> packages() {
        return List.of();
    }

    public static RepoInfo repo(String name) {
        String arch = OSDetection.getArch();
        List<RepoInfo> repoInfoList = repos();
        for (RepoInfo repoInfo : repoInfoList) {
            if (repoInfo.getName().equals(name) && repoInfo.getArch().contains(arch)) {
                return repoInfo;
            }
        }
        log.error("Cannot find repo: [{}], arch: [{}]", name, arch);
        throw new StackException("Repo not found: " + name);
    }

    public static List<RepoInfo> repos() {
        List<RepoInfo> repoInfoList = List.of();
        File file = createFile(clusterCacheDir() + CacheFiles.REPOS_INFO);
        if (file.exists()) {
            repoInfoList = JsonUtils.readFromFile(file, new TypeReference<>() {});
        }
        return repoInfoList;
    }

    public static ClusterInfo cluster() {
        ClusterInfo clusterInfo = new ClusterInfo();
        File file = createFile(clusterCacheDir() + CacheFiles.CLUSTER_INFO);
        if (file.exists()) {
            clusterInfo = JsonUtils.readFromFile(file, new TypeReference<>() {});
        }
        return clusterInfo;
    }

    protected static String clusterCacheDir() {
        String agentCachePath = ProjectPathUtils.getAgentCachePath();
        String clusterId = FileUtils.readFile2Str(agentCachePath + File.separator + "current");
        return agentCachePath + File.separator + clusterId;
    }

    protected static File createFile(String fileName) {
        return new File(fileName);
    }
}
