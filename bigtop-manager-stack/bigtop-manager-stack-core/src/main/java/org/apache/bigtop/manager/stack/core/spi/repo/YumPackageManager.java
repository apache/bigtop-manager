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
package org.apache.bigtop.manager.stack.core.spi.repo;

import org.apache.bigtop.manager.common.shell.ShellExecutor;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.exception.StackException;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@AutoService(PackageManager.class)
public class YumPackageManager implements PackageManager {

    private static final String YUM = "/usr/bin/yum";

    @Override
    public ShellResult installPackage(Collection<String> packages) {
        List<String> builderParameters = new ArrayList<>();
        builderParameters.add(YUM);
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
        builderParameters.add(YUM);
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
    public List<String> listPackages() {
        List<String> builderParameters = new ArrayList<>();
        builderParameters.add(YUM);
        builderParameters.add("list");
        builderParameters.add("installed");

        try {
            ShellResult output = ShellExecutor.execCommand(builderParameters);
            return output.getOutput()
                    .strip()
                    .lines()
                    .skip(1)
                    .map(line -> line.split("\\s+")[0])
                    .map(line -> line.split("\\.")[0])
                    .toList();
        } catch (IOException e) {
            throw new StackException(e);
        }
    }

    @Override
    public String getName() {
        return PackageManagerType.YUM.name();
    }
}
