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

import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.grpc.pojo.PackageInfo;
import org.apache.bigtop.manager.grpc.pojo.RepoInfo;
import org.apache.bigtop.manager.grpc.pojo.TemplateInfo;
import org.apache.bigtop.manager.stack.core.enums.ConfigType;
import org.apache.bigtop.manager.stack.core.spi.param.BaseParams;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.TarballUtils;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxFileUtils;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.apache.bigtop.manager.common.constants.Constants.PERMISSION_755;

@Slf4j
public abstract class AbstractScript implements Script {

    public static final String PROPERTY_KEY_SKIP_LEVELS = "skipLevels";

    @Override
    public ShellResult add(Params params) {
        return this.add(params, new Properties());
    }

    public ShellResult add(Params params, Properties properties) {
        RepoInfo repo = params.repo();
        List<PackageInfo> packages = params.packages();
        String stackHome = params.stackHome();
        String serviceHome = params.serviceHome();

        if (!Files.exists(Path.of(stackHome))) {
            String user = System.getProperty("user.name");
            LinuxFileUtils.createDirectories(stackHome, user, user, Constants.PERMISSION_755, true);
        }

        for (PackageInfo packageInfo : packages) {
            Integer skipLevels = Integer.parseInt(properties.getProperty(PROPERTY_KEY_SKIP_LEVELS, "0"));
            TarballUtils.installPackage(repo.getBaseUrl(), stackHome, serviceHome, packageInfo, skipLevels);

            // Dir already created by TarballUtils, this changes the owner and permission for the service home
            LinuxFileUtils.createDirectories(
                    serviceHome, params.user(), params.group(), Constants.PERMISSION_755, true);
        }

        return ShellResult.success();
    }

    @Override
    public ShellResult configure(Params params) {
        List<TemplateInfo> templates = params.templates();
        for (TemplateInfo template : templates) {
            String filename = params.serviceHome() + "/" + template.getDest();
            String dir = Path.of(filename).getParent().toString();
            LinuxFileUtils.createDirectories(dir, params.user(), params.group(), PERMISSION_755, true);
            LinuxFileUtils.toFile(
                    ConfigType.CONTENT,
                    filename,
                    params.user(),
                    params.group(),
                    Constants.PERMISSION_755,
                    template.getContent(),
                    ((BaseParams) params).getGlobalParamsMap());
        }

        return ShellResult.success();
    }

    @Override
    public ShellResult init(Params params) {
        return ShellResult.success();
    }

    @Override
    public ShellResult prepare(Params params) {
        return ShellResult.success();
    }

    @Override
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
