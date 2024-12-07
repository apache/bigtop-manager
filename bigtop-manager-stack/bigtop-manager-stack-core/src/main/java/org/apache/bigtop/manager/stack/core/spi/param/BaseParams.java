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

import lombok.NoArgsConstructor;
import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.message.entity.payload.CommandPayload;
import org.apache.bigtop.manager.common.message.entity.pojo.PackageInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.PackageSpecificInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.RepoInfo;
import org.apache.bigtop.manager.common.utils.NetUtils;
import org.apache.bigtop.manager.common.utils.os.OSDetection;
import org.apache.bigtop.manager.stack.core.annotations.GlobalParams;
import org.apache.bigtop.manager.stack.core.exception.StackException;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;

import lombok.Getter;
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

    @Getter
    protected final Map<String, Object> globalParamsMap = new HashMap<>();

    public static final String LIMITS_CONF_DIR = "/etc/security/limits.d";

    protected CommandPayload commandPayload;

    @SuppressWarnings("unchecked")
    protected BaseParams(CommandPayload commandPayload) {
        this.commandPayload = commandPayload;

        Command command = commandPayload.getCommand();
        if (command == Command.ADD) {
            return;
        }
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
        return this.commandPayload.getServiceUser();
    }

    @Override
    public String group() {
        return LocalSettings.cluster().getUserGroup();
    }

    @Override
    public RepoInfo repo() {
        return LocalSettings.repos().stream()
                // Use service tarballs repo(type == 1)
                .filter(r -> OSDetection.getArch().equals(r.getArch()) && r.getType() == 1)
                .findFirst()
                .orElseThrow(() -> new StackException(
                        "Cannot find repo for os: [{0}] and arch: [{1}]", OSDetection.getOS(), OSDetection.getArch()));
    }

    @Override
    public List<PackageInfo> packages() {
        RepoInfo repo = this.repo();
        List<PackageInfo> packageInfoList = new ArrayList<>();
        for (PackageSpecificInfo packageSpecificInfo : this.commandPayload.getPackageSpecifics()) {
            if (!packageSpecificInfo.getArch().contains(repo.getArch())) {
                continue;
            }

            packageInfoList.addAll(packageSpecificInfo.getPackages());
        }

        return packageInfoList;
    }

    @Override
    public String serviceName() {
        return this.commandPayload.getServiceName();
    }

    @Override
    public String stackHome() {
        String root = LocalSettings.cluster().getRootDir();
        return MessageFormat.format("{0}/services", root);
    }

    @Override
    public String serviceHome() {
        String service = this.commandPayload.getServiceName();
        return MessageFormat.format("{0}/{1}", stackHome(), service.toLowerCase());
    }
}
