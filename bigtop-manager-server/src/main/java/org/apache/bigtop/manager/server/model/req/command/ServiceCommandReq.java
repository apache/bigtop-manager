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
package org.apache.bigtop.manager.server.model.req.command;

import org.apache.bigtop.manager.server.config.CommandGroupSequenceProvider;
import org.apache.bigtop.manager.server.model.req.ComponentHostReq;
import org.apache.bigtop.manager.server.model.req.ServiceConfigReq;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class ServiceCommandReq {

    @NotNull @Schema(description = "Service name", example = "zookeeper")
    private String serviceName;

    @Schema(description = "Whether the service is already installed", example = "false")
    private Boolean installed;

    @NotEmpty(groups = {CommandGroupSequenceProvider.ServiceInstallCommandGroup.class})
    @Schema(description = "Components for service on each hosts")
    private List<@Valid ComponentHostReq> componentHosts;

    @NotEmpty(groups = {CommandGroupSequenceProvider.ServiceInstallCommandGroup.class})
    @Schema(description = "Configs for service")
    private List<@Valid ServiceConfigReq> configs;
}
