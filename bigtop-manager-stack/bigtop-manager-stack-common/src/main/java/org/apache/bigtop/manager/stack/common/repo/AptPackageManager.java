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
package org.apache.bigtop.manager.stack.common.repo;

import org.apache.bigtop.manager.common.shell.ShellExecutor;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.spi.stack.PackageManager;
import org.apache.bigtop.manager.stack.common.enums.PackageManagerType;
import org.apache.bigtop.manager.stack.common.exception.StackException;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@AutoService(PackageManager.class)
public class AptPackageManager implements PackageManager {

    private static final String APT = "/usr/bin/apt";
    private static final String APT_GET = "/usr/bin/apt-get";

    @Override
    public ShellResult installPackage(Collection<String> packages) {
        List<String> builderParameters = new ArrayList<>();
        builderParameters.add(APT_GET);
        builderParameters.add("install");
        builderParameters.add("-y");
        builderParameters.addAll(packages);

        try {
            return ShellExecutor.execCommand(builderParameters, true);
        } catch (IOException e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult uninstallPackage(Collection<String> packages) {
        List<String> builderParameters = new ArrayList<>();
        builderParameters.add(APT_GET);
        builderParameters.add("remove");
        builderParameters.add("-y");
        builderParameters.addAll(packages);

        try {
            return ShellExecutor.execCommand(builderParameters, true);
        } catch (IOException e) {
            throw new StackException(e);
        }
    }

    @Override
    public String listPackages() {
        List<String> builderParameters = new ArrayList<>();
        builderParameters.add(APT);
        builderParameters.add("list");

        try {
            ShellResult output = ShellExecutor.execCommand(builderParameters);
            return output.getOutput();
        } catch (IOException e) {
            throw new StackException(e);
        }
    }

    @Override
    public String getName() {
        return PackageManagerType.APT.name();
    }
}
