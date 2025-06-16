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
package org.apache.bigtop.manager.stack.core.spi.param;

import org.apache.bigtop.manager.grpc.pojo.PackageInfo;
import org.apache.bigtop.manager.grpc.pojo.RepoInfo;
import org.apache.bigtop.manager.grpc.pojo.TemplateInfo;
import org.apache.bigtop.manager.stack.core.spi.PrioritySPI;

import java.util.List;

public interface Params extends PrioritySPI {

    void initGlobalParams();

    String confDir();

    String user();

    String group();

    RepoInfo repo();

    List<PackageInfo> packages();

    List<TemplateInfo> templates();

    String javaHome();

    String stackHome();

    String serviceHome();

    String getServiceName();

    @Override
    default String getName() {
        return getServiceName();
    }
}
