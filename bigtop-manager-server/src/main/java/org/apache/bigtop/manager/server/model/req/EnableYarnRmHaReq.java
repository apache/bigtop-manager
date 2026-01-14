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
package org.apache.bigtop.manager.server.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class EnableYarnRmHaReq {

    @NotBlank
    @Schema(description = "Active ResourceManager hostname (rm1)", example = "rm-a")
    private String activeResourceManagerHost;

    @NotBlank
    @Schema(description = "Standby ResourceManager hostname (rm2)", example = "rm-b")
    private String standbyResourceManagerHost;

    @NotEmpty
    @Schema(description = "RM IDs used by yarn.resourcemanager.ha.rm-ids", example = "[\"rm1\",\"rm2\"]")
    private List<String> rmIds;

    @NotBlank
    @Schema(description = "YARN cluster id (yarn.resourcemanager.cluster-id)", example = "yarn-cluster")
    private String yarnClusterId;

    @Schema(description = "Zookeeper service id (legacy, optional)", example = "12")
    private Long zookeeperServiceId;

    @Schema(
            description = "Zookeeper hosts (preferred). If specified, server will build zk-address as host:2181 and ignore zookeeperServiceId.",
            example = "[\"zk-1\",\"zk-2\",\"zk-3\"]")
    private List<String> zookeeperHosts;
}
