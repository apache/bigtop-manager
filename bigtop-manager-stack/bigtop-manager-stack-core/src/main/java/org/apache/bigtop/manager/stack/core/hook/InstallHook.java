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
package org.apache.bigtop.manager.stack.core.hook;

import org.apache.bigtop.manager.common.message.entity.pojo.RepoInfo;
import org.apache.bigtop.manager.common.utils.os.OSDetection;
import org.apache.bigtop.manager.spi.stack.Hook;
import org.apache.bigtop.manager.stack.common.utils.LocalSettings;
import org.apache.bigtop.manager.stack.common.utils.PackageUtils;
import org.apache.bigtop.manager.stack.common.utils.template.BaseTemplate;

import java.util.List;

import com.google.auto.service.AutoService;

import lombok.extern.slf4j.Slf4j;

/**
 * obtain agent execute command
 */
@Slf4j
@AutoService(Hook.class)
public class InstallHook extends AbstractHook {

    public static final String NAME = "install";

    @Override
    public void doBefore() {
        List<RepoInfo> repos = LocalSettings.repos();
        String repoTemplate = LocalSettings.cluster().getRepoTemplate();

        for (RepoInfo repo : repos) {
            if (OSDetection.getOS().equals(repo.getOs()) && OSDetection.getArch().equals(repo.getArch())) {
                BaseTemplate.writeCustomTemplate("/etc/yum.repos.d/" + repo.getRepoId().replace(".", "_") + ".repo",
                        repo, repoTemplate);
            }
        }

        List<String> packages = LocalSettings.packages();
        PackageUtils.install(packages);
    }

    @Override
    public void doAfter() {
    }

    @Override
    public String getName() {
        return NAME;
    }
}
