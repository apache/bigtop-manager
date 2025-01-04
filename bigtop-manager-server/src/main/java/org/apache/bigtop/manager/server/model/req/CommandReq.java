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

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.server.config.CommandGroupSequenceProvider;
import org.apache.bigtop.manager.server.enums.CommandLevel;
import org.apache.bigtop.manager.server.model.req.command.ClusterCommandReq;
import org.apache.bigtop.manager.server.model.req.command.ComponentCommandReq;
import org.apache.bigtop.manager.server.model.req.command.HostCommandReq;
import org.apache.bigtop.manager.server.model.req.command.ServiceCommandReq;

import org.hibernate.validator.group.GroupSequenceProvider;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@GroupSequenceProvider(CommandGroupSequenceProvider.class)
public class CommandReq {

    @NotNull @Schema(example = "start")
    private Command command;

    @Schema(example = "custom_command")
    private String customCommand;

    @Schema(example = "1")
    private Long clusterId;

    @NotNull @Schema(example = "cluster")
    private CommandLevel commandLevel;

    @Schema(description = "Command details for cluster level command")
    private ClusterCommandReq clusterCommand;

    @Schema(description = "Command details for host level command")
    private List<@Valid HostCommandReq> hostCommands;

    @Schema(description = "Command details for service level command")
    private List<@Valid ServiceCommandReq> serviceCommands;

    @Schema(description = "Command details for component level command")
    private List<@Valid ComponentCommandReq> componentCommands;
}
