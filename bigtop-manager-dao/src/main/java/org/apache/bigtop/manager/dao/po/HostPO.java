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
package org.apache.bigtop.manager.dao.po;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "host")
public class HostPO extends BasePO implements Serializable {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "hostname")
    private String hostname;

    @Column(name = "ssh_user")
    private String sshUser;

    @Column(name = "ssh_port")
    private Integer sshPort;

    @Column(name = "auth_type")
    private Integer authType;

    @Column(name = "ssh_password")
    private String sshPassword;

    @Column(name = "ssh_key_string")
    private String sshKeyString;

    @Column(name = "ssh_key_filename")
    private String sshKeyFilename;

    @Column(name = "ssh_key_password")
    private String sshKeyPassword;

    @Column(name = "grpc_port")
    private Integer grpcPort;

    @Column(name = "ipv4")
    private String ipv4;

    @Column(name = "ipv6")
    private String ipv6;

    @Column(name = "arch")
    private String arch;

    @Column(name = "os")
    private String os;

    @Column(name = "available_processors")
    private Integer availableProcessors;

    @Column(name = "free_memory_size")
    private Long freeMemorySize;

    @Column(name = "total_memory_size")
    private Long totalMemorySize;

    @Column(name = "free_disk")
    private Long freeDisk;

    @Column(name = "total_disk")
    private Long totalDisk;

    @Column(name = "desc")
    private String desc;

    @Column(name = "status")
    private Integer status;

    @Column(name = "err_info")
    private String errInfo;

    @Column(name = "cluster_id", nullable = false)
    private Long clusterId;

    @Transient
    private String clusterName;

    @Transient
    private Integer componentNum;
}
