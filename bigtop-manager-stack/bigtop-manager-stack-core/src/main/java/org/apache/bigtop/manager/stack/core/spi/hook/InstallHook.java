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
package org.apache.bigtop.manager.stack.core.spi.hook;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.bigtop.manager.common.message.entity.pojo.RepoInfo;
import org.apache.bigtop.manager.common.utils.os.OSDetection;
import org.apache.bigtop.manager.stack.core.param.Params;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;
import org.apache.bigtop.manager.stack.core.utils.PackageUtils;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxAccountUtils;
import org.apache.bigtop.manager.stack.core.utils.template.BaseTemplate;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.List;

/**
 * obtain agent execute command
 */
@Slf4j
@AutoService(Hook.class)
public class InstallHook extends AbstractHook {

    public static final String NAME = "install";

    @Override
    public void doBefore(Params params) {
        addUserAndGroup(params);

        writeRepo(params);

        installPackages(params);
    }

    @Override
    public void doAfter(Params params) {}

    @Override
    public String getName() {
        return NAME;
    }

    private void addUserAndGroup(Params params) {
        String user = params.user();
        String group = params.group();

        LinuxAccountUtils.groupAdd(group);
        String primaryGroup = LinuxAccountUtils.getUserPrimaryGroup(user);
        if (primaryGroup == null || !primaryGroup.equals(group)) {
            log.info("Adding user: [{}] to group: [{}]", user, group);
            LinuxAccountUtils.userAdd(user, group);
        }
    }

    private void writeRepo(Params params) {
        RepoInfo repo = LocalSettings.repos().stream()
                .filter(r -> OSDetection.getOS().equals(r.getOs())
                        && OSDetection.getArch().equals(r.getArch()))
                .findFirst()
                .orElse(null);

        if (repo != null) {
            String filename = "/etc/yum.repos.d/" + repo.getRepoId().replace(".", "_") + ".repo";
            if (!new File(filename).exists()) {
                log.info("Writing repo file: [{}]", filename);
                String repoTemplate = LocalSettings.cluster().getRepoTemplate();
                BaseTemplate.writeCustomTemplate(filename, repo, repoTemplate);
            }
        }
    }

    private void installPackages(Params params) {
        List<String> installedPackages = PackageUtils.listInstalledPackages();
        List<String> needInstallPackages = params.getPackageList().stream()
                .filter(pkg -> !installedPackages.contains(pkg))
                .toList();

        if (!CollectionUtils.isEmpty(needInstallPackages)) {
            PackageUtils.install(needInstallPackages);
        }
    }
}
