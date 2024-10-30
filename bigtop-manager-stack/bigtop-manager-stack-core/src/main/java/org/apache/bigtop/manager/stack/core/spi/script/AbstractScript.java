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
package org.apache.bigtop.manager.stack.core.spi.script;

import org.apache.bigtop.manager.common.message.entity.pojo.PackageInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.RepoInfo;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.param.Params;
import org.apache.bigtop.manager.stack.core.utils.TarballUtils;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Properties;

@Slf4j
public abstract class AbstractScript implements Script {

    public static final String PROPERTY_KEY_SKIP_LEVELS = "skipLevels";

    @Override
    public ShellResult install(Params params) {
        return this.install(params, new Properties());
    }

    public ShellResult install(Params params, Properties properties) {
        RepoInfo repo = params.repo();
        List<PackageInfo> packages = params.packages();
        String stackHome = params.stackHome();
        String serviceHome = params.serviceHome();
        for (PackageInfo packageInfo : packages) {
            Integer skipLevels = Integer.parseInt(properties.getProperty(PROPERTY_KEY_SKIP_LEVELS, "0"));
            TarballUtils.installPackage(repo.getBaseUrl(), stackHome, serviceHome, packageInfo, skipLevels);
        }

        return ShellResult.success();
    }

    public ShellResult restart(Params params) {
        ShellResult shellResult = stop(params);
        if (shellResult.getExitCode() != 0) {
            return shellResult;
        }
        ShellResult shellResult1 = start(params);
        if (shellResult1.getExitCode() != 0) {
            return shellResult1;
        }

        return new ShellResult(
                0,
                StringUtils.join(shellResult.getOutput(), shellResult1.getOutput()),
                StringUtils.join(shellResult.getErrMsg(), shellResult1.getErrMsg()));
    }

    @Override
    public ShellResult check(Params params) {
        return ShellResult.success();
    }
}
