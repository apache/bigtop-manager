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

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.message.entity.payload.CommandPayload;
import org.apache.bigtop.manager.common.message.entity.pojo.OSSpecificInfo;
import org.apache.bigtop.manager.common.utils.NetUtils;
import org.apache.bigtop.manager.common.utils.os.OSDetection;
import org.apache.bigtop.manager.stack.core.spi.Params;
import org.apache.bigtop.manager.stack.core.annotations.GlobalParams;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.bigtop.manager.common.constants.Constants.ROOT_USER;

@Slf4j
public abstract class BaseParams implements Params {

    @Getter
    protected final Map<String, Object> globalParamsMap = new HashMap<>();

    public static final String LIMITS_CONF_DIR = "/etc/security/limits.d";

    protected final CommandPayload commandPayload;

    @SuppressWarnings("unchecked")
    protected BaseParams(CommandPayload commandPayload) {
        this.commandPayload = commandPayload;

        Command command = commandPayload.getCommand();
        if (command == Command.INSTALL) {
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

    /**
     * get the package list according to the os and arch
     */
    public List<String> getPackageList() {
        List<OSSpecificInfo> osSpecifics = this.commandPayload.getOsSpecifics();
        if (osSpecifics == null) {
            return List.of();
        }

        String os = OSDetection.getOS();
        String arch = OSDetection.getArch();
        for (OSSpecificInfo osSpecific : osSpecifics) {
            List<String> pkgOS = osSpecific.getOs();
            List<String> pkgArch = osSpecific.getArch();
            if (pkgOS.contains(os) && pkgArch.contains(arch)) {
                return osSpecific.getPackages();
            }
        }

        return List.of();
    }

    public String hostname() {
        return NetUtils.getHostname();
    }

    public String stackBinDir() {
        String stackName = this.commandPayload.getStackName();
        String stackVersion = this.commandPayload.getStackVersion();
        String root = this.commandPayload.getRoot();
        return MessageFormat.format("{0}/{1}/{2}/usr/bin", root, stackName.toLowerCase(), stackVersion);
    }

    public String stackLibDir() {
        String stackName = this.commandPayload.getStackName();
        String stackVersion = this.commandPayload.getStackVersion();
        String root = this.commandPayload.getRoot();
        return MessageFormat.format("{0}/{1}/{2}/usr/lib", root, stackName.toLowerCase(), stackVersion);
    }

    /**
     * service home dir
     */
    public String serviceHome() {
        String service = this.commandPayload.getServiceName();
        return stackLibDir() + "/" + service.toLowerCase();
    }

    /**
     * service conf dir
     */
    public String confDir() {
        return "/etc/" + this.commandPayload.getServiceName().toLowerCase() + "/conf";
    }

    public String user() {
        return StringUtils.isNotBlank(this.commandPayload.getServiceUser())
                ? this.commandPayload.getServiceUser()
                : ROOT_USER;
    }

    public String group() {
        return LocalSettings.cluster().getUserGroup();
    }

    public String serviceName() {
        return this.commandPayload.getServiceName();
    }
}
