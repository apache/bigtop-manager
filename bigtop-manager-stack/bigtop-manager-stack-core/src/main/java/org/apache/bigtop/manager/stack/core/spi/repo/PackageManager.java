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

import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.spi.PrioritySPI;

import java.util.Collection;
import java.util.List;

/**
 * Interface representing a package manager.
 * Provides methods to install, uninstall, and list packages for different type of operating systems.
 * <br/>
 * See {@link PackageManagerType} for all supported systems.
 */
public interface PackageManager extends PrioritySPI {

    /**
     * Install packages.
     *
     * @param packages a collection of package names to be installed
     * @return a ShellResult object containing the result of the installation process
     */
    ShellResult installPackage(Collection<String> packages);

    /**
     * Uninstall packages.
     *
     * @param packages a collection of package names to be uninstalled
     * @return a ShellResult object containing the result of the uninstallation process
     */
    ShellResult uninstallPackage(Collection<String> packages);

    /**
     * List all packages.
     *
     * @return a string representation of the list of installed packages
     */
    List<String> listPackages();
}
