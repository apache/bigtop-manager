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
package org.apache.bigtop.manager.stack.infra.param;

import org.apache.bigtop.manager.common.utils.Environments;
import org.apache.bigtop.manager.grpc.payload.ComponentCommandPayload;
import org.apache.bigtop.manager.stack.core.spi.param.BaseParams;

import org.apache.commons.lang3.SystemUtils;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.Map;

@Slf4j
@NoArgsConstructor
public abstract class InfraParams extends BaseParams {

    protected InfraParams(ComponentCommandPayload componentCommandPayload) {
        super(componentCommandPayload);
    }

    /**
     * Infra stack do not belong to any cluster, so we need to override this and provide a group name
     *
     * @return group name
     */
    @Override
    public String group() {
        return "infra";
    }

    /**
     * Infra stack do not belong to any cluster, we cannot use stack home of cluster
     *
     * @return group name
     */
    @Override
    public String stackHome() {
        // Parent path of agent dir, which is bigtop-manager-agent/../
        String parentPath;
        if (Environments.isDevMode()) {
            return SystemUtils.getUserDir().getParentFile().getPath();
        } else {
            parentPath = new File(InfraParams.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .getPath())
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getPath();
        }

        return parentPath + "/infras";
    }

    public Map<String, List<String>> getClusterHosts() {
        // In Component Status stage, clusterHosts is null
        return payload.getClusterHosts();
    }
}
