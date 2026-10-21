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
package org.apache.bigtop.manager.stack.extra.v1_0_0.trino;

import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.grpc.pojo.RepoInfo;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.spi.script.Script;
import org.apache.bigtop.manager.stack.core.tarball.FileDownloader;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxFileUtils;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
@AutoService(Script.class)
public class TrinoClientScript extends AbstractTrinoScript {

    @Override
    public ShellResult add(Params params) {
        String cliFileName = "trino-cli-479";
        String downTmp = "/tmp";
        String binPath = "/usr/bin/";
        RepoInfo generalRepo = LocalSettings.repo("general");
        FileDownloader.download(getCliDownloadPath(generalRepo.getBaseUrl(), cliFileName), downTmp);
        LinuxFileUtils.moveFile(downTmp + File.separator + cliFileName, binPath + cliFileName);
        LinuxFileUtils.updateOwner(binPath + cliFileName, params.user(), params.group(), false);
        LinuxFileUtils.updatePermissions(binPath + cliFileName, Constants.PERMISSION_755, false);
        return ShellResult.success();
    }

    @Override
    public ShellResult configure(Params params) {
        return super.configure(params);
    }

    @Override
    public String getComponentName() {
        return "trino_client";
    }

    private String getCliDownloadPath(String basePath, String fileName) {
        if (basePath.endsWith(File.separator) && fileName.startsWith(File.separator)) {
            return basePath + fileName.substring(1);
        } else if (basePath.endsWith(File.separator) || fileName.startsWith(File.separator)) {
            return basePath + fileName;
        } else {
            return basePath + File.separator + fileName;
        }
    }
}
