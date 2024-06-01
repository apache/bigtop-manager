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
package org.apache.bigtop.manager.stack.common.enums;

import static org.apache.bigtop.manager.common.enums.OSType.CENTOS7;
import static org.apache.bigtop.manager.common.enums.OSType.DEBIAN10;
import static org.apache.bigtop.manager.common.enums.OSType.DEBIAN11;
import static org.apache.bigtop.manager.common.enums.OSType.FEDORA36;
import static org.apache.bigtop.manager.common.enums.OSType.ROCKY8;
import static org.apache.bigtop.manager.common.enums.OSType.UBUNTU20;
import static org.apache.bigtop.manager.common.enums.OSType.UBUNTU22;

import org.apache.bigtop.manager.common.enums.OSType;

import java.util.List;

import lombok.Getter;

@Getter
public enum PackageManagerType {

    YUM(List.of(CENTOS7)),

    DNF(List.of(ROCKY8, FEDORA36)),

    APT(List.of(UBUNTU20, UBUNTU22, DEBIAN10, DEBIAN11)),

    ;

    /**
     * Supported OS Types for Package Manager
     */
    private final List<OSType> osTypes;

    PackageManagerType(List<OSType> osTypes) {
        this.osTypes = osTypes;
    }

}
