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
package org.apache.bigtop.manager.stack.infra.param;

import org.apache.bigtop.manager.common.constants.CacheFiles;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.common.utils.ProjectPathUtils;
import org.apache.bigtop.manager.grpc.payload.ComponentCommandPayload;
import org.apache.bigtop.manager.grpc.pojo.ClusterInfo;
import org.apache.bigtop.manager.stack.core.spi.param.BaseParams;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@NoArgsConstructor
public abstract class InfraParams extends BaseParams {

    protected InfraParams(ComponentCommandPayload componentCommandPayload) {
        super(componentCommandPayload);
    }

    /**
     * Infra stack do not belong to any cluster, so we need to override this and provide a group name
     *
     * @return group name
     */
    @Override
    public String group() {
        return "infra";
    }

    protected Map<String, List<String>> getClusterHosts() {
        Map<String, List<String>> clusterHosts = new HashMap<>();
        List<String> subDirs = getClusterDirs();

        for (String subDir : subDirs) {
            List<String> hosts = JsonUtils.readFromFile(subDir + CacheFiles.HOSTS_INFO);
            Map<String, String> clusterInfo = JsonUtils.readFromFile(subDir + CacheFiles.CLUSTER_INFO);
            clusterHosts.put(clusterInfo.get("name"), hosts);
        }

        return clusterHosts;
    }

    protected Map<String, List<String>> getComponentHosts(String componentName) {
        Map<String, List<String>> componentHosts = new HashMap<>();
        List<String> subDirs = getClusterDirs();

        for (String subDir : subDirs) {
            Map<String, List<String>> components = JsonUtils.readFromFile(subDir + CacheFiles.COMPONENTS_INFO);
            Map<String, String> clusterInfo = JsonUtils.readFromFile(subDir + CacheFiles.CLUSTER_INFO);

            List<String> hosts = components.getOrDefault(componentName, List.of());
            componentHosts.put(clusterInfo.get("name"), hosts);
        }

        return componentHosts;
    }

    protected Map<String, Map<String, Object>> configurations(String service, String type) {
        Map<String, Map<String, Object>> configurations = new HashMap<>();
        List<String> subDirs = getClusterDirs();

        for (String subDir : subDirs) {
            Map<String, String> clusterInfo = JsonUtils.readFromFile(subDir + CacheFiles.CLUSTER_INFO);
            Map<String, Map<String, Object>> configJson =
                    JsonUtils.readFromFile(subDir + CacheFiles.CONFIGURATIONS_INFO);

            Object configData =
                    configJson.getOrDefault(service, new HashMap<>()).getOrDefault(type, new HashMap<>());
            Map<String, Object> map = JsonUtils.readFromString(configData.toString());
            configurations.put(clusterInfo.get("name"), map);
        }

        return configurations;
    }

    protected Map<String, Object> configurations(String service, String type, String key) {
        Map<String, Object> configurations = new HashMap<>();
        List<String> subDirs = getClusterDirs();

        for (String subDir : subDirs) {
            Map<String, String> clusterInfo = JsonUtils.readFromFile(subDir + CacheFiles.CLUSTER_INFO);
            Map<String, Map<String, Object>> configJson =
                    JsonUtils.readFromFile(subDir + CacheFiles.CONFIGURATIONS_INFO);

            Object configData =
                    configJson.getOrDefault(service, new HashMap<>()).get(type);
            Map<String, Object> map = JsonUtils.readFromString(configData.toString());
            configurations.put(clusterInfo.get("name"), map.getOrDefault(key, null));
        }

        return configurations;
    }

    protected List<ClusterInfo> clusters() {
        return getClusterDirs().stream()
                .map(dir -> JsonUtils.readFromFile(dir + CacheFiles.CLUSTER_INFO, new TypeReference<ClusterInfo>() {}))
                .toList();
    }

    /**
     * Get the cluster info of the host where the component is running on.
     * Since infra service can be installed across clusters, this will get different cluster info based on the host.
     *
     * @return ClusterInfo
     */
    protected ClusterInfo hostCluster() {
        return LocalSettings.cluster();
    }

    private List<String> getClusterDirs() {
        File file = new File(ProjectPathUtils.getAgentCachePath());
        return Arrays.stream(Objects.requireNonNull(file.listFiles()))
                .filter(File::isDirectory)
                .map(File::getAbsolutePath)
                .toList();
    }
}
