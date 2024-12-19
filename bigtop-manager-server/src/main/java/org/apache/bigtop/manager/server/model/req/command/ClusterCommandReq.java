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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class ClusterCommandReq {

    @NotEmpty
    @Schema(example = "c1")
    private String name;

    @NotEmpty
    @Schema(example = "c1")
    private String displayName;

    @Schema(example = "desc")
    private String desc;

    @NotNull @Schema(example = "1")
    private Integer type;

    @Schema(example = "hadoop")
    private String userGroup;

    @Schema(example = "/opt")
    private String rootDir;

    @Schema(example = "[1, 2]")
    private List<Long> hostIds;
}
