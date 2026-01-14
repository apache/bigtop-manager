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
public class EnableHdfsHaReq {

    @NotBlank
    @Schema(description = "Active NameNode hostname", example = "nn-a")
    private String activeNameNodeHost;

    @NotBlank
    @Schema(description = "Standby NameNode hostname", example = "nn-b")
    private String standbyNameNodeHost;

    @NotEmpty
    @Schema(description = "JournalNode hostnames, must be >=3", example = "[\"jn-1\",\"jn-2\",\"jn-3\"]")
    private List<String> journalNodeHosts;

    @Schema(description = "Zookeeper service id (legacy, optional)", example = "12")
    private Long zookeeperServiceId;

    @Schema(
            description = "Zookeeper hosts (preferred). If specified, server will build quorum as host:2181 and ignore zookeeperServiceId.",
            example = "[\"zk-1\",\"zk-2\",\"zk-3\"]")
    private List<String> zookeeperHosts;

    @NotEmpty
    @Schema(description = "ZKFC hostnames (usually 2 namenodes)", example = "[\"nn-a\",\"nn-b\"]")
    private List<String> zkfcHosts;

    @NotBlank
    @Schema(description = "HDFS nameservice, allow rename", example = "nameservice1")
    private String nameservice;
}
