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
package org.apache.bigtop.manager.server.model.vo;

import lombok.Data;

@Data
public class HostVO {

    private Long id;

    private String hostname;

    private String sshUser;

    private Integer sshPort;

    private Integer authType;

    private String sshPassword;

    private String sshKeyString;

    private String sshKeyFilename;

    private String sshKeyPassword;

    private Integer grpcPort;

    private String ipv4;

    private String ipv6;

    private String arch;

    private String os;

    private Integer availableProcessors;

    private Long freeMemorySize;

    private Long totalMemorySize;

    private Long freeDisk;

    private Long totalDisk;

    private String desc;

    private Integer status;

    private String errInfo;

    private String clusterName;

    private Integer componentNum;
}
