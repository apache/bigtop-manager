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

import org.apache.bigtop.manager.common.enums.MaintainState;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.UniqueConstraint;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "host",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_hostname",
                    columnNames = {"hostname", "cluster_id"})
        },
        indexes = {@Index(name = "idx_host_cluster_id", columnList = "cluster_id")})
@TableGenerator(name = "host_generator", table = "sequence", pkColumnName = "seq_name", valueColumnName = "seq_count")
public class HostPO extends BasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "host_generator")
    @Column(name = "id")
    private Long id;

    @Column(name = "hostname")
    private String hostname;

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

    @Column(name = "state")
    private MaintainState state;

    @Column(name = "cluster_id")
    private Long clusterId;

}
