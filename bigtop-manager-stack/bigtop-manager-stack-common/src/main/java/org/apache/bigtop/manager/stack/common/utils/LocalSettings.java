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
package org.apache.bigtop.manager.stack.common.utils;

import org.apache.bigtop.manager.common.constants.CacheFiles;
import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.message.entity.pojo.ClusterInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.ComponentInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.RepoInfo;
import org.apache.bigtop.manager.common.utils.JsonUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class LocalSettings {

    public static Object configurations(String service, String type, String key, Object defaultValue) {
        Map<String, Object> configMap = configurations(service, type);
        return configMap.getOrDefault(key, defaultValue);
    }

    public static Map<String, Object> configurations(String service, String type) {

        Map<String, Object> configDataMap = new HashMap<>();
        File file = new File(Constants.STACK_CACHE_DIR + CacheFiles.CONFIGURATIONS_INFO);
        try {
            if (file.exists()) {
                Map<String, Map<String, Object>> configJson = JsonUtils.readFromFile(file, new TypeReference<>() {});
                Object configData =
                        configJson.getOrDefault(service, new HashMap<>()).get(type);
                if (configData != null) {
                    configDataMap = JsonUtils.readFromString((String) configData, new TypeReference<>() {});
                }
            }
        } catch (Exception e) {
            log.warn("{} parse error", CacheFiles.CONFIGURATIONS_INFO, e);
        }

        return configDataMap;
    }

    public static List<String> hosts(String componentName) {
        return hosts().getOrDefault(componentName, List.of());
    }

    public static Map<String, List<String>> hosts() {

        Map<String, List<String>> hostJson = new HashMap<>();
        File file = new File(Constants.STACK_CACHE_DIR + CacheFiles.HOSTS_INFO);
        if (file.exists()) {
            hostJson = JsonUtils.readFromFile(file, new TypeReference<>() {});
        }
        return hostJson;
    }

    public static Map<String, Object> basicInfo() {

        Map<String, Object> settings = new HashMap<>();
        File file = new File(Constants.STACK_CACHE_DIR + CacheFiles.SETTINGS_INFO);
        if (file.exists()) {
            settings = JsonUtils.readFromFile(file, new TypeReference<>() {});
        }
        return settings;
    }

    public static Map<String, Set<String>> users() {

        Map<String, Set<String>> userMap = new HashMap<>();
        File file = new File(Constants.STACK_CACHE_DIR + CacheFiles.USERS_INFO);
        if (file.exists()) {
            userMap = JsonUtils.readFromFile(file, new TypeReference<>() {});
        }
        return userMap;
    }

    public static List<String> packages() {
        ClusterInfo cluster = cluster();
        return Optional.ofNullable(cluster.getPackages()).orElse(List.of());
    }

    public static List<RepoInfo> repos() {

        List<RepoInfo> repoInfoList = List.of();
        File file = new File(Constants.STACK_CACHE_DIR + CacheFiles.REPOS_INFO);
        if (file.exists()) {
            repoInfoList = JsonUtils.readFromFile(file, new TypeReference<>() {});
        }
        return repoInfoList;
    }

    public static ClusterInfo cluster() {

        ClusterInfo clusterInfo = new ClusterInfo();
        File file = new File(Constants.STACK_CACHE_DIR + CacheFiles.CLUSTER_INFO);
        if (file.exists()) {
            clusterInfo = JsonUtils.readFromFile(file, new TypeReference<>() {});
        }
        return clusterInfo;
    }

    public static Map<String, ComponentInfo> components() {

        Map<String, ComponentInfo> componentInfo = new HashMap<>();
        File file = new File(Constants.STACK_CACHE_DIR + CacheFiles.COMPONENTS_INFO);
        if (file.exists()) {
            componentInfo = JsonUtils.readFromFile(file, new TypeReference<>() {});
        }
        return componentInfo;
    }
}
