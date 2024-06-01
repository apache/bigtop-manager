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
package org.apache.bigtop.manager.stack.common.utils;

import org.apache.bigtop.manager.common.enums.OSType;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.common.utils.os.OSDetection;
import org.apache.bigtop.manager.spi.plugin.PrioritySPIFactory;
import org.apache.bigtop.manager.spi.stack.PackageManager;
import org.apache.bigtop.manager.stack.common.enums.PackageManagerType;
import org.apache.bigtop.manager.stack.common.exception.StackException;

import org.apache.commons.lang3.EnumUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PackageUtils {

    private static final Map<String, PackageManager> PACKAGE_MANAGER_MAP;

    static {
        PrioritySPIFactory<PackageManager> spiFactory = new PrioritySPIFactory<>(PackageManager.class);
        PACKAGE_MANAGER_MAP = spiFactory.getSPIMap();
    }

    public static PackageManager getPackageManager() {
        String os = OSDetection.getOS();
        OSType currentOS;
        if (EnumUtils.isValidEnumIgnoreCase(OSType.class, os)) {
            currentOS = OSType.valueOf(os.toUpperCase());
        } else {
            throw new StackException("PackageManager Unsupported OS for [" + os + "]");
        }

        PackageManager packageManager = null;
        PackageManagerType[] values = PackageManagerType.values();
        for (PackageManagerType value : values) {
            List<OSType> osTypes = value.getOsTypes();
            if (osTypes.contains(currentOS)) {
                packageManager = PACKAGE_MANAGER_MAP.get(value.name());
                break;
            }
        }

        if (packageManager == null) {
            throw new StackException("Unsupported PackageManager for [" + os + "]");
        }
        return packageManager;
    }

    /**
     * install package
     *
     * @param packageList packages need to be installed
     */
    public static ShellResult install(Collection<String> packageList) {
        if (packageList == null || packageList.isEmpty()) {
            ShellResult shellResult = new ShellResult();
            shellResult.setExitCode(-1);
            shellResult.setErrMsg("packageList is empty");
            return shellResult;
        }

        return getPackageManager().installPackage(packageList);
    }

}
