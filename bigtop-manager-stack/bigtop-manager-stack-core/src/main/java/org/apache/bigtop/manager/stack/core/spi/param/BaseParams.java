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
import org.apache.bigtop.manager.grpc.pojo.PackageInfo;
import org.apache.bigtop.manager.grpc.pojo.PackageSpecificInfo;
import org.apache.bigtop.manager.grpc.pojo.RepoInfo;
import org.apache.bigtop.manager.grpc.pojo.TemplateInfo;
import org.apache.bigtop.manager.stack.core.annotations.GlobalParams;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@NoArgsConstructor
public abstract class BaseParams implements Params {

    protected final Map<String, Object> globalParamsMap = new HashMap<>();

    private boolean globalParamsResolved = false;

    private static int CURRENT_RESOLVING_DEPTH = 0;
    private static final int MAX_RECURSION_DEPTH = 5;

    public static final String LIMITS_CONF_DIR = "/etc/security/limits.d";

    protected ComponentCommandPayload payload;

    protected BaseParams(ComponentCommandPayload payload) {
        this.payload = payload;
    }

    public void putGlobalParam(String key, Object value) {
        if (value instanceof String s) {
            if (s.contains("${") && s.contains("}")) {
                globalParamsResolved = false;
            }
        }

        globalParamsMap.put(key, value);
    }

    public Object getGlobalParam(String key) {
        if (!globalParamsResolved) {
            recursivelyResolveGlobalParams();
        }

        return globalParamsMap.get(key);
    }

    public Map<String, Object> getGlobalParamsMap() {
        if (!globalParamsResolved) {
            recursivelyResolveGlobalParams();
        }

        return globalParamsMap;
    }

    @SuppressWarnings("unchecked")
    public void initGlobalParams() {
        // Global Parameters Injection
        Method[] declaredMethods = this.getClass().getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            try {
                if (declaredMethod.isAnnotationPresent(GlobalParams.class) && declaredMethod.getParameterCount() == 0) {
                    Map<String, Object> invoke = (Map<String, Object>) declaredMethod.invoke(this);
                    globalParamsMap.putAll(invoke);
                }
            } catch (Exception e) {
                log.warn("Get {} Params error", declaredMethod, e);
            }
        }

        globalParamsMap.remove("content");

        recursivelyResolveGlobalParams();
        globalParamsResolved = true;
    }

    private void recursivelyResolveGlobalParams() {
        CURRENT_RESOLVING_DEPTH++;
        boolean allResolved = true;
        for (Map.Entry<String, Object> entry : globalParamsMap.entrySet()) {
            if (entry.getValue() instanceof String value) {
                while (value.contains("${") && value.contains("}")) {
                    allResolved = false;
                    int start = -1, end = -1;
                    for (int i = 0; i < value.length(); i++) {
                        if (value.startsWith("${", i)) {
                            start = i;
                        } else if (value.charAt(i) == '}') {
                            end = i;
                            break;
                        }
                    }
                    if (start != -1 && end != -1) {
                        String key = value.substring(start + 2, end);
                        Object resolvedValue = globalParamsMap.get(key);
                        if (resolvedValue != null) {
                            value = value.substring(0, start) + resolvedValue + value.substring(end + 1);
                            entry.setValue(value);
                        } else {
                            // Log a warning if the key is not found
                            log.warn("Parameter '{}' not found for replacement in '{}'", key, entry.getKey());
                            break;
                        }
                    }
                }
            }
        }

        if (!allResolved) {
            if (CURRENT_RESOLVING_DEPTH >= MAX_RECURSION_DEPTH) {
                CURRENT_RESOLVING_DEPTH = 0;
            } else {
                recursivelyResolveGlobalParams();
            }
        } else {
            CURRENT_RESOLVING_DEPTH = 0;
        }
    }

    public String hostname() {
        return NetUtils.getHostname();
    }

    /**
     * service conf dir
     */
    @Override
    public String confDir() {
        return serviceHome() + "/conf";
    }

    @Override
    public String user() {
        return this.payload.getServiceUser();
    }

    @Override
    public String group() {
        return LocalSettings.cluster().getUserGroup();
    }

    @Override
    public RepoInfo repo() {
        return LocalSettings.repo("general");
    }

    @Override
    public List<PackageInfo> packages() {
        List<PackageInfo> packageInfoList = new ArrayList<>();
        for (PackageSpecificInfo packageSpecificInfo : this.payload.getPackageSpecifics()) {
            if (!packageSpecificInfo.getArch().contains(OSDetection.getArch())) {
                continue;
            }

            packageInfoList.addAll(packageSpecificInfo.getPackages());
        }

        return packageInfoList;
    }

    @Override
    public List<TemplateInfo> templates() {
        return this.payload.getTemplates();
    }

    @Override
    public String javaHome() {
        String root = LocalSettings.cluster().getRootDir();
        return MessageFormat.format("{0}/dependencies/jdk", root);
    }

    @Override
    public String stackHome() {
        String root = LocalSettings.cluster().getRootDir();
        return MessageFormat.format("{0}/services", root);
    }

    @Override
    public String serviceHome() {
        return MessageFormat.format("{0}/{1}", stackHome(), getServiceName());
    }
}
