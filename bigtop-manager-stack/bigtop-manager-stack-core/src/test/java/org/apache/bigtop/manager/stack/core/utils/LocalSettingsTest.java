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

import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.common.utils.ProjectPathUtils;
import org.apache.bigtop.manager.grpc.pojo.ClusterInfo;
import org.apache.bigtop.manager.grpc.pojo.RepoInfo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class LocalSettingsTest {

    private MockedStatic<JsonUtils> jsonUtilsMockedStatic;

    private MockedStatic<ProjectPathUtils> projectPathUtilsMockedStatic;

    private MockedStatic<LocalSettings> localSettingsMockedStatic;

    @BeforeEach
    public void setUp() {
        jsonUtilsMockedStatic = mockStatic(JsonUtils.class);
        projectPathUtilsMockedStatic = mockStatic(ProjectPathUtils.class);
        localSettingsMockedStatic = mockStatic(LocalSettings.class);
    }

    @AfterEach
    public void tearDown() {
        jsonUtilsMockedStatic.close();
        projectPathUtilsMockedStatic.close();
        localSettingsMockedStatic.close();
    }

    @Test
    public void testConfigurations() throws Exception {
        String service = "serviceA";
        String type = "typeA";
        String key = "keyA";
        String defaultValue = "defaultValue";

        Map<String, Object> configDataMap = new HashMap<>();
        configDataMap.put(key, "valueA");
        String configDataMapJson = new ObjectMapper().writeValueAsString(configDataMap);

        Map<String, Map<String, Object>> configJson = new HashMap<>();
        configJson.put(service, new HashMap<>());
        configJson.get(service).put(type, configDataMapJson);

        File file = mock(File.class);
        localSettingsMockedStatic.when(ProjectPathUtils::getAgentCachePath).thenReturn("/mock/path/");
        localSettingsMockedStatic
                .when(() -> LocalSettings.createFile(anyString()))
                .thenReturn(file);
        when(file.exists()).thenReturn(true);
        localSettingsMockedStatic
                .when(() -> JsonUtils.readFromFile(any(File.class), any(TypeReference.class)))
                .thenReturn(configJson);
        localSettingsMockedStatic
                .when(() -> JsonUtils.readFromString(anyString(), any(TypeReference.class)))
                .thenCallRealMethod();
        localSettingsMockedStatic
                .when(() -> LocalSettings.configurations(anyString(), anyString(), anyString(), any()))
                .thenCallRealMethod();

        localSettingsMockedStatic
                .when(() -> LocalSettings.configurations(anyString(), anyString()))
                .thenCallRealMethod();
        assertEquals("valueA", LocalSettings.configurations(service, type, key, defaultValue));

        localSettingsMockedStatic
                .when(() -> LocalSettings.configurations(anyString(), anyString()))
                .thenReturn(null);
        assertEquals("defaultValue", LocalSettings.configurations(service, type, key, defaultValue));
    }

    @Test
    public void testComponentHosts() {
        String componentName = "componentA";

        Map<String, List<String>> hostJson = new HashMap<>();
        hostJson.put(componentName, List.of("host1", "host2"));

        File file = mock(File.class);
        localSettingsMockedStatic.when(ProjectPathUtils::getAgentCachePath).thenReturn("/mock/path/");
        localSettingsMockedStatic
                .when(() -> LocalSettings.createFile(anyString()))
                .thenReturn(file);
        when(file.exists()).thenReturn(true);
        localSettingsMockedStatic
                .when(() -> JsonUtils.readFromFile(any(File.class), any(TypeReference.class)))
                .thenReturn(hostJson);
        localSettingsMockedStatic
                .when(() -> LocalSettings.componentHosts(anyString()))
                .thenCallRealMethod();
        localSettingsMockedStatic.when(LocalSettings::componentHosts).thenCallRealMethod();

        List<String> expectedHosts = List.of("host1", "host2");
        assertEquals(expectedHosts, LocalSettings.componentHosts(componentName));
    }

    @Test
    public void testBasicInfo() {
        Map<String, Object> settingsData = new HashMap<>();
        settingsData.put("key1", "value1");

        File file = mock(File.class);
        localSettingsMockedStatic.when(ProjectPathUtils::getAgentCachePath).thenReturn("/mock/path/");
        localSettingsMockedStatic
                .when(() -> LocalSettings.createFile(anyString()))
                .thenReturn(file);
        when(file.exists()).thenReturn(true);
        localSettingsMockedStatic
                .when(() -> JsonUtils.readFromFile(any(File.class), any(TypeReference.class)))
                .thenReturn(settingsData);
        localSettingsMockedStatic.when(LocalSettings::basicInfo).thenCallRealMethod();

        Map<String, Object> expectedSettings = new HashMap<>();
        expectedSettings.put("key1", "value1");
        assertEquals(expectedSettings, LocalSettings.basicInfo());
    }

    @Test
    public void testUsers() {
        Map<String, String> userMap = new HashMap<>();
        userMap.put("user1", "password1");

        File file = mock(File.class);
        localSettingsMockedStatic.when(ProjectPathUtils::getAgentCachePath).thenReturn("/mock/path/");
        localSettingsMockedStatic
                .when(() -> LocalSettings.createFile(anyString()))
                .thenReturn(file);
        when(file.exists()).thenReturn(true);
        localSettingsMockedStatic
                .when(() -> JsonUtils.readFromFile(any(File.class), any(TypeReference.class)))
                .thenReturn(userMap);
        localSettingsMockedStatic.when(LocalSettings::users).thenCallRealMethod();

        Map<String, String> expectedUserMap = new HashMap<>();
        expectedUserMap.put("user1", "password1");
        assertEquals(expectedUserMap, LocalSettings.users());
    }

    @Test
    public void testRepos() {
        RepoInfo repo1 = new RepoInfo("repo1", "x86_64", "http://repo1.com", "pkg", "MD5:123", 1);
        RepoInfo repo2 = new RepoInfo("repo2", "arch64", "http://repo2.com", "pkg", "MD5:123", 1);
        List<RepoInfo> repoInfoList = List.of(repo1, repo2);

        File file = mock(File.class);
        localSettingsMockedStatic.when(ProjectPathUtils::getAgentCachePath).thenReturn("/mock/path/");
        localSettingsMockedStatic
                .when(() -> LocalSettings.createFile(anyString()))
                .thenReturn(file);
        when(file.exists()).thenReturn(true);
        localSettingsMockedStatic
                .when(() -> JsonUtils.readFromFile(any(File.class), any(TypeReference.class)))
                .thenReturn(repoInfoList);
        localSettingsMockedStatic.when(LocalSettings::repos).thenCallRealMethod();

        List<RepoInfo> expectedRepoInfoList = List.of(
                new RepoInfo("repo1", "x86_64", "http://repo1.com", "pkg", "MD5:123", 1),
                new RepoInfo("repo2", "arch64", "http://repo2.com", "pkg", "MD5:123", 1));
        assertEquals(expectedRepoInfoList, LocalSettings.repos());
    }

    @Test
    public void testCluster() {
        ClusterInfo clusterInfo = new ClusterInfo();
        clusterInfo.setName("Test Cluster");

        File file = mock(File.class);
        localSettingsMockedStatic.when(ProjectPathUtils::getAgentCachePath).thenReturn("/mock/path/");
        localSettingsMockedStatic
                .when(() -> LocalSettings.createFile(anyString()))
                .thenReturn(file);
        when(file.exists()).thenReturn(true);
        localSettingsMockedStatic
                .when(() -> JsonUtils.readFromFile(any(File.class), any(TypeReference.class)))
                .thenReturn(clusterInfo);
        localSettingsMockedStatic.when(LocalSettings::cluster).thenCallRealMethod();

        ClusterInfo expectedClusterInfo = new ClusterInfo();
        expectedClusterInfo.setName("Test Cluster");
        assertEquals(expectedClusterInfo, LocalSettings.cluster());
    }

    @Test
    public void testPackages() {
        List<String> expectedPackages = List.of();
        assertEquals(expectedPackages, LocalSettings.packages());
    }
}
