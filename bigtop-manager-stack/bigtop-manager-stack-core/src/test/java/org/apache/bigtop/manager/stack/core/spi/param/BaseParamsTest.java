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
package org.apache.bigtop.manager.stack.core.spi.param;

import org.apache.bigtop.manager.common.utils.NetUtils;
import org.apache.bigtop.manager.common.utils.os.OSDetection;
import org.apache.bigtop.manager.grpc.payload.ComponentCommandPayload;
import org.apache.bigtop.manager.grpc.pojo.ClusterInfo;
import org.apache.bigtop.manager.grpc.pojo.PackageInfo;
import org.apache.bigtop.manager.grpc.pojo.PackageSpecificInfo;
import org.apache.bigtop.manager.grpc.pojo.RepoInfo;
import org.apache.bigtop.manager.grpc.pojo.TemplateInfo;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class BaseParamsTest {

    private MockedStatic<LocalSettings> localSettingsMockedStatic;
    private MockedStatic<OSDetection> osDetectionMockedStatic;
    private MockedStatic<NetUtils> netUtilsMockedStatic;

    private final ClusterInfo clusterInfo = new ClusterInfo();

    private final ComponentCommandPayload payload = new ComponentCommandPayload();

    private MockBaseParams mockBaseParams;

    @BeforeEach
    public void setUp() {
        List<RepoInfo> repos = new ArrayList<>();
        RepoInfo general = new RepoInfo("general", "mockArch", "testURL", "testPkgName", "testChecksum", 1);
        RepoInfo test = new RepoInfo("test", "mockArch", "testURL", "testPkgName", "testChecksum", 1);
        repos.add(general);
        repos.add(test);

        List<String> arch = new ArrayList<>();
        arch.add("mockArch");

        List<PackageInfo> packages = new ArrayList<>();
        packages.add(new PackageInfo("package1", "testChecksum1"));
        packages.add(new PackageInfo("package2", "testChecksum2"));

        List<PackageSpecificInfo> packageSpecifics = new ArrayList<>();
        PackageSpecificInfo packageSpecific = new PackageSpecificInfo();
        packageSpecific.setArch(arch);
        packageSpecific.setPackages(packages);
        packageSpecifics.add(packageSpecific);

        List<TemplateInfo> templates = new ArrayList<>();
        TemplateInfo template = new TemplateInfo();
        template.setSrc("mockSrc");
        template.setDest("mockDest");
        template.setContent("mockContent");
        templates.add(template);

        payload.setServiceUser("mockUser");
        payload.setPackageSpecifics(packageSpecifics);
        payload.setTemplates(templates);

        clusterInfo.setRootDir("/mockRoot");
        clusterInfo.setUserGroup("mockGroup");

        mockBaseParams = new MockBaseParams(payload);

        localSettingsMockedStatic = mockStatic(LocalSettings.class);
        osDetectionMockedStatic = mockStatic(OSDetection.class);
        netUtilsMockedStatic = mockStatic(NetUtils.class);
        netUtilsMockedStatic.when(NetUtils::getHostname).thenReturn("mockHostname");
        localSettingsMockedStatic.when(() -> LocalSettings.repo(any())).thenReturn(general);
        localSettingsMockedStatic.when(LocalSettings::repos).thenReturn(repos);
        localSettingsMockedStatic.when(LocalSettings::cluster).thenReturn(clusterInfo);
        osDetectionMockedStatic.when(OSDetection::getArch).thenReturn("mockArch");
    }

    @AfterEach
    public void tearDown() {
        localSettingsMockedStatic.close();
        osDetectionMockedStatic.close();
        netUtilsMockedStatic.close();
    }

    @Test
    public void testInitGlobalParams() {
        Map<String, Object> globalParamsMap = mockBaseParams.getGlobalParamsMap();
        globalParamsMap.put("a1", "k1");
        globalParamsMap.put("a2", "k2");
        globalParamsMap.put("a3", "k3");
        globalParamsMap.put("k1_k2", "kk1");
        globalParamsMap.put("kk1_k3", "value");
        globalParamsMap.put("key", "${key3}");
        globalParamsMap.put("key1", "${k1_${a2}}_${a3}");
        globalParamsMap.put("key2", "${${k1_${a2}}_${a3}}");
        globalParamsMap.put("key3", "${key1}");
        globalParamsMap.put("key4", "${not_exists}");
        mockBaseParams.initGlobalParams();
        assertEquals("kk1_k3", globalParamsMap.get("key"));
        assertEquals("kk1_k3", globalParamsMap.get("key1"));
        assertEquals("value", mockBaseParams.getGlobalParamsMap().get("key2"));
        assertEquals("kk1_k3", globalParamsMap.get("key3"));
        assertEquals("${not_exists}", globalParamsMap.get("key4"));
    }

    @Test
    public void testHostname() {
        String hostname = mockBaseParams.hostname();
        assertEquals("mockHostname", hostname);
    }

    @Test
    public void testConfDir() {
        String confDir = mockBaseParams.confDir();
        assertEquals("/mockRoot/services/mockService/conf", confDir);
    }

    @Test
    public void testUser() {
        String user = mockBaseParams.user();
        assertEquals("mockUser", user);
    }

    @Test
    public void testGroup() {
        String group = mockBaseParams.group();
        assertEquals("mockGroup", group);
    }

    @Test
    public void testRepo() {
        RepoInfo repo = mockBaseParams.repo();
        assertEquals("general", repo.getName());
        assertEquals("mockArch", repo.getArch());
    }

    @Test
    public void testPackages() {
        List<PackageInfo> packages = mockBaseParams.packages();
        assertEquals(2, packages.size());
        assertEquals("package1", packages.get(0).getName());
        assertEquals("package2", packages.get(1).getName());
    }

    @Test
    public void testTemplates() {
        List<TemplateInfo> templates = mockBaseParams.templates();
        assertEquals(1, templates.size());
        assertEquals("mockSrc", templates.get(0).getSrc());
        assertEquals("mockDest", templates.get(0).getDest());
        assertEquals("mockContent", templates.get(0).getContent());
    }

    @Test
    public void testJavaHome() {
        String javaHome = mockBaseParams.javaHome();
        assertEquals("/mockRoot/dependencies/jdk", javaHome);
    }

    @Test
    public void testStackHome() {
        String stackHome = mockBaseParams.stackHome();
        assertEquals("/mockRoot/services", stackHome);
    }

    @Test
    public void testServiceHome() {
        String serviceHome = mockBaseParams.serviceHome();
        assertEquals("/mockRoot/services/mockService", serviceHome);
    }

    private static class MockBaseParams extends BaseParams {
        public MockBaseParams(ComponentCommandPayload payload) {
            this.payload = payload;
        }

        @Override
        public String getServiceName() {
            return "mockService";
        }
    }
}
