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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.tez;

import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.grpc.pojo.PackageInfo;
import org.apache.bigtop.manager.grpc.pojo.RepoInfo;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.spi.script.AbstractClientScript;
import org.apache.bigtop.manager.stack.core.spi.script.Script;
import org.apache.bigtop.manager.stack.core.tarball.FileDownloader;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;

@Slf4j
@AutoService(Script.class)
public class TezClientScript extends AbstractClientScript {

    /**
     * Tez tarball file doesn't need to be extracted, so here we override {@link #add(Params)} method
     * and do nothing after download the tarball file.
     *
     * @param params the parameters required for installation
     * @return ShellResult
     */
    @Override
    public ShellResult add(Params params) {
        RepoInfo repo = params.repo();
        List<PackageInfo> packages = params.packages();

        String repoUrl = repo.getBaseUrl();
        String stackHome = params.stackHome();
        for (PackageInfo packageInfo : packages) {
            String remoteUrl = repoUrl + File.separator + packageInfo.getName();
            FileDownloader.download(remoteUrl, stackHome, packageInfo);
        }

        return ShellResult.success();
    }

    @Override
    public ShellResult configure(Params params) {
        super.configure(params);

        return TezSetup.configure(params);
    }

    @Override
    public String getComponentName() {
        return "tez_client";
    }
}
